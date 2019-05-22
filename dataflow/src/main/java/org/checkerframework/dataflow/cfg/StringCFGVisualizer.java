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

        this.sbBlock.setLength(0);

        List<Node> contents = new ArrayList<>();
        loopOverContents(bb, contents, analysis);

        if (analysis != null) {
            visualizeBlockTransferInput(bb, analysis);
        }
        return this.sbBlock.toString();
    }

    @Override
    public void visualizeBlockTransferInput(Block bb, Analysis<A, S, T> analysis) {

        TransferInput<A, S> input = analysis.getInput(bb);
        assert input != null;

        this.sbStore.setLength(0);

        this.sbStore.append("Before:");
        if (!input.containsTwoStores()) {
            S regularStore = input.getRegularStore();
            this.sbStore.append(visualizeStore(regularStore));
        } else {
            S thenStore = input.getThenStore();
            this.sbStore.append("Then:");
            this.sbStore.append(visualizeStore(thenStore));
            S elseStore = input.getElseStore();
            this.sbStore.append("Else:");
            this.sbStore.append(visualizeStore(elseStore));
        }

        // the transfer input before this block is added before the block content
        this.sbBlock.insert(0, this.sbStore);

        if (verbose) {
            Node lastNode = getLastNode(bb);
            if (lastNode != null) {
                this.sbStore.setLength(0);
                this.sbStore.append("After:");
                this.sbStore.append(visualizeStore(analysis.getResult().getStoreAfter(lastNode)));
                this.sbBlock.append(this.sbStore);
            }
        }
    }

    @Override
    public String visualizeStore(S store) {
        return store.visualize(this);
    }

    @Override
    public String visualizeStoreThisVal(A value) {
        return "this > " + value.toString() + lineSeparator;
    }

    @Override
    public String visualizeStoreLocalVar(FlowExpressions.LocalVariable localVar, A value) {
        return localVar.toString() + " > " + value.toString() + lineSeparator;
    }

    @Override
    public String visualizeStoreFieldVals(FlowExpressions.FieldAccess fieldAccess, A value) {
        return fieldAccess.toString() + " > " + value.toString() + lineSeparator;
    }

    @Override
    public String visualizeStoreArrayVal(FlowExpressions.ArrayAccess arrayValue, A value) {
        return arrayValue.toString() + " > " + value.toString() + lineSeparator;
    }

    @Override
    public String visualizeStoreMethodVals(FlowExpressions.MethodCall methodCall, A value) {
        return methodCall.toString() + " > " + value.toString() + lineSeparator;
    }

    @Override
    public String visualizeStoreClassVals(FlowExpressions.ClassName className, A value) {
        return className.toString() + " > " + value.toString() + lineSeparator;
    }

    @Override
    public String visualizeStoreKeyVal(String keyName, Object value) {
        return keyName + " = " + value.toString() + lineSeparator;
    }

    @Override
    public String visualizeStoreHeader(String classCanonicalName) {
        return classCanonicalName + lineSeparator;
    }

    @Override
    public String visualizeStoreFooter() {
        return ")";
    }

    /** StringCFGVisualizer does not write into file, so left intentionally blank. */
    @Override
    public void shutdown() {}
}
