package org.checkerframework.checker.units;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.CheckOp;
import org.checkerframework.checker.units.qual.Op;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.checker.units.qual.UnitsTypeCheckAsOp;
import org.checkerframework.checker.units.qual.g;
import org.checkerframework.checker.units.qual.kg;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeFormatter;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ErrorReporter;
import org.checkerframework.javacutil.TreeUtils;

/**
 * Annotated Type Factory for the Units Checker.
 *
 * <p>This class:
 *
 * <p>- Orchestrates the loading and post-processing of bundled and external units, delegating
 * details to {@link UnitsAnnotationClassLoader}.
 *
 * <p>- Normalizes alias annotations via {@link UnitsAliasManager}.
 *
 * <p>- Formats error message presentation of units via {@link UnitsAnnotatedTypeFormatter}.
 *
 * <p>- Defines subtyping relationships between prefixed multiples of units via {@link
 * UnitsQualifierHierarchy}.
 *
 * <p>- Defines arithmetic relationships between units via {@link UnitsRelationsManager}.
 *
 * <p>- Computes and propagates units annotations for expressions in an AST via {@link
 * UnitsPropagationTreeAnnotator}.
 */
public class UnitsAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    private final AnnotationMirror UNKNOWN;
    private final AnnotationMirror BOTTOM;

    private final UnitsRelationsEnforcer enforcer;
    private final UnitsRelationsManager relations;

    public UnitsAnnotatedTypeFactory(BaseTypeChecker checker) {
        // Use data-flow based local type inference
        super(checker, true);

        // Setup units annotation mirrors before calling postInit()
        UnitsMirrors mirrors = UnitsMirrors.getInstance(checker);
        UNKNOWN = mirrors.UNKNOWN;
        BOTTOM = mirrors.BOTTOM;

        this.postInit();

        // During createSupportedTypeQualifiers() within postInit(), the
        // relations map contains all user defined relations and default
        // relations between each unit and Dimensionless, UnknownUnits, and
        // UnitsBottom. Fill in the rest here.
        relations = UnitsRelationsManager.getInstance(checker, this);
        relations.completeRelationMaps();

        // Create an instance of the enforcer
        enforcer = UnitsRelationsEnforcer.getInstance(checker, this);

        // Print CSV file if requested in command line
        if (checker.hasOption("writeCSV")) {
            String csvFilePath = checker.getOption("writeCSV");
            if (csvFilePath != null) {
                boolean printUU = checker.hasOption("printUU");
                relations.writeCSV(csvFilePath, printUU);
            } else {
                checker.errorAbort(
                        "The writeCSV option must be used with a file path. Ex: -AwriteCSV=$PWD");
            }
        }
    }

    /**
     * Overrides and creates a custom annotated type formatter to format the print out of qualifiers
     * by removing {@link Prefix#one}.
     */
    @Override
    protected AnnotatedTypeFormatter createAnnotatedTypeFormatter() {
        return new UnitsAnnotatedTypeFormatter(checker);
    }

    /**
     * Converts all prefixed units' alias annotations (eg {@link kg}) into base unit annotations
     * with prefix values (eg {@link g}({@link Prefix#kilo})) via the {@link UnitsAliasManager}. If
     * the user uses prefixed base units directly, it is not considered an alias annotation.
     */
    @Override
    public AnnotationMirror aliasedAnnotation(AnnotationMirror anno) {
        AnnotationMirror baseAnno = UnitsAliasManager.getInstance(checker).getAliasAnnotation(anno);
        if (baseAnno == null) {
            baseAnno = super.aliasedAnnotation(anno);
        }
        return baseAnno;
    }

    /**
     * Loads and extracts units relationships from bundled and external units via the {@link
     * UnitsAnnotationClassLoader}.
     */
    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {

        // Use the Units Annotated Type Loader instead of the default one
        UnitsAnnotationClassLoader unitsLoader = new UnitsAnnotationClassLoader(checker, this);
        loader = unitsLoader;

        // get all the loaded annotations
        Set<Class<? extends Annotation>> qualSet = new HashSet<>();
        qualSet.addAll(getBundledTypeQualifiersWithPolyAll());

        // load all the external units
        unitsLoader.loadAllExternalUnits();

        // copy all loaded Units to qual set
        qualSet.addAll(unitsLoader.getLoadedAnnotationClasses());

        return qualSet;
    }

    /**
     * Override to use {@link ImplicitsTreeAnnotator} for the null literal and void class, and
     * {@link UnitsPropagationTreeAnnotator} to type check and propagate units.
     */
    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(
                new ImplicitsTreeAnnotator(this), new UnitsPropagationTreeAnnotator(this));
    }

    /**
     * Units Propagation Tree Annotator type checks and computes resulting units for arithmetic and
     * comparison operations as expressed in binary operations, compound assignment operations, or
     * method invocations of tagged methods.
     */
    protected class UnitsPropagationTreeAnnotator extends PropagationTreeAnnotator {

        public UnitsPropagationTreeAnnotator(AnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        /** Type check and propagate resulting units for arithmetic and comparison operations */
        @Override
        public Void visitBinary(BinaryTree node, AnnotatedTypeMirror type) {
            AnnotatedTypeMirror lht = getAnnotatedType(node.getLeftOperand());
            AnnotatedTypeMirror rht = getAnnotatedType(node.getRightOperand());
            Tree.Kind kind = node.getKind();

            // Remove Prefix.one
            if (UnitsRelationsTools.getPrefixValue(lht) == Prefix.one) {
                lht = UnitsRelationsTools.removePrefix(elements, lht);
            }
            if (UnitsRelationsTools.getPrefixValue(rht) == Prefix.one) {
                rht = UnitsRelationsTools.removePrefix(elements, rht);
            }

            switch (kind) {
                case MINUS:
                    type.replaceAnnotation(enforcer.getArithmeticUnit(Op.SUB, lht, rht));
                    break;
                case PLUS:
                    type.replaceAnnotation(enforcer.getArithmeticUnit(Op.ADD, lht, rht));
                    break;
                case DIVIDE:
                    type.replaceAnnotation(enforcer.getArithmeticUnit(Op.DIV, lht, rht));
                    break;
                case MULTIPLY:
                    type.replaceAnnotation(enforcer.getArithmeticUnit(Op.MUL, lht, rht));
                    break;
                case REMAINDER:
                    // in modulo operation, it always returns the left unit
                    // regardless of what unit it is
                    type.replaceAnnotation(lht.getAnnotationInHierarchy(UNKNOWN));
                    break;
                case EQUAL_TO:
                case NOT_EQUAL_TO:
                case GREATER_THAN:
                case GREATER_THAN_EQUAL:
                case LESS_THAN:
                case LESS_THAN_EQUAL:
                    type.replaceAnnotation(enforcer.getComparableUnits(lht, rht, node));
                    break;
                default:
                    // Placeholders for unhandled binary operations
                    // For now, do nothing
                    break;
            }

            return null;
        }

        /** Type check and propagate units from compound assignment operations. */
        // This is called if a compound assignment is a part of another expression, eg x = y += z;
        @SuppressWarnings("fallthrough")
        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree node, AnnotatedTypeMirror type) {
            switch (node.getKind()) {
                case PLUS_ASSIGNMENT:
                case MINUS_ASSIGNMENT:
                case MULTIPLY_ASSIGNMENT:
                case DIVIDE_ASSIGNMENT:
                    AnnotationMirror resultUnit = enforcer.getCompoundAssignmentUnit(node);
                    if (resultUnit != null) {
                        type.replaceAnnotation(resultUnit);
                        return null;
                    }
                default:
                    return super.visitCompoundAssignment(node, type);
            }
        }

        /**
         * Type check and propagate units from tagged methods. Non-tagged methods are type checked
         * using super.
         */
        // TODO: this gets called 3 times per method invocation, and if there
        // are errors to report then there are many duplicate outputs
        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, AnnotatedTypeMirror type) {

            // get the receiver type, which is null for static methods
            AnnotatedTypeMirror receiver = getReceiverType(node);

            // get the list of method call arguments
            List<? extends ExpressionTree> methodArguments = node.getArguments();

            for (AnnotationMirror anno :
                    atypeFactory.getDeclAnnotations(TreeUtils.elementFromUse(node))) {

                if (AnnotationUtils.areSameByClass(anno, UnitsTypeCheckAsOp.class)) {
                    // extract op, lhsPos, and rhsPos from the annotation tagged on the method
                    CheckOp checkOp =
                            AnnotationUtils.getElementValueEnum(anno, "op", CheckOp.class, false);
                    int lhsPos =
                            Integer.valueOf(
                                    AnnotationUtils.getElementValue(
                                            anno, "lhsPos", String.class, true));
                    int rhsPos =
                            Integer.valueOf(
                                    AnnotationUtils.getElementValue(
                                            anno, "rhsPos", String.class, true));

                    // Check index bounds
                    int lhsLowBound = -1;
                    int rhsLowBound = -1;

                    if (receiver == null) {
                        lhsLowBound = 0;
                        rhsLowBound = 0;
                    }

                    if (lhsPos < lhsLowBound || lhsPos >= methodArguments.size()) {
                        ErrorReporter.errorAbort(
                                "Attempted to use an invalid position"
                                        + " index as an argument for custom unit type checking"
                                        + " of method "
                                        + node.toString()
                                        + ". Please update the"
                                        + " @UnitsTypeCheckAsOp lhsPos index value to an integer"
                                        + " between "
                                        + lhsLowBound
                                        + " and "
                                        + (methodArguments.size() - 1)
                                        + " inclusive.");
                    } else if (rhsPos < rhsLowBound || rhsPos >= methodArguments.size()) {
                        ErrorReporter.errorAbort(
                                "Attempted to use an invalid position"
                                        + " index as an argument for custom unit type checking"
                                        + " of method "
                                        + node.toString()
                                        + ". Please update the"
                                        + " @UnitsTypeCheckAsOp rhsPos index value to an integer"
                                        + " between "
                                        + lhsLowBound
                                        + " and "
                                        + (methodArguments.size() - 1)
                                        + " inclusive.");
                    }

                    // if Pos isn't the receiver, obtain the method argument
                    // type, otherwise use the receiver
                    AnnotatedTypeMirror lht =
                            lhsPos >= 0
                                    ? atypeFactory.getAnnotatedType(methodArguments.get(lhsPos))
                                    : receiver;
                    AnnotatedTypeMirror rht =
                            rhsPos >= 0
                                    ? atypeFactory.getAnnotatedType(methodArguments.get(rhsPos))
                                    : receiver;

                    // Type check the tagged methods using the enforcer
                    // TODO: merge with visitBinary in some way to share common logic
                    switch (checkOp) {
                        case COMPARE:
                            type.replaceAnnotation(enforcer.getComparableUnits(lht, rht, node));
                            return null;
                        case ADD:
                            type.replaceAnnotation(enforcer.getArithmeticUnit(Op.ADD, lht, rht));
                            return null;
                        case SUB:
                            type.replaceAnnotation(enforcer.getArithmeticUnit(Op.SUB, lht, rht));
                            return null;
                        case MUL:
                            type.replaceAnnotation(enforcer.getArithmeticUnit(Op.MUL, lht, rht));
                            return null;
                        case DIV:
                            type.replaceAnnotation(enforcer.getArithmeticUnit(Op.DIV, lht, rht));
                            return null;
                        case MOD:
                            // in modulo operation, it always returns the left unit
                            // regardless of what unit it is
                            type.replaceAnnotation(lht.getAnnotationInHierarchy(UNKNOWN));
                            return null;
                        default:
                            break;
                    }
                }
            }
            // Use super to type check any non-tagged methods
            return super.visitMethodInvocation(node, type);
        }
    }

    /**
     * Use custom qualifier hierarchy to programmatically set bottom of hierarchy and define subtype
     * and LUB relations for units.
     */
    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory mgf) {
        return new UnitsQualifierHierarchy(mgf);
    }

    /**
     * Units Qualifier Hierarchy programmatically defines {@link UnitsBottom} as the bottom of the
     * hierarchy, and defines the subtype and LUB relationships for prefixed units.
     */
    protected class UnitsQualifierHierarchy extends GraphQualifierHierarchy {

        public UnitsQualifierHierarchy(MultiGraphFactory mgf) {
            super(mgf, BOTTOM);
        }

        /** Checks to see if a1 is subtype of a2, accounting for unit prefixes */
        @Override
        public boolean isSubtype(AnnotationMirror a1, AnnotationMirror a2) {
            // If the prefix is Prefix.one, automatically strip it for LUB
            // checking
            if (UnitsRelationsTools.getPrefixValue(a1) == Prefix.one) {
                a1 = removePrefix(a1);
            }
            if (UnitsRelationsTools.getPrefixValue(a2) == Prefix.one) {
                a2 = removePrefix(a2);
            }

            // See if the two units have the same base unit
            if (AnnotationUtils.areSameIgnoringValues(a1, a2)) {
                // If so, return whether they are the exact same prefixed unit
                return AnnotationUtils.areSame(a1, a2);
            } else {
                // If not, check using super
                a1 = removePrefix(a1);
                a2 = removePrefix(a2);

                // super call can only check using annotation mirrors in the
                // supported type qualifier hierarchy, which must be
                // non-prefixed units
                return super.isSubtype(a1, a2);
            }
        }

        /**
         * Computes the LUB of two Units.
         *
         * <p>Alias annotations are not placed in the Supported Type Qualifiers set, instead, their
         * base units are in the set. Whenever an alias annotation or prefix-multiple of a base unit
         * is used we handle the LUB resolution here so that these units can correctly resolve to an
         * LUB Unit.
         */
        @Override
        public AnnotationMirror leastUpperBound(AnnotationMirror a1, AnnotationMirror a2) {
            // If the prefix is Prefix.one, automatically strip it for LUB
            // checking
            if (UnitsRelationsTools.getPrefixValue(a1) == Prefix.one) {
                a1 = removePrefix(a1);
            }
            if (UnitsRelationsTools.getPrefixValue(a2) == Prefix.one) {
                a2 = removePrefix(a2);
            }

            // Compute base units
            AnnotationMirror baseA1 = removePrefix(a1);
            AnnotationMirror baseA2 = removePrefix(a2);

            if (UnitsRelationsTools.isSameUnit(baseA1, baseA2)) {
                // If the two units have the same base unit
                if (UnitsRelationsTools.isSameUnit(a1, a2)) {
                    // And if they have the same Prefix, it means it is the same
                    // unit, so return the unit
                    return a1;
                } else {
                    // If they don't have the same Prefix, find the LUB:
                    // Check if a1 is a prefixed multiple of a base unit
                    boolean a1Prefixed = UnitsRelationsTools.hasPrefixValue(a1);
                    // Check if a2 is a prefixed multiple of a base unit
                    boolean a2Prefixed = UnitsRelationsTools.hasPrefixValue(a2);
                    // Obtain a1 and a2's direct supertypes
                    AnnotationMirror a1Super = getDirectSupertype(baseA1);
                    AnnotationMirror a2Super = getDirectSupertype(baseA2);

                    // findLub() only works with base units, so we use the
                    // direct supertype for any prefixed unit
                    if (a1Prefixed && a2Prefixed) {
                        // if both are prefixed, find the LUB of their direct
                        // supertypes
                        // eg LUB(@km, @km) == LUB(@Length, @Length) = @Length
                        return findLub(a1Super, a2Super);
                    } else if (a1Prefixed && !a2Prefixed) {
                        // if only the left is prefixed, find LUB of (supertype
                        // of a1) and a2
                        // eg LUB(@km, @m) == LUB(@Length, @m) = @Length
                        return findLub(a1Super, a2);
                    } else {
                        // else (only right is prefixed), find LUB of a1 and
                        // (supertype of a2)
                        // eg LUB(@m, @km) == LUB(@m, @Length) = @Length
                        return findLub(a1, a2Super);
                    }
                }
            } else {
                // if they don't have the same base unit, user super
                return super.leastUpperBound(a1, a2);
            }
        }

        /**
         * Obtains the direct supertype of a unit.
         *
         * <p>Climbs the supertypes graph to get the direct supertype of unit.
         *
         * @param unit An annotation mirror.
         * @return the supertype of unit as an annotation mirror.
         */
        private AnnotationMirror getDirectSupertype(AnnotationMirror unit) {
            for (AnnotationMirror key : supertypesGraph.keySet()) {
                if (AnnotationUtils.areSame(key, unit)) {
                    // each unit has exactly 1 super type
                    return supertypesGraph.get(key).iterator().next();
                }
            }
            return null;
        }
    }

    /**
     * Removes the prefix of any prefixed units and returns its base unit.
     *
     * @param anno An annotation mirror.
     * @return A copy of the annotation mirror with prefix removed.
     */
    private AnnotationMirror removePrefix(AnnotationMirror anno) {
        return UnitsRelationsTools.removePrefix(elements, anno);
    }
}
