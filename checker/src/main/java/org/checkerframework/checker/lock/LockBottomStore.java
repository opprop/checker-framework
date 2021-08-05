package org.checkerframework.checker.lock;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.cfg.node.ArrayAccessNode;
import org.checkerframework.dataflow.cfg.node.FieldAccessNode;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.ThisLiteralNode;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeFactory;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

public class LockBottomStore extends LockStore {
    public LockBottomStore(LockAnalysis analysis, boolean sequentialSemantics) {
        super(analysis, sequentialSemantics);
    }

    @Override
    public boolean isBottom() {
        return true;
    }

    @Override
    public LockStore copy() {
        //        throw new BugInCF("Copying of bottom store is not allowed.");
        return this;
    }

    /** The LUB of a bottom store and a second store is the second store */
    @Override
    public LockStore leastUpperBound(LockStore other) {
        return other;
    }

    @Override
    public LockStore widenedUpperBound(LockStore previous) {
        return previous;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return this == o;
    }

    @Nullable @Override
    public CFValue getValue(FlowExpressions.Receiver expr) {
        return null;
    }

    @Nullable @Override
    public CFValue getValue(FieldAccessNode n) {
        return null;
    }

    @Nullable @Override
    public CFValue getValue(MethodInvocationNode n) {
        return null;
    }

    @Nullable @Override
    public CFValue getValue(ArrayAccessNode n) {
        return null;
    }

    @Nullable @Override
    public CFValue getValue(LocalVariableNode n) {
        return null;
    }

    @Nullable @Override
    public CFValue getValue(ThisLiteralNode n) {
        return null;
    }

    @Override
    public void updateForMethodCall(
            MethodInvocationNode n, AnnotatedTypeFactory atypeFactory, CFValue val) {}

    @Override
    public void insertValue(FlowExpressions.Receiver r, AnnotationMirror a) {}

    @Override
    public void insertOrRefine(FlowExpressions.Receiver r, AnnotationMirror newAnno) {}

    @Override
    public void insertValue(FlowExpressions.Receiver r, @Nullable CFValue value) {}

    @Override
    public void insertThisValue(AnnotationMirror a, TypeMirror underlyingType) {}

    @Override
    public void replaceValue(FlowExpressions.Receiver r, @Nullable CFValue value) {}

    @Override
    public void clearValue(FlowExpressions.Receiver r) {}

    @Override
    public void updateForAssignment(Node n, @Nullable CFValue val) {}
}
