package org.checkerframework.checker.nullness;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.cfg.node.ArrayAccessNode;
import org.checkerframework.dataflow.cfg.node.FieldAccessNode;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.ThisLiteralNode;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.type.AnnotatedTypeFactory;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

public class NullnessBottomStore extends NullnessStore {
    private final AnnotationMirror NONNULL;

    public NullnessBottomStore(
            CFAbstractAnalysis<NullnessValue, NullnessStore, ?> analysis,
            boolean sequentialSemantics) {
        super(analysis, sequentialSemantics);
        NONNULL = ((NullnessAnnotatedTypeFactory) analysis.getTypeFactory()).NONNULL;
    }

    @Override
    public boolean isBottom() {
        return true;
    }

    @Override
    public NullnessStore copy() {
        // throw new BugInCF("Copying of bottom store is not allowed.");
        return this;
    }

    /** The LUB of a bottom store and a second store is the second store */
    @Override
    public NullnessStore leastUpperBound(NullnessStore other) {
        return other;
    }

    @Override
    public NullnessStore widenedUpperBound(NullnessStore previous) {
        return previous;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return this == o;
    }

    @Nullable @Override
    public NullnessValue getValue(FlowExpressions.Receiver expr) {
        return analysis.createSingleAnnotationValue(NONNULL, expr.getType());
    }

    private NullnessValue getBottomValue(Node n) {
        return analysis.createSingleAnnotationValue(NONNULL, n.getType());
    }

    @Nullable @Override
    public NullnessValue getValue(FieldAccessNode n) {
        return getBottomValue(n);
    }

    @Nullable @Override
    public NullnessValue getValue(MethodInvocationNode n) {
        return getBottomValue(n);
    }

    @Nullable @Override
    public NullnessValue getValue(ArrayAccessNode n) {
        return getBottomValue(n);
    }

    @Nullable @Override
    public NullnessValue getValue(LocalVariableNode n) {
        return getBottomValue(n);
    }

    @Nullable @Override
    public NullnessValue getValue(ThisLiteralNode n) {
        return getBottomValue(n);
    }

    @Override
    public void updateForMethodCall(
            MethodInvocationNode n, AnnotatedTypeFactory atypeFactory, NullnessValue val) {}

    @Override
    public void insertValue(FlowExpressions.Receiver r, AnnotationMirror a) {}

    @Override
    public void insertOrRefine(FlowExpressions.Receiver r, AnnotationMirror newAnno) {}

    @Override
    public void insertValue(FlowExpressions.Receiver r, @Nullable NullnessValue value) {}

    @Override
    public void insertThisValue(AnnotationMirror a, TypeMirror underlyingType) {}

    @Override
    public void replaceValue(FlowExpressions.Receiver r, @Nullable NullnessValue value) {}

    @Override
    public void clearValue(FlowExpressions.Receiver r) {}

    @Override
    public void updateForAssignment(Node n, @Nullable NullnessValue val) {}
}
