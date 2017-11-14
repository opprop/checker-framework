package org.checkerframework.checker.units;

import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.units.qual.Dimensionless;
import org.checkerframework.checker.units.qual.Op;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.ErrorReporter;

/**
 * Helper class responsible for type checking and computing result units for all arithmetic and
 * comparison operations for the Units Checker.
 */
public class UnitsRelationsEnforcer {
    private final BaseTypeChecker checker;
    private final UnitsAnnotatedTypeFactory atf;
    private final UnitsRelationsManager relationsManager;

    private final AnnotationMirror UNKNOWN;
    private final AnnotationMirror DIMENSIONLESS;
    private final AnnotationMirror BOTTOM;

    public UnitsRelationsEnforcer(BaseTypeChecker checker, UnitsAnnotatedTypeFactory atf) {
        this.checker = checker;
        this.atf = atf;
        relationsManager = atf.relationsManager;

        UNKNOWN = atf.UNKNOWN;
        DIMENSIONLESS = atf.DIMENSIONLESS;
        BOTTOM = atf.BOTTOM;
    }

    /**
     * Obtains the resulting unit for an arithmetic operation between lht and rht.
     *
     * @param op An arithmetic operation.
     * @param lht Left hand annotated type mirror of the operation.
     * @param rht Right hand annotated type mirror of the operation.
     * @return The resulting unit as an annotation mirror, or an error if no relationships exist
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
     * @return The resulting unit as an annotation mirror, or an error if no relationships exist
     *     between the units for the given operation.
     */
    public AnnotationMirror getArithmeticUnit(Op op, AnnotationMirror lht, AnnotationMirror rht) {
        AnnotationMirror result = relationsManager.getResultUnit(op, lht, rht);

        // If there's no direct mapping of the relation, then error
        if (result == null) {
            ErrorReporter.errorAbort(
                    "no arithmetic relationship defined for " + lht + " " + op + " " + rht);
        }
        return result;
    }

    /**
     * Checks to ensure that lht and rht are comparable units. If not an error is generated. This
     * always return {@link Dimensionless} as the unit for the result.
     *
     * @param lht Left hand annotated type mirror of the comparison.
     * @param rht Right hand annotated type mirror of the comparison.
     * @param node Tree node of the comparison operation.
     * @return {@link Dimensionless} as the unit for the result.
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
        /*
         * If the units are the same, or either are Dimensionless (for magnitude comparison), or
         * either are Bottom (for null reference comparison) then set the resulting boolean or
         * integer to Dimensionless.
         *
         * Note: a boolean result is most common, eg x >= y, or x.equals(y) an int result is also
         * possible for Integer.compare(x, y)
         */
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
    // TODO: would be good to refactor this to UnitsRelationsTools, but UNKNOWN needs to be passed
    // every time...
    protected AnnotationMirror getUnit(AnnotatedTypeMirror atm) {
        return atm.getEffectiveAnnotationInHierarchy(UNKNOWN);
    }

    /**
     * Obtains the resulting unit for a compound assignment of the form "var op expr".
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
                checkAddSubAssignmentUnit(Op.ADD, varType, varUnit, exprType, node);
                break;
            case MINUS_ASSIGNMENT:
                checkAddSubAssignmentUnit(Op.SUB, varType, varUnit, exprType, node);
                break;
            case MULTIPLY_ASSIGNMENT:
                checkMulDivAssignmentUnit(Op.MUL, varType, varUnit, exprType, node);
                break;
            case DIVIDE_ASSIGNMENT:
                checkMulDivAssignmentUnit(Op.DIV, varType, varUnit, exprType, node);
                break;
            default:
                break;
        }
        // The result unit is always the unit of the left hand variable.
        return varUnit;
    }

    /**
     * Checks plus assignment and minus assignment. If the unit of "var op expr" is not the same as
     * var, a warning is given to the user.
     *
     * @param op An arithmetic operation.
     * @param varType An ATM representing the left hand variable of the compound assignment.
     * @param varUnit The unit of the variable of the compound assignment.
     * @param exprType An ATM representing the right hand expression of the compound assignment.
     * @param node The Java AST node of the compound assignment operation.
     */
    private void checkAddSubAssignmentUnit(
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

    /**
     * Checks multiply assignment and divide assignment. If the unit of "var op expr" is not the
     * same as var, a warning is given to the user.
     *
     * @param op An arithmetic operation.
     * @param varType An ATM representing the left hand variable of the compound assignment.
     * @param varUnit The unit of the variable of the compound assignment.
     * @param exprType An ATM representing the right hand expression of the compound assignment.
     * @param node The Java AST node of the compound assignment operation.
     */
    private void checkMulDivAssignmentUnit(
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
