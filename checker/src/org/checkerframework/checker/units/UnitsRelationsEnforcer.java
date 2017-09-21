package org.checkerframework.checker.units;

import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.Op;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.ErrorReporter;

/**
 * This class contains the logic for type checking all arithmetic and comparison operations for the
 * Units Checker.
 *
 * <p>This class is a singleton class.
 */
public class UnitsRelationsEnforcer {
    private static UnitsRelationsEnforcer instance = null;

    private final UnitsRelationsManager relations;
    private final BaseTypeChecker checker;
    private final AnnotatedTypeFactory atf;

    private final AnnotationMirror UNKNOWN;
    private final AnnotationMirror DIMENSIONLESS;
    private final AnnotationMirror BOTTOM;

    // TODO: change class name?
    protected static UnitsRelationsEnforcer getInstance(
            BaseTypeChecker checker, AnnotatedTypeFactory atf) {
        if (instance == null) {
            instance = new UnitsRelationsEnforcer(checker, atf);
        }
        return instance;
    }

    private UnitsRelationsEnforcer(BaseTypeChecker checker, AnnotatedTypeFactory atf) {
        this.checker = checker;
        this.atf = atf;

        UnitsMirrors mirrors = UnitsMirrors.getInstance(checker);
        UNKNOWN = mirrors.UNKNOWN;
        DIMENSIONLESS = mirrors.DIMENSIONLESS;
        BOTTOM = mirrors.BOTTOM;

        relations = UnitsRelationsManager.getInstance(checker, atf);
    }

    /**
     * Obtains the resulting unit for an arithmetic operation between lht and rht.
     *
     * @param op An arithmetic operation.
     * @param lht Left hand annotated type mirror of the operation.
     * @param rht Right hand annotated type mirror of the operation.
     * @return The resulting unit as an annotation mirror, or error if no relationships exist
     *     between the units for the given operation.
     */
    public AnnotationMirror getArithmeticUnit(
            Op op, AnnotatedTypeMirror lht, AnnotatedTypeMirror rht) {
        return getArithmeticUnit(op, getUnit(lht), getUnit(rht));
    }

    /**
     * Obtains the resulting unit for an arithmetic operation between lht and rht.
     *
     * @param op An arithmetic operation.
     * @param lht Left hand annotation mirror of the operation.
     * @param rht Right hand annotation mirror of the operation.
     * @return The resulting unit as an annotation mirror, or an exception if no relationships exist
     *     between the units for the given operation.
     */
    public AnnotationMirror getArithmeticUnit(Op op, AnnotationMirror lht, AnnotationMirror rht) {
        AnnotationMirror result = relations.getResultUnit(op, lht, rht);

        // If there's no direct mapping of the relation, then error
        if (result == null) {
            ErrorReporter.errorAbort(
                    "no arithmetic relationship for " + lht + " " + op + " " + rht);
        }
        return result;
    }

    /**
     * Checks to ensure that lht and rht are comparable units. If not an error is generated. This
     * always return {@link Dimensionless} as the result.
     *
     * @param lht Left hand annotated type mirror of the comparison.
     * @param rht Right hand annotated type mirror of the comparison.
     * @param node Tree node of the comparison operation.
     * @return {@link Dimensionless} as the result unit of the comparison.
     */
    public AnnotationMirror getComparableUnits(
            AnnotatedTypeMirror lht, AnnotatedTypeMirror rht, Tree node) {
        if (!isComparableUnits(lht, rht)) {
            checker.report(
                    Result.failure("comparison.unit.mismatch", lht.toString(), rht.toString()),
                    node);
        }
        return DIMENSIONLESS;
    }

    /**
     * Determines whether two units are comparable.
     *
     * @param lht Left hand annotated type mirror of the comparison.
     * @param rht Right hand annotated type mirror of the comparison.
     * @return True if they are comparable, false otherwise.
     */
    private boolean isComparableUnits(AnnotatedTypeMirror lht, AnnotatedTypeMirror rht) {
        // if the units are the same, or either are dimensionless (for magnitude
        // comparison), or either are bottom (for null reference comparison)
        // then set the resulting boolean or integer to dimensionless
        // Note: a boolean result is most common, eg x >= y, or x.equals(y)
        // an int result is also possible for Integer.compare(x, y)
        return (UnitsRelationsTools.isSameUnit(getUnit(lht), getUnit(rht))
                || UnitsRelationsTools.hasSpecificUnit(lht, DIMENSIONLESS)
                || UnitsRelationsTools.hasSpecificUnit(rht, DIMENSIONLESS)
                || UnitsRelationsTools.hasSpecificUnit(lht, BOTTOM)
                || UnitsRelationsTools.hasSpecificUnit(rht, BOTTOM));
    }

    /**
     * Returns the unit of an annotated type mirror.
     *
     * @param atm An annotated type mirror.
     * @return The unit as an annotation mirror.
     */
    protected AnnotationMirror getUnit(AnnotatedTypeMirror atm) {
        return atm.getEffectiveAnnotationInHierarchy(UNKNOWN);
    }

    /**
     * Obtains the resulting unit for a compound assignment of var op expr.
     *
     * @param node The {@link CompoundAssignmentTree} node of the compound assignment operation.
     * @return The unit as an annotation mirror.
     */
    public AnnotationMirror getCompoundAssignmentUnit(CompoundAssignmentTree node) {
        ExpressionTree var = node.getVariable();
        ExpressionTree expr = node.getExpression();
        AnnotatedTypeMirror varType = atf.getAnnotatedType(var);
        AnnotatedTypeMirror exprType = atf.getAnnotatedType(expr);
        AnnotationMirror varUnit = getUnit(varType);
        switch (node.getKind()) {
            case PLUS_ASSIGNMENT:
                checkCompoundAddSubAssignmentUnit(Op.ADD, varType, varUnit, exprType, node);
                break;
            case MINUS_ASSIGNMENT:
                checkCompoundAddSubAssignmentUnit(Op.SUB, varType, varUnit, exprType, node);
                break;
            case MULTIPLY_ASSIGNMENT:
                checkCompoundMulDivAssignmentUnit(Op.MUL, varType, varUnit, exprType, node);
                break;
            case DIVIDE_ASSIGNMENT:
                checkCompoundMulDivAssignmentUnit(Op.DIV, varType, varUnit, exprType, node);
                break;
            default:
                break;
        }
        // The result unit is always the unit of the left hand variable.
        return varUnit;
    }

    private void checkCompoundAddSubAssignmentUnit(
            Op op,
            AnnotatedTypeMirror varType,
            AnnotationMirror varUnit,
            AnnotatedTypeMirror exprType,
            CompoundAssignmentTree node) {
        AnnotationMirror result = getArithmeticUnit(op, varType, exprType);
        if (!UnitsRelationsTools.isSameUnit(varUnit, result)) {
            checker.report(
                    Result.failure("compound.assignment.type.incompatible", exprType, varType),
                    node);
        }
    }

    private void checkCompoundMulDivAssignmentUnit(
            Op op,
            AnnotatedTypeMirror varType,
            AnnotationMirror varUnit,
            AnnotatedTypeMirror exprType,
            CompoundAssignmentTree node) {
        AnnotationMirror result = getArithmeticUnit(op, varType, exprType);
        if (!UnitsRelationsTools.isSameUnit(varUnit, result)) {
            AnnotatedTypeMirror resultType = exprType.deepCopy();
            resultType.replaceAnnotation(result);
            checker.report(
                    Result.failure("compound.assignment.type.incompatible", resultType, varType),
                    node);
        }
    }
}
