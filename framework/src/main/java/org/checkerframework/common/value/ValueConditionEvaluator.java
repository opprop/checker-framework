package org.checkerframework.common.value;

import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.common.value.qual.IntRange;
import org.checkerframework.common.value.qual.IntRangeFromGTENegativeOne;
import org.checkerframework.common.value.qual.IntRangeFromNonNegative;
import org.checkerframework.common.value.qual.IntRangeFromPositive;
import org.checkerframework.common.value.qual.IntVal;
import org.checkerframework.common.value.util.Range;
import org.checkerframework.dataflow.analysis.ConditionEvaluator;
import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.analysis.FlowExpressions.Receiver;
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

public class ValueConditionEvaluator extends ConditionEvaluator<CFValue, CFStore> {

    protected final ValueAnnotatedTypeFactory atypefactory;

    protected final QualifierHierarchy hierarchy;

    public ValueConditionEvaluator(CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        this.atypefactory = (ValueAnnotatedTypeFactory) analysis.getTypeFactory();
        this.hierarchy = atypefactory.getQualifierHierarchy();
    }

    @Override
    public ConditionalFlow visitGreaterThan(
            GreaterThanNode node, TransferInput<CFValue, CFStore> in) {
        Receiver leftRec = FlowExpressions.internalReprOf(atypefactory, node.getLeftOperand());
        Receiver rightRec = FlowExpressions.internalReprOf(atypefactory, node.getRightOperand());
        AnnotationMirror leftAm = getValueAnnotation(in.getThenStore().getValue(leftRec));
        AnnotationMirror rightAm = getValueAnnotation(in.getThenStore().getValue(rightRec));
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.from > rightRange.to) {
                return ConditionalFlow.THEN;
            } else if (leftRange.to <= rightRange.from) {
                return ConditionalFlow.ELSE;
            }
        }
        return super.visitGreaterThan(node, in);
    }

    @Override
    public ConditionalFlow visitGreaterThanOrEqual(
            GreaterThanOrEqualNode node, TransferInput<CFValue, CFStore> in) {
        Receiver leftRec = FlowExpressions.internalReprOf(atypefactory, node.getLeftOperand());
        Receiver rightRec = FlowExpressions.internalReprOf(atypefactory, node.getRightOperand());
        AnnotationMirror leftAm = getValueAnnotation(in.getThenStore().getValue(leftRec));
        AnnotationMirror rightAm = getValueAnnotation(in.getThenStore().getValue(rightRec));
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.from >= rightRange.to) {
                return ConditionalFlow.THEN;
            } else if (leftRange.to < rightRange.from) {
                return ConditionalFlow.ELSE;
            }
        }
        return super.visitGreaterThanOrEqual(node, in);
    }

    @Override
    public ConditionalFlow visitLessThanOrEqual(
            LessThanOrEqualNode node, TransferInput<CFValue, CFStore> in) {
        Receiver leftRec = FlowExpressions.internalReprOf(atypefactory, node.getLeftOperand());
        Receiver rightRec = FlowExpressions.internalReprOf(atypefactory, node.getRightOperand());
        AnnotationMirror leftAm = getValueAnnotation(in.getThenStore().getValue(leftRec));
        AnnotationMirror rightAm = getValueAnnotation(in.getThenStore().getValue(rightRec));
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.to <= rightRange.from) {
                return ConditionalFlow.THEN;
            } else if (leftRange.from > rightRange.to) {
                return ConditionalFlow.ELSE;
            }
        }
        return super.visitLessThanOrEqual(node, in);
    }

    @Override
    public ConditionalFlow visitLessThan(LessThanNode node, TransferInput<CFValue, CFStore> in) {
        Receiver leftRec = FlowExpressions.internalReprOf(atypefactory, node.getLeftOperand());
        Receiver rightRec = FlowExpressions.internalReprOf(atypefactory, node.getRightOperand());

        CFStore store = in.getThenStore().leastUpperBound(in.getElseStore());
        AnnotationMirror leftAm = getValueAnnotation(store.getValue(leftRec));
        AnnotationMirror rightAm = getValueAnnotation(store.getValue(rightRec));
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.to < rightRange.from) {
                return ConditionalFlow.THEN;
            } else if (leftRange.from >= rightRange.to) {
                return ConditionalFlow.ELSE;
            }
        }

        leftAm = getValueAnnotation(in.getElseStore().getValue(leftRec));
        rightAm = getValueAnnotation(in.getElseStore().getValue(rightRec));
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.to < rightRange.from) {
                return ConditionalFlow.THEN;
            } else if (leftRange.from >= rightRange.to) {
                return ConditionalFlow.ELSE;
            }
        }

        return super.visitLessThan(node, in);
    }

    @Override
    public ConditionalFlow visitEqualTo(EqualToNode node, TransferInput<CFValue, CFStore> in) {
        Receiver leftRec = FlowExpressions.internalReprOf(atypefactory, node.getLeftOperand());
        Receiver rightRec = FlowExpressions.internalReprOf(atypefactory, node.getRightOperand());
        AnnotationMirror leftAm = getValueAnnotation(in.getThenStore().getValue(leftRec));
        AnnotationMirror rightAm = getValueAnnotation(in.getThenStore().getValue(rightRec));
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.to == rightRange.to && leftRange.from == rightRange.from) {
                return ConditionalFlow.THEN;
            } else if (leftRange.to < rightRange.from || rightRange.to < leftRange.from) {
                return ConditionalFlow.ELSE;
            }
        }
        return super.visitEqualTo(node, in);
    }

    @Override
    public ConditionalFlow visitNotEqual(NotEqualNode node, TransferInput<CFValue, CFStore> in) {
        Receiver leftRec = FlowExpressions.internalReprOf(atypefactory, node.getLeftOperand());
        Receiver rightRec = FlowExpressions.internalReprOf(atypefactory, node.getRightOperand());
        AnnotationMirror leftAm = getValueAnnotation(in.getThenStore().getValue(leftRec));
        AnnotationMirror rightAm = getValueAnnotation(in.getThenStore().getValue(rightRec));
        if (isIntRangeOrIntVal(leftAm) && isIntRangeOrIntVal(rightAm)) {
            Range leftRange = ValueAnnotatedTypeFactory.getRange(leftAm);
            Range rightRange = ValueAnnotatedTypeFactory.getRange(rightAm);
            if (leftRange.to == rightRange.to && leftRange.from == rightRange.from) {
                return ConditionalFlow.ELSE;
            } else if (leftRange.to < rightRange.from || rightRange.to < leftRange.from) {
                return ConditionalFlow.THEN;
            }
        }
        return super.visitNotEqual(node, in);
    }

    /** Returns true if this node is annotated with {@code @IntRange} and {@code @IntVal}. */
    private boolean isIntRangeOrIntVal(AnnotationMirror am) {
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
