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

public class KeyForBottomStore extends KeyForStore {
    public KeyForBottomStore(
            CFAbstractAnalysis<KeyForValue, KeyForStore, ?> analysis, boolean sequentialSemantics) {
        super(analysis, sequentialSemantics);
    }

    @Override
    public boolean isBottom() {
        return true;
    }

    @Override
    public KeyForStore copy() {
        //        throw new BugInCF("Copying of bottom store is not allowed.");
        return this;
    }

    /** The LUB of a bottom store and a second store is the second store */
    @Override
    public KeyForStore leastUpperBound(KeyForStore other) {
        return other;
    }

    @Override
    public KeyForStore widenedUpperBound(KeyForStore previous) {
        return previous;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return this == o;
    }

    @Nullable @Override
    public KeyForValue getValue(FlowExpressions.Receiver expr) {
        return null;
    }

    @Nullable @Override
    public KeyForValue getValue(FieldAccessNode n) {
        return null;
    }

    @Nullable @Override
    public KeyForValue getValue(MethodInvocationNode n) {
        return null;
    }

    @Nullable @Override
    public KeyForValue getValue(ArrayAccessNode n) {
        return null;
    }

    @Nullable @Override
    public KeyForValue getValue(LocalVariableNode n) {
        return null;
    }

    @Nullable @Override
    public KeyForValue getValue(ThisLiteralNode n) {
        return null;
    }

    @Override
    public void updateForMethodCall(
            MethodInvocationNode n, AnnotatedTypeFactory atypeFactory, KeyForValue val) {}

    @Override
    public void insertValue(FlowExpressions.Receiver r, AnnotationMirror a) {}

    @Override
    public void insertOrRefine(FlowExpressions.Receiver r, AnnotationMirror newAnno) {}

    @Override
    public void insertValue(FlowExpressions.Receiver r, @Nullable KeyForValue value) {}

    @Override
    public void insertThisValue(AnnotationMirror a, TypeMirror underlyingType) {}

    @Override
    public void replaceValue(FlowExpressions.Receiver r, @Nullable KeyForValue value) {}

    @Override
    public void clearValue(FlowExpressions.Receiver r) {}

    @Override
    public void updateForAssignment(Node n, @Nullable KeyForValue val) {}
}
