package org.checkerframework.dataflow.cfg;

import java.util.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.*;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.node.Node;

/** Generate a String version of a control flow graph. */
public class StringCFGVisualizer<
                A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
        extends AbstractCFGVisualizer<A, S, T> {

    @Override
    public void init(Map<String, Object> args) {
        super.init(args);
    }

    @Override
    public @Nullable Map<String, Object> visualize(
            ControlFlowGraph cfg, Block entry, @Nullable Analysis<A, S, T> analysis) {
        Set<Block> blocks = cfg.getAllBlocks();
        StringBuilder sbAllBlocks = new StringBuilder();
        for (Block eachBlock : blocks) {
            sbAllBlocks.append(eachBlock.toString()).append("\n");
        }
        Map<String, Object> res = new HashMap<>();
        res.put("allBlocks", sbAllBlocks.toString());
        return res;
    }

    @Override
    public @Nullable String visualizeBlock(Block bb, @Nullable Analysis<A, S, T> analysis) {

        this.sbBlock.setLength(0);

        List<Node> contents = new ArrayList<>();
        loopOverContents(bb, contents, analysis);

        if (analysis != null) {
            visualizeBlockTransferInput(bb, analysis);
        }
        return this.sbBlock.toString();
    }

    @Override
    public @Nullable String visualizeStore(S store) {
        if (this.sbStore.length() > 0) {
            this.sbStore.setLength(0);
        }
        store.visualize(this);
        return this.sbStore.toString();
    }

    @Override
    public String visualizeStoreThisVal(A value) {
        this.sbStore.append("  this > ").append(value).append("\\n");
        return "this > " + value.toString();
    }

    @Override
    public String visualizeStoreLocalVar(FlowExpressions.LocalVariable localVar, A value) {
        this.sbStore
                .append("  ")
                .append(localVar)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
        return localVar.toString() + " > " + value.toString();
    }

    @Override
    public String visualizeStoreFieldVals(FlowExpressions.FieldAccess fieldAccess, A value) {
        this.sbStore
                .append("  ")
                .append(fieldAccess)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
        return fieldAccess.toString() + " > " + value.toString();
    }

    @Override
    public String visualizeStoreArrayVal(FlowExpressions.ArrayAccess arrayValue, A value) {
        this.sbStore
                .append("  ")
                .append(arrayValue)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
        return arrayValue.toString() + " > " + value.toString();
    }

    @Override
    public String visualizeStoreMethodVals(FlowExpressions.MethodCall methodCall, A value) {
        this.sbStore
                .append("  ")
                .append(methodCall.toString().replace("\"", "\\\""))
                .append(" > ")
                .append(value)
                .append("\\n");
        return methodCall.toString() + " > " + value.toString();
    }

    @Override
    public String visualizeStoreClassVals(FlowExpressions.ClassName className, A value) {
        this.sbStore
                .append("  ")
                .append(className)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
        return className.toString() + " > " + value.toString();
    }

    @Override
    public String visualizeStoreKeyVal(String keyName, Object value) {
        this.sbStore.append("  ").append(keyName).append(" = ").append(value).append("\\n");
        return keyName + " = " + value.toString();
    }

    @Override
    public String visualizeStoreHeader(String classCanonicalName) {
        this.sbStore.append(classCanonicalName).append(" (\\n");
        return classCanonicalName;
    }

    @Override
    public String visualizeStoreFooter() {
        this.sbStore.append(")");
        return ")";
    }

    /** StringCFGVisualizer does not write into file, so leave it blank. */
    @Override
    public void shutdown() {}
}
