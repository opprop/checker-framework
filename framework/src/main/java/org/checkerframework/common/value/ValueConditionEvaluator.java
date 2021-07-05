package org.checkerframework.common.value;

import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntRangeFromGTENegativeOne;
import org.checkerframework.common.value.qual.IntRangeFromNonNegative;
import org.checkerframework.common.value.qual.IntRangeFromPositive;
import org.checkerframework.common.value.qual.IntVal;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.dataflow.analysis.ConditionEvaluator;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.cfg.node.EqualToNode;
import org.checkerframework.dataflow.cfg.node.GreaterThanNode;
import org.checkerframework.dataflow.cfg.node.GreaterThanOrEqualNode;
import org.checkerframework.dataflow.cfg.node.LessThanNode;
import org.checkerframework.dataflow.cfg.node.LessThanOrEqualNode;
import org.checkerframework.dataflow.cfg.node.NotEqualNode;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

/** The condition evaluator class for the Value Checker. */
public class ValueConditionEvaluator extends ConditionEvaluator<CFValue, CFStore> {
    /** The Value type factory. */
    protected final ValueAnnotatedTypeFactory atypefactory;
    /** The Value qualifier hierarchy. */
    protected final QualifierHierarchy hierarchy;

    /**
     * Create a new Value condition evaluator
     *
     * @param analysis the appropriate analysis
     */
    public ValueConditionEvaluator(CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        this.atypefactory = (ValueAnnotatedTypeFactory) analysis.getTypeFactory();
        this.hierarchy = atypefactory.getQualifierHierarchy();
    }

    @Override
    public ConditionFlow visitGreaterThan(
            GreaterThanNode node, TransferInput<CFValue, CFStore> in) {
        CFValue leftValue = in.getValueOfSubNode(node.getLeftOperand());
        CFValue rightValue = in.getValueOfSubNode(node.getRightOperand());
        AnnotationMirror leftAm = getValueAnnotation(leftValue);
        AnnotationMirror rightAm = getValueAnnotation(rightValue);
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.from > rightRange.to) {
                return ConditionFlow.TRUE;
            } else if (leftRange.to <= rightRange.from) {
                return ConditionFlow.FALSE;
            }
        }
        return super.visitGreaterThan(node, in);
    }

    @Override
    public ConditionFlow visitGreaterThanOrEqual(
            GreaterThanOrEqualNode node, TransferInput<CFValue, CFStore> in) {
        CFValue leftValue = in.getValueOfSubNode(node.getLeftOperand());
        CFValue rightValue = in.getValueOfSubNode(node.getRightOperand());
        AnnotationMirror leftAm = getValueAnnotation(leftValue);
        AnnotationMirror rightAm = getValueAnnotation(rightValue);
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.from >= rightRange.to) {
                return ConditionFlow.TRUE;
            } else if (leftRange.to < rightRange.from) {
                return ConditionFlow.FALSE;
            }
        }
        return super.visitGreaterThanOrEqual(node, in);
    }

    @Override
    public ConditionFlow visitLessThanOrEqual(
            LessThanOrEqualNode node, TransferInput<CFValue, CFStore> in) {
        CFValue leftValue = in.getValueOfSubNode(node.getLeftOperand());
        CFValue rightValue = in.getValueOfSubNode(node.getRightOperand());
        AnnotationMirror leftAm = getValueAnnotation(leftValue);
        AnnotationMirror rightAm = getValueAnnotation(rightValue);
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.to <= rightRange.from) {
                return ConditionFlow.TRUE;
            } else if (leftRange.from > rightRange.to) {
                return ConditionFlow.FALSE;
            }
        }
        return super.visitLessThanOrEqual(node, in);
    }

    @Override
    public ConditionFlow visitLessThan(LessThanNode node, TransferInput<CFValue, CFStore> in) {
        CFValue leftValue = in.getValueOfSubNode(node.getLeftOperand());
        CFValue rightValue = in.getValueOfSubNode(node.getRightOperand());
        AnnotationMirror leftAm = getValueAnnotation(leftValue);
        AnnotationMirror rightAm = getValueAnnotation(rightValue);
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.to < rightRange.from) {
                return ConditionFlow.TRUE;
            } else if (leftRange.from >= rightRange.to) {
                return ConditionFlow.FALSE;
            }
        }

        return super.visitLessThan(node, in);
    }

    @Override
    public ConditionFlow visitEqualTo(EqualToNode node, TransferInput<CFValue, CFStore> in) {
        CFValue leftValue = in.getValueOfSubNode(node.getLeftOperand());
        CFValue rightValue = in.getValueOfSubNode(node.getRightOperand());
        AnnotationMirror leftAm = getValueAnnotation(leftValue);
        AnnotationMirror rightAm = getValueAnnotation(rightValue);
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.to == leftRange.from
                    && leftRange.to == rightRange.to
                    && leftRange.from == rightRange.from) {
                return ConditionFlow.TRUE;
            } else if (leftRange.to < rightRange.from || rightRange.to < leftRange.from) {
                return ConditionFlow.FALSE;
            }
        }
        return super.visitEqualTo(node, in);
    }

    @Override
    public ConditionFlow visitNotEqual(NotEqualNode node, TransferInput<CFValue, CFStore> in) {
        CFValue leftValue = in.getValueOfSubNode(node.getLeftOperand());
        CFValue rightValue = in.getValueOfSubNode(node.getRightOperand());
        AnnotationMirror leftAm = getValueAnnotation(leftValue);
        AnnotationMirror rightAm = getValueAnnotation(rightValue);
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.to == leftRange.from
                    && leftRange.to == rightRange.to
                    && leftRange.from == rightRange.from) {
                return ConditionFlow.FALSE;
            } else if (leftRange.to < rightRange.from || rightRange.to < leftRange.from) {
                return ConditionFlow.TRUE;
            }
        }
        return super.visitNotEqual(node, in);
    }

    /**
     * Returns true if this node is annotated with {@code @IntRange} and {@code @IntVal}.
     *
     * @param am the annotation mirror
     * @return true if the annotation mirror is a integer
     */
    private boolean isIntRangeOrIntVal(AnnotationMirror am) {
        if (am == null) {
            return false;
        }
        return AnnotationUtils.areSameByClass(am, IntVal.class)
                || AnnotationUtils.areSameByClass(am, IntRange.class)
                || AnnotationUtils.areSameByClass(am, IntRangeFromPositive.class)
                || AnnotationUtils.areSameByClass(am, IntRangeFromNonNegative.class)
                || AnnotationUtils.areSameByClass(am, IntRangeFromGTENegativeOne.class);
    }

    /**
     * Extract the Value Checker annotation from a CFValue object.
     *
     * @param cfValue a CFValue object
     * @return the Value Checker annotation within cfValue
     */
    private AnnotationMirror getValueAnnotation(CFValue cfValue) {
        return hierarchy.findAnnotationInHierarchy(
                cfValue.getAnnotations(), atypefactory.UNKNOWNVAL);
    }
}
