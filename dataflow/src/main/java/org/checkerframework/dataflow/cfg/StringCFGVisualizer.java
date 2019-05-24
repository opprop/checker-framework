package org.checkerframework.dataflow.cfg;

import java.util.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.*;
import org.checkerframework.dataflow.cfg.block.Block;

/** Generate a String version of a control flow graph. */
public class StringCFGVisualizer<
                A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
        extends AbstractCFGVisualizer<A, S, T> {

    private final String lineSeparator = System.getProperty("line.separator");

    @Override
    public void init(Map<String, Object> args) {
        super.init(args);
    }

    @Override
    public Map<String, Object> visualize(
            ControlFlowGraph cfg, Block entry, @Nullable Analysis<A, S, T> analysis) {
        return null;
    }

    @Override
    public @Nullable String visualizeBlock(Block bb, @Nullable Analysis<A, S, T> analysis) {
        return super.visualizeBlock(bb, analysis).replace("\\n", "\n") + " \",];\n";
    }

    @Override
    public String visualizeStoreThisVal(A value) {
        return "this > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreLocalVar(FlowExpressions.LocalVariable localVar, A value) {
        return localVar.toString() + " > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreFieldVals(FlowExpressions.FieldAccess fieldAccess, A value) {
        return fieldAccess.toString() + " > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreArrayVal(FlowExpressions.ArrayAccess arrayValue, A value) {
        return arrayValue.toString() + " > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreMethodVals(FlowExpressions.MethodCall methodCall, A value) {
        return methodCall.toString() + " > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreClassVals(FlowExpressions.ClassName className, A value) {
        return className.toString() + " > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreKeyVal(String keyName, Object value) {
        return keyName + " = " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreHeader(String classCanonicalName) {
        return classCanonicalName + " (" + lineSeparator;
    }

    /** StringCFGVisualizer does not write into file, so left intentionally blank. */
    @Override
    public void shutdown() {}
}
