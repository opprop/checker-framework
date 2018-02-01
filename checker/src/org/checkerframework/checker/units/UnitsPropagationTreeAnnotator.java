package org.checkerframework.checker.units;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import org.checkerframework.checker.units.qual.Op;
import org.checkerframework.checker.units.qual.Prefix;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;

/**
 * Units Propagation Tree Annotator type checks and computes resulting units for arithmetic and
 * comparison operations.
 */
public class UnitsPropagationTreeAnnotator extends PropagationTreeAnnotator {
    private final UnitsAnnotatedTypeFactory atf;
    private final Elements elements;
    private final UnitsRelationsEnforcer relationsEnforcer;

    private final AnnotationMirror UNKNOWN;

    public UnitsPropagationTreeAnnotator(UnitsAnnotatedTypeFactory atf) {
        super(atf);
        this.atf = atf;
        this.elements = atf.getElementUtils();
        this.relationsEnforcer = atf.relationsEnforcer;

        this.UNKNOWN = atf.UNKNOWN;
    }

    /** Type check and propagate resulting units for arithmetic and comparison operations */
    @Override
    public Void visitBinary(BinaryTree node, AnnotatedTypeMirror type) {
        AnnotatedTypeMirror lht = atf.getAnnotatedType(node.getLeftOperand());
        AnnotatedTypeMirror rht = atf.getAnnotatedType(node.getRightOperand());
        Tree.Kind kind = node.getKind();

        // Remove Prefix.one
        if (UnitsRelationsTools.getPrefixValue(lht) == Prefix.one) {
            lht = UnitsRelationsTools.removePrefix(elements, lht);
        }
        if (UnitsRelationsTools.getPrefixValue(rht) == Prefix.one) {
            rht = UnitsRelationsTools.removePrefix(elements, rht);
        }

        // The computation of the result unit is delegated to relationsEnforcer
        switch (kind) {
            case MINUS:
                type.replaceAnnotation(relationsEnforcer.getArithmeticUnit(Op.SUB, lht, rht));
                break;
            case PLUS:
                type.replaceAnnotation(relationsEnforcer.getArithmeticUnit(Op.ADD, lht, rht));
                break;
            case DIVIDE:
                type.replaceAnnotation(relationsEnforcer.getArithmeticUnit(Op.DIV, lht, rht));
                break;
            case MULTIPLY:
                type.replaceAnnotation(relationsEnforcer.getArithmeticUnit(Op.MUL, lht, rht));
                break;
            case REMAINDER:
                // in modulo operation, it always returns the left operand unit regardless of what
                // unit it is
                type.replaceAnnotation(lht.getAnnotationInHierarchy(UNKNOWN));
                break;
            case EQUAL_TO:
            case NOT_EQUAL_TO:
            case GREATER_THAN:
            case GREATER_THAN_EQUAL:
            case LESS_THAN:
            case LESS_THAN_EQUAL:
                type.replaceAnnotation(relationsEnforcer.getComparableUnits(lht, rht, node));
                break;
            default:
                // For unhandled binary operations
                return super.visitBinary(node, type);
        }

        return null;
    }

    /** Type check and propagate units from compound assignment operations. */
    // This is called if a compound assignment is a part of another expression, eg x = y += z;
    @SuppressWarnings("fallthrough") // fallthrough is intended
    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, AnnotatedTypeMirror type) {
        switch (node.getKind()) {
            case PLUS_ASSIGNMENT:
            case MINUS_ASSIGNMENT:
            case MULTIPLY_ASSIGNMENT:
            case DIVIDE_ASSIGNMENT:
                AnnotationMirror resultUnit = relationsEnforcer.getCompoundAssignmentUnit(node);
                if (resultUnit != null) {
                    type.replaceAnnotation(resultUnit);
                    return null;
                }
            default:
                return super.visitCompoundAssignment(node, type);
        }
    }
}
