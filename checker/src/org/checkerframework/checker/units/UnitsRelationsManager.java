package org.checkerframework.checker.units;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.Op;
import org.checkerframework.checker.units.qual.PolyUnit;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.checker.units.qual.Relation;
import org.checkerframework.checker.units.qual.UnitsBottom;
import org.checkerframework.checker.units.qual.UnknownUnits;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ErrorReporter;

/**
 * This class contains the logic for defining and managing the arithmetic relationships between
 * units for the Units Checker.
 *
 * <p>This class is a singleton class.
 */
public class UnitsRelationsManager {
    private static UnitsRelationsManager instance = null;

    private final BaseTypeChecker checker;
    private final AnnotatedTypeFactory atf;
    private final ProcessingEnvironment processingEnv;

    private static final Map<Op, UnitsRelationsMapping> relationMaps = new HashMap<>();

    private final AnnotationMirror UNKNOWN;
    private final AnnotationMirror DIMENSIONLESS;
    private final AnnotationMirror BOTTOM;
    private final AnnotationMirror POLYUNIT;
    private final AnnotationMirror INSTANT;
    private final AnnotationMirror DURATION;

    private static final String newline = System.getProperty("line.separator");

    protected static UnitsRelationsManager getInstance(
            BaseTypeChecker checker, AnnotatedTypeFactory atf) {
        if (instance == null) {
            instance = new UnitsRelationsManager(checker, atf);
        }
        return instance;
    }

    /**
     * Instantiates the UnitsRelationsManager, and defines the relationships between {@link
     * UnknownUnits}, {@link Dimensionless}, {@link UnitsBottom}, and {@link PolyUnit}. The latter
     * is used in type checking the body of polymorphic methods.
     */
    private UnitsRelationsManager(BaseTypeChecker checker, AnnotatedTypeFactory atf) {
        this.checker = checker;
        this.atf = atf;
        processingEnv = checker.getProcessingEnvironment();

        UnitsMirrors mirrors = UnitsMirrors.getInstance(checker);
        UNKNOWN = mirrors.UNKNOWN;
        DIMENSIONLESS = mirrors.DIMENSIONLESS;
        BOTTOM = mirrors.BOTTOM;
        POLYUNIT = mirrors.POLYUNIT;
        INSTANT = mirrors.INSTANT;
        DURATION = mirrors.DURATION;

        // create maps for all ops
        for (Op op : Op.values()) {
            relationMaps.put(op, new UnitsRelationsMapping());
        }

        // create relations for UnknownUnits, Dimensionless, and Bottom, which must be the first relations in the
        // sets
        for (Op op : Op.values()) {
            addDirectRelation(op, UNKNOWN, UNKNOWN, UNKNOWN);
            addDirectRelation(op, DIMENSIONLESS, DIMENSIONLESS, DIMENSIONLESS);
            addDirectRelation(op, BOTTOM, BOTTOM, UNKNOWN);

            // any other interactions between the units result in UnknownUnits
            addDirectRelation(op, DIMENSIONLESS, UNKNOWN, UNKNOWN);
            addDirectRelation(op, UNKNOWN, DIMENSIONLESS, UNKNOWN);
            addDirectRelation(op, DIMENSIONLESS, BOTTOM, UNKNOWN);
            addDirectRelation(op, BOTTOM, DIMENSIONLESS, UNKNOWN);
            addDirectRelation(op, BOTTOM, UNKNOWN, UNKNOWN);
            addDirectRelation(op, UNKNOWN, BOTTOM, UNKNOWN);
        }

        // create relations for PolyUnit, these relations are used to type check the body of PolyUnit methods
        addDirectRelation(Op.ADD, POLYUNIT, DIMENSIONLESS, UNKNOWN);
        addDirectRelation(Op.ADD, DIMENSIONLESS, POLYUNIT, UNKNOWN);
        addDirectRelation(Op.SUB, POLYUNIT, DIMENSIONLESS, UNKNOWN);
        addDirectRelation(Op.SUB, DIMENSIONLESS, POLYUNIT, UNKNOWN);
        addDirectRelation(Op.MUL, POLYUNIT, POLYUNIT, UNKNOWN);
        addDirectRelation(Op.DIV, DIMENSIONLESS, POLYUNIT, UNKNOWN);
        addStandardRelations(POLYUNIT);
    }

    /**
     * Adds all standard relationships for a given unit w.r.t. itself, {@link UnknownUnits}, {@link
     * Dimensionless}, and {@link UnitsBottom}.
     *
     * @param unit An annotation mirror representing a unit.
     */
    protected void addStandardRelations(AnnotationMirror unit) {
        boolean isTimeInstant = isTimeInstant(unit);
        boolean isCategoryUnit =
                UnitsRelationsTools.isSameUnit(
                        UnitsRelationsTools.getDirectSupertype(processingEnv, unit), UNKNOWN);

        if (isTimeInstant) {
            // I + I = UU
            addDirectRelation(Op.ADD, unit, unit, UNKNOWN);
        } else {
            // X + X = X
            addDirectRelation(Op.ADD, unit, unit, unit);
        }
        // X + UU = UU
        addDirectRelation(Op.ADD, unit, UNKNOWN, UNKNOWN);
        // UU + X = UU
        addDirectRelation(Op.ADD, UNKNOWN, unit, UNKNOWN);
        // X + BOT = UU
        addDirectRelation(Op.ADD, unit, BOTTOM, UNKNOWN);
        // BOT + X = UU
        addDirectRelation(Op.ADD, BOTTOM, unit, UNKNOWN);
        // X + Dimensionless = LUB(X, Dimensionless)  // handled in completerelationMaps()
        // Dimensionless + X = LUB(Dimensionless, X)

        if (!isTimeInstant) {
            // X - X = X
            addDirectRelation(Op.SUB, unit, unit, unit);
        }
        // X - UU = UU
        addDirectRelation(Op.SUB, unit, UNKNOWN, UNKNOWN);
        // UU - X = UU
        addDirectRelation(Op.SUB, UNKNOWN, unit, UNKNOWN);
        // X - BOT = UU
        addDirectRelation(Op.SUB, unit, BOTTOM, UNKNOWN);
        // BOT - X = UU
        addDirectRelation(Op.SUB, BOTTOM, unit, UNKNOWN);
        // X - Dimensionless = LUB(X, Dimensionless)  // handled in completerelationMaps()
        // Dimensionless - X = LUB(Dimensionless, X)

        // X * X = (external rule, default UU)  // UU handled in completerelationMaps()
        // X * UU = UU
        addDirectRelation(Op.MUL, unit, UNKNOWN, UNKNOWN);
        // UU * X = UU
        addDirectRelation(Op.MUL, UNKNOWN, unit, UNKNOWN);
        // X * BOT = UU
        addDirectRelation(Op.MUL, unit, BOTTOM, UNKNOWN);
        // BOT * X = UU
        addDirectRelation(Op.MUL, BOTTOM, unit, UNKNOWN);
        // X * Dimensionless = X
        addDirectRelation(Op.MUL, unit, DIMENSIONLESS, unit);
        // Dimensionless * X = X
        addDirectRelation(Op.MUL, DIMENSIONLESS, unit, unit);

        if (isCategoryUnit && unit != POLYUNIT) {
            // CAT / CAT = UNKNOWN
            addDirectRelation(Op.DIV, unit, unit, UNKNOWN);
        } else {
            // X / X = Dimensionless
            addDirectRelation(Op.DIV, unit, unit, DIMENSIONLESS);
        }
        // X / UU = UU
        addDirectRelation(Op.DIV, unit, UNKNOWN, UNKNOWN);
        // UU / X = UU
        addDirectRelation(Op.DIV, UNKNOWN, unit, UNKNOWN);
        // X / BOT = UU
        addDirectRelation(Op.DIV, unit, BOTTOM, UNKNOWN);
        // BOT / X = UU
        addDirectRelation(Op.DIV, BOTTOM, unit, UNKNOWN);
        // X / Dimensionless = X
        addDirectRelation(Op.DIV, unit, DIMENSIONLESS, unit);
        // Dimensionless / X = (external rule, default UU)  // UU handled in completerelationMaps()
    }

    /**
     * Adds the relation lhs op rhs = res, and it's commutative equivalents to the map of relations.
     *
     * <p>This method is used for adding explicitly declared relationships from {@link Relation}.
     *
     * @param op An arithmetic operation.
     * @param lhs Annotation class of the left hand argument.
     * @param rhs Annotation class of the right hand argument.
     * @param res Annotation class of the result of the operation.
     */
    protected void addRelation(
            Op op,
            Class<? extends Annotation> lhs,
            Class<? extends Annotation> rhs,
            Class<? extends Annotation> res) {
        AnnotationMirror lhsMirror = buildAnnoMirror(lhs);
        AnnotationMirror rhsMirror = buildAnnoMirror(rhs);
        AnnotationMirror resMirror = buildAnnoMirror(res);
        addRelation(op, lhsMirror, rhsMirror, resMirror);
    }

    /**
     * Adds the relation lhs op rhs = res, and it's commutative equivalents to the map of relations.
     *
     * <p>This method is used for adding explicitly declared relationships from {@link Relation}.
     *
     * @param op An arithmetic operation.
     * @param lhs Annotation mirror of the left hand argument.
     * @param rhs Annotation mirror of the right hand argument.
     * @param res Annotation mirror of the result of the operation.
     */
    protected void addRelation(
            Op op, AnnotationMirror lhs, AnnotationMirror rhs, AnnotationMirror res) {
        // add the stated relationship relation
        addDirectRelation(op, lhs, rhs, res);

        // add commutative and inverse relationships
        if (op == Op.ADD) {
            // lhs + rhs = res ==> rhs + lhs = res
            addDirectRelation(Op.ADD, rhs, lhs, res);
            // lhs + rhs = res ==> res - lhs = rhs
            addDirectRelation(Op.SUB, res, lhs, rhs);
            // lhs + rhs = res ==> res - rhs = lhs
            addDirectRelation(Op.SUB, res, rhs, lhs);
        } else if (op == Op.SUB) {
            // lhs - rhs = res ==> lhs - res = rhs
            addDirectRelation(Op.SUB, lhs, res, rhs);
            // lhs - rhs = res ==> rhs + res = lhs
            addDirectRelation(Op.ADD, rhs, res, lhs);
            // lhs - rhs = res ==> res + rhs = lhs
            addDirectRelation(Op.ADD, res, rhs, lhs);
        } else if (op == Op.MUL) {
            // lhs * rhs = res ==> rhs * lhs = res
            addDirectRelation(Op.MUL, rhs, lhs, res);
            // lhs * rhs = res ==> res / lhs = rhs
            addDirectRelation(Op.DIV, res, lhs, rhs);
            // lhs * rhs = res ==> res / rhs = lhs
            addDirectRelation(Op.DIV, res, rhs, lhs);
        } else if (op == Op.DIV) {
            // lhs / rhs = res ==> lhs / res = rhs
            addDirectRelation(Op.DIV, lhs, res, rhs);
            // lhs / rhs = res ==> rhs * res = lhs
            addDirectRelation(Op.MUL, rhs, res, lhs);
            // lhs / rhs = res ==> res * rhs = lhs
            addDirectRelation(Op.MUL, res, rhs, lhs);
        }
    }

    /**
     * Directly adds the relation lhs op rhs = res the map of relations. Once added, lhs op rhs
     * cannot be set to another res. Duplicate relations are ignored.
     *
     * @param op An arithmetic operation.
     * @param lhs Annotation mirror of the left hand argument.
     * @param rhs Annotation mirror of the right hand argument.
     * @param res Annotation mirror of the result of the operation.
     */
    protected void addDirectRelation(
            Op op, AnnotationMirror lhs, AnnotationMirror rhs, AnnotationMirror res) {

        UnitsRelationsMapping relations = relationMaps.get(op);

        // make sure (lhs, rhs) isn't already mapped to some res
        if (relations.containsKeys(lhs, rhs)) {
            AnnotationMirror current = relations.get(lhs, rhs);
            if (!AnnotationUtils.areSame(res, current)) {
                // if we are attempting to replace a relation, produce error message
                ErrorReporter.errorAbort(
                        "Conflicting arithmetic relation: attempted to replace (op = "
                                + op
                                + ", lhs = "
                                + lhs
                                + ", rhs = "
                                + rhs
                                + ", res = "
                                + current
                                + ") with res = "
                                + res
                                + " please check @Relation definitions in annotations for conflicts.");
            } else {
                // if we are attempting to add a duplicate relation, do nothing
                return;
            }
        }

        relations.put(lhs, rhs, res);
    }

    /**
     * Builds and obtains the annotation mirror representing class unit. Alias annotations are
     * normalized to their base units.
     *
     * @param unit Annotation class of a unit.
     * @return An annotation mirror representing the unit.
     */
    protected AnnotationMirror buildAnnoMirror(Class<? extends Annotation> unit) {
        AnnotationMirror mirror = UnitsAliasManager.getInstance(checker).getAliasAnnotation(unit);
        // Build without prefix, because all Prefix.one's are stripped from non-alias annotations
        if (mirror == null)
            mirror = UnitsRelationsTools.buildAnnoMirrorWithNoPrefix(processingEnv, unit);
        return mirror;
    }

    /**
     * Obtains the result unit of the arithmetic operation lhs op rhs.
     *
     * @param op An arithmetic operation.
     * @param lhs Annotation mirror of the left hand argument.
     * @param rhs Annotation mirror of the right hand argument.
     * @return Annotation mirror of the result of the operation.
     */
    protected AnnotationMirror getResultUnit(Op op, AnnotationMirror lhs, AnnotationMirror rhs) {
        return relationMaps.get(op).get(lhs, rhs);
    }

    /**
     * Adds relationships for every pair of units loaded by units checker, including prefixed
     * multiples of base units. The rules within computes the default result unit for any given
     * arithmetic operation. This is called after all user declared units relationships are added to
     * the relation maps.
     */
    protected void completeRelationMaps() {
        for (Op op : relationMaps.keySet()) {
            // TODO: more assertions where possible within (check the unit
            // relations vs category relations, and check the size of the
            // relation maps)

            UnitsRelationsMapping relations = relationMaps.get(op).deepCopy();

            Set<AnnotationMirror> originalUnits = relations.xKeySet();
            Set<AnnotationMirror> baseUnits = getBaseUnits(originalUnits);
            Set<AnnotationMirror> originalAliasUnits = getAliasUnits(originalUnits);
            Set<AnnotationMirror> remainingUnits = getRemainingUnits(originalUnits);

            assert baseUnits.size() + originalAliasUnits.size() + remainingUnits.size()
                    == originalUnits.size();

            // Generate all prefixed units of each base unit
            Set<AnnotationMirror> allPrefixedUnits = new HashSet<AnnotationMirror>();
            for (AnnotationMirror baseUnit : baseUnits) {
                allPrefixedUnits.addAll(getAllPrefixedUnits(baseUnit));
            }

            // Add standard relations for the prefixed units
            for (AnnotationMirror unit : allPrefixedUnits) {
                addStandardRelations(unit);
            }

            Set<AnnotationMirror> allUnits = new HashSet<AnnotationMirror>();
            allUnits.addAll(baseUnits);
            allUnits.addAll(allPrefixedUnits);
            allUnits.addAll(remainingUnits);

            QualifierHierarchy hierarchy = atf.getQualifierHierarchy();

            for (AnnotationMirror u1 : allUnits) {
                for (AnnotationMirror u2 : allUnits) {
                    if (!relationMaps.get(op).containsKeys(u1, u2)) {
                        boolean u1IsTimeInstant = isTimeInstant(u1);
                        boolean u2IsTimeInstant = isTimeInstant(u2);
                        boolean u1IsTimeDuration = isTimeDuration(u1);
                        boolean u2IsTimeDuration = isTimeDuration(u2);

                        if (op == Op.ADD) {
                            if (u1IsTimeInstant && u2IsTimeInstant) {
                                addDirectRelation(op, u1, u2, UNKNOWN);
                            } else if ((u1IsTimeInstant && u2IsTimeDuration)
                                    || (u1IsTimeDuration && u2IsTimeInstant)) {
                                addDirectRelation(op, u1, u2, INSTANT);
                            } else {
                                addDirectRelation(op, u1, u2, hierarchy.leastUpperBound(u1, u2));
                            }
                        } else if (op == Op.SUB) {
                            if (u1IsTimeInstant && u2IsTimeInstant) {
                                addDirectRelation(op, u1, u2, DURATION);
                            } else if (u1IsTimeInstant && u2IsTimeDuration) {
                                addDirectRelation(op, u1, u2, INSTANT);
                            } else if (u1IsTimeDuration && u2IsTimeInstant) {
                                addDirectRelation(op, u1, u2, UNKNOWN);
                            } else {
                                addDirectRelation(op, u1, u2, hierarchy.leastUpperBound(u1, u2));
                            }
                        } else if (op == Op.MUL || op == Op.DIV) {
                            AnnotationMirror u1Super;
                            AnnotationMirror u2Super;

                            if (UnitsRelationsTools.isSameUnit(u1, POLYUNIT)) {
                                // The relations for PolyUnit op X is the same
                                // as UnknownUnits op X for any X unit.
                                u1Super = UNKNOWN;
                            } else {
                                u1Super = UnitsRelationsTools.getDirectSupertype(processingEnv, u1);
                            }

                            if (UnitsRelationsTools.isSameUnit(u2, POLYUNIT)) {
                                // The relations for X op PolyUnit is the same
                                // as X op UnknownUnits for any X unit.
                                u2Super = UNKNOWN;
                            } else {
                                u2Super = UnitsRelationsTools.getDirectSupertype(processingEnv, u2);
                            }

                            boolean u1IsCategoryUnit =
                                    UnitsRelationsTools.isSameUnit(u1Super, UNKNOWN);
                            boolean u2IsCategoryUnit =
                                    UnitsRelationsTools.isSameUnit(u2Super, UNKNOWN);

                            if (u1IsCategoryUnit
                                    && !u2IsCategoryUnit
                                    && relations.containsKeys(u1, u2Super)) {
                                // assuming dimensionless / hour isn't mapped, then
                                // dimensionless / hour == dimensionless / duration = frequency
                                addDirectRelation(op, u1, u2, relations.get(u1, u2Super));
                            } else if (!u1IsCategoryUnit
                                    && u2IsCategoryUnit
                                    && relations.containsKeys(u1Super, u2)) {
                                // MHz * duration == frequency * duration == dimensionless
                                addDirectRelation(op, u1, u2, relations.get(u1Super, u2));
                            } else if (relations.containsKeys(u1Super, u2Super)) {
                                // km / s == length / duration = speed
                                // mm * km == length * length = area
                                addDirectRelation(op, u1, u2, relations.get(u1Super, u2Super));
                            } else {
                                addDirectRelation(op, u1, u2, UNKNOWN);
                            }
                        }
                    }
                }
            }

            // Ensure there's N^2 relations for the op.
            assert relationMaps.get(op).size() == Math.pow(allUnits.size(), 2);
        }
    }

    /**
     * Checks to see if a unit is a time instant unit.
     *
     * @param unit An annotation mirror representing a unit.
     * @return True if the unit is a time instant unit.
     */
    private boolean isTimeInstant(AnnotationMirror unit) {
        return UnitsRelationsTools.isSameUnit(unit, INSTANT)
                || UnitsRelationsTools.isSameUnit(
                        UnitsRelationsTools.getDirectSupertype(processingEnv, unit), INSTANT);
    }

    /**
     * Checks to see if a unit is a time duration unit.
     *
     * @param unit An annotation mirror representing a unit.
     * @return True if the unit is a time duration unit.
     */
    private boolean isTimeDuration(AnnotationMirror unit) {
        return UnitsRelationsTools.isSameUnit(unit, DURATION)
                || UnitsRelationsTools.isSameUnit(
                        UnitsRelationsTools.getDirectSupertype(processingEnv, unit), DURATION);
    }

    /**
     * Obtains the sub-set of base units from the input set and returns it.
     *
     * @param allUnits A set of annotation mirrors, each representing a unit.
     * @return A set of annotation mirrors of only base units that appear in the input set.
     */
    private Set<AnnotationMirror> getBaseUnits(Set<AnnotationMirror> allUnits) {
        Set<AnnotationMirror> units = new HashSet<AnnotationMirror>();
        for (AnnotationMirror unit : allUnits) {
            if (UnitsRelationsTools.isBaseUnit(unit)) {
                units.add(unit);
            }
        }
        return units;
    }

    /**
     * Obtains the sub-set of alias units from the input set and returns it.
     *
     * @param allUnits A set of annotation mirrors, each representing a unit.
     * @return A set of annotation mirrors of only alias units that appear in the input set.
     */
    private Set<AnnotationMirror> getAliasUnits(Set<AnnotationMirror> allUnits) {
        Set<AnnotationMirror> units = new HashSet<AnnotationMirror>();
        for (AnnotationMirror unit : allUnits) {
            if (UnitsRelationsTools.isAliasUnit(unit)) {
                units.add(unit);
            }
        }
        return units;
    }

    /**
     * Obtains the sub-set of units from the input set that are not base units or alias units, and
     * returns it.
     *
     * @param allUnits A set of annotation mirrors, each representing a unit.
     * @return A set of annotation mirrors of units that appear in the input set that are not base
     *     units or alias units.
     */
    private Set<AnnotationMirror> getRemainingUnits(Set<AnnotationMirror> allUnits) {
        Set<AnnotationMirror> units = new HashSet<AnnotationMirror>();
        for (AnnotationMirror unit : allUnits) {
            if (!UnitsRelationsTools.isBaseUnit(unit) && !UnitsRelationsTools.isAliasUnit(unit)) {
                units.add(unit);
            }
        }
        return units;
    }

    /**
     * Generates every possible prefixed-multiple of the input base unit and returns them as a set.
     *
     * @param baseUnit An annotation mirror representing a base unit.
     * @return All possible prefixed-multiple of the base unit as a set.
     */
    private Set<AnnotationMirror> getAllPrefixedUnits(AnnotationMirror baseUnit) {
        Set<AnnotationMirror> prefixedUnits = new HashSet<AnnotationMirror>();
        for (Prefix p : Prefix.values()) {
            if (p != Prefix.one) {
                AnnotationBuilder builder = new AnnotationBuilder(processingEnv, baseUnit);
                builder.setValue("value", p);
                AnnotationMirror prefixedUnit = builder.build();
                prefixedUnits.add(prefixedUnit);
            }
        }

        assert prefixedUnits.size() == Prefix.values().length - 1;

        return prefixedUnits;
    }

    // Methods to support constant_constant, constant_variable,
    // variable_constant, and variable_variable encoding in inference.
    protected AnnotationMirror getCCAnno(Op op, AnnotationMirror c1, AnnotationMirror c2) {
        return relationMaps.get(op).get(c1, c2);
    }

    protected Map<AnnotationMirror, AnnotationMirror> getCVMap(Op op, AnnotationMirror constant) {
        return relationMaps.get(op).getAllY(constant);
    }

    protected Map<AnnotationMirror, AnnotationMirror> getVCMap(Op op, AnnotationMirror constant) {
        return relationMaps.get(op).getAllX(constant);
    }

    protected Map<AnnotationMirror, Map<AnnotationMirror, AnnotationMirror>> getVVMap(Op op) {
        return relationMaps.get(op).getAllXY();
    }

    /** Displays all arithmetic relationships to the screen. */
    protected void debugPrint() {
        checker.message(Diagnostic.Kind.NOTE, "Arithmetic Relations:");
        printOp(Op.ADD);
        printOp(Op.SUB);
        printOp(Op.MUL);
        printOp(Op.DIV);
    }

    /**
     * Displays the arithmetic relationships for the given operation to the screen.
     *
     * @param op An arithmetic operation.
     */
    private void printOp(Op op) {
        UnitsRelationsMapping relations = relationMaps.get(op);
        for (AnnotationMirror x : sortUnits(relations.xKeySet())) {
            for (AnnotationMirror y : sortUnits(relations.yKeySet(x))) {
                AnnotationMirror val = relations.get(x, y);
                checker.message(
                        Diagnostic.Kind.NOTE,
                        "  "
                                + shortName(x)
                                + " "
                                + op
                                + " "
                                + shortName(y)
                                + " = "
                                + shortName(val));
            }
        }
    }

    /**
     * Writes all arithmetic relationships into a set of CSV files at the given filePath, with an
     * option to print or hide UnknownUnits in the CSV file.
     *
     * @param filePath A fully qualified path from root of file system to a desired folder where the
     *     CSV files will be written.
     * @param printUU Set to true to print {@literal @UnknownUnits} in the cells of the CSV file,
     *     false otherwise.
     */
    protected void writeCSV(final String filePath, boolean printUU) {
        writeOpToFile(Op.ADD, filePath, printUU);
        writeOpToFile(Op.SUB, filePath, printUU);
        writeOpToFile(Op.MUL, filePath, printUU);
        writeOpToFile(Op.DIV, filePath, printUU);
    }

    /**
     * Writes all arithmetic relationships into a set of CSV files at the given filePath, with an
     * option to print or hide UnknownUnits in the CSV file.
     *
     * @param op An arithmetic operation.
     * @param filePath A fully qualified path from root of file system to a desired folder where the
     *     CSV files will be written.
     * @param printUU Set to true to print {@literal @UnknownUnits} in the cells of the CSV file,
     *     false otherwise.
     */
    private void writeOpToFile(Op op, final String filePath, boolean printUU) {
        final String filename = filePath + "/UnitsRelationTables-" + op.toString() + ".csv";
        BufferedWriter br;
        try {
            br = new BufferedWriter(new FileWriter(filename));
            StringBuilder sb = new StringBuilder();

            UnitsRelationsMapping relations = relationMaps.get(op);

            sb.append("Op = ");
            sb.append(op.toString());

            // Write header row, which prints out the RHS of OP
            for (AnnotationMirror rhs : sortUnits(relations.xKeySet())) {
                sb.append(",");
                sb.append(fileWritingShortName(rhs, printUU, true));
            }
            sb.append(newline);

            // Write body rows
            for (AnnotationMirror lhs : sortUnits(relations.xKeySet())) {
                // left column is LHS of OP
                sb.append(fileWritingShortName(lhs, printUU, true));
                sb.append(",");

                // Write cell value
                for (AnnotationMirror rhs :
                        sortUnits(relations.yKeySet(lhs))) { // should line up...
                    AnnotationMirror val = relations.get(lhs, rhs);
                    sb.append(fileWritingShortName(val, printUU, false));
                    sb.append(",");
                }

                sb.append(newline);
            }

            br.write(sb.toString());
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Computes and returns the short name of a unit used for writing to a CSV file.
     *
     * @param unit An annotation mirror representing a unit.
     * @param printUU Set to true to print {@literal @UnknownUnits} in the cells of a CSV file,
     *     false otherwise.
     * @param isHeader Boolean flag of whether it is a header row.
     * @return The short name of a unit for file writing.
     */
    private String fileWritingShortName(AnnotationMirror unit, boolean printUU, boolean isHeader) {
        String name = shortName(unit);
        if (!isHeader)
            name =
                    printUU
                            ? name.replaceAll("UnknownUnits", "UU")
                            : name.replaceAll("@UnknownUnits", "");
        name = name.replaceAll("TimeInstant", "Instant");
        name = name.replaceAll("TimeDuration", "Duration");
        return name;
    }

    /**
     * Computes and returns the short name of a unit.
     *
     * @param unit An annotation mirror representing a unit.
     * @return The short name of a unit.
     */
    private String shortName(AnnotationMirror unit) {
        // obtain the fully qualified name of the unit, including prefixes
        String name = unit.toString();
        // get rid of any "org.checkerframework.checker.units.qual"
        name = name.replaceAll("org.checkerframework.checker.units.qual.", "");
        // get rid of "time.duration"
        name = name.replaceAll("time.duration.", "");
        // get rid of "time.instant"
        name = name.replaceAll("time.instant.", "");
        // get rid of "time."
        name = name.replaceAll("time.", "");
        // get rid of "qual."
        name = name.replaceAll("qual.", "");
        return name;
    }

    /**
     * Alphabetically sorts the units given in the input set by their fully qualified names and
     * returns the sorted units as a list.
     *
     * @param set A set of annotation mirrors, each representing a unit.
     * @return A sorted list of the annotation mirrors.
     */
    private List<AnnotationMirror> sortUnits(Set<AnnotationMirror> set) {
        // Copy set into list
        List<AnnotationMirror> sortedList = new ArrayList<AnnotationMirror>(set);

        // Sort the list
        Collections.sort(
                sortedList,
                new Comparator<AnnotationMirror>() {
                    @Override
                    public int compare(AnnotationMirror a1, AnnotationMirror a2) {
                        if (a1 != null && a2 != null) {
                            // compare by annotation name
                            String a1Name = AnnotationUtils.annotationName(a1);
                            String a2Name = AnnotationUtils.annotationName(a2);
                            if (a1Name != a2Name) {
                                return a1Name.compareTo(a2Name);
                            } else {
                                // if names are the same, compare by element values
                                Map<? extends ExecutableElement, ? extends AnnotationValue> elval1 =
                                        AnnotationUtils.getElementValuesWithDefaults(a1);
                                Map<? extends ExecutableElement, ? extends AnnotationValue> elval2 =
                                        AnnotationUtils.getElementValuesWithDefaults(a2);

                                return elval1.toString().compareTo(elval2.toString());
                            }
                        } else if (a2 == null) {
                            return 1; // 1 > 2
                        } else if (a1 == null) {
                            return -1; // 2 < 1
                        } else {
                            return 0;
                        }
                    }
                });

        return sortedList;
    }
}
