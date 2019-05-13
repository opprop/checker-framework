package org.checkerframework.dataflow.cfg;

import java.util.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.*;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.block.ExceptionBlock;
import org.checkerframework.dataflow.cfg.block.RegularBlock;
import org.checkerframework.dataflow.cfg.block.SpecialBlock;
import org.checkerframework.dataflow.cfg.node.Node;

/**
 * An abstract class of control flow graph visualizer. It provides the methods to generate a control
 * flow graph in the language of DOT. To achieve this abstract class, you need to override {@link
 * #init(Map)}, {@link #visualize(ControlFlowGraph, Block, Analysis)} and {@link #shutdown()}.
 *
 * <p>Examples: {@link DOTCFGVisualizer} and {@link StringCFGVisualizer}.
 */
public abstract class AbstractCFGVisualizer<
                A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
        implements CFGVisualizer<A, S, T> {

    protected boolean verbose;

    protected StringBuilder sbStore;
    protected StringBuilder sbBlock;

    @Override
    public void init(Map<String, Object> args) {
        {
            Object verb = args.get("verbose");
            this.verbose =
                    verb != null
                            && (verb instanceof String
                                    ? Boolean.getBoolean((String) verb)
                                    : (boolean) verb);
        }
        this.sbStore = new StringBuilder();
        this.sbBlock = new StringBuilder();
    }

    /**
     * Generate the order of processing blocks.
     *
     * @param cfg the current control flow graph
     */
    protected IdentityHashMap<Block, List<Integer>> getProcessOrder(ControlFlowGraph cfg) {
        IdentityHashMap<Block, List<Integer>> depthFirstOrder = new IdentityHashMap<>();
        int count = 1;
        for (Block b : cfg.getDepthFirstOrderedBlocks()) {
            if (depthFirstOrder.get(b) == null) {
                depthFirstOrder.put(b, new ArrayList<>());
            }
            depthFirstOrder.get(b).add(count++);
        }
        return depthFirstOrder;
    }

    protected void loopOverContents(
            Block bb, List<Node> contents, @Nullable Analysis<A, S, T> analysis) {
        switchBlockType(bb, contents);
        boolean notFirst = false;
        for (Node t : contents) {
            if (notFirst) {
                this.sbBlock.append("\\n");
            }
            notFirst = true;
            visualizeBlockNode(t, analysis);
        }
    }

    protected void switchBlockType(Block bb, List<Node> contents) {
        switch (bb.getType()) {
            case REGULAR_BLOCK:
                contents.addAll(((RegularBlock) bb).getContents());
                break;
            case EXCEPTION_BLOCK:
                contents.add(((ExceptionBlock) bb).getNode());
                break;
            case CONDITIONAL_BLOCK:
                break;
            case SPECIAL_BLOCK:
                break;
            default:
                assert false : "All types of basic blocks covered";
        }
    }

    @Override
    public void visualizeSpecialBlock(SpecialBlock sbb) {
        switch (sbb.getSpecialType()) {
            case ENTRY:
                this.sbBlock.append("<entry>");
                break;
            case EXIT:
                this.sbBlock.append("<exit>");
                break;
            case EXCEPTIONAL_EXIT:
                this.sbBlock.append("<exceptional-exit>");
                break;
        }
    }

    @Override
    public void visualizeBlockTransferInput(Block bb, Analysis<A, S, T> analysis) {
        assert analysis != null
                : "analysis should be non-null when visualizing the transfer input of a block.";

        TransferInput<A, S> input = analysis.getInput(bb);
        assert input != null;

        this.sbStore.setLength(0);

        // split input representation to two lines
        this.sbStore.append("Before:");
        if (!input.containsTwoStores()) {
            S regularStore = input.getRegularStore();
            this.sbStore.append('[');
            visualizeStore(regularStore);
            this.sbStore.append(']');
        } else {
            S thenStore = input.getThenStore();
            this.sbStore.append("[then=");
            visualizeStore(thenStore);
            S elseStore = input.getElseStore();
            this.sbStore.append(", else=");
            visualizeStore(elseStore);
            this.sbStore.append("]");
        }
        // separator
        this.sbStore.append("\\n~~~~~~~~~\\n");

        // the transfer input before this block is added before the block content
        this.sbBlock.insert(0, this.sbStore);

        if (verbose) {
            Node lastNode;
            switch (bb.getType()) {
                case REGULAR_BLOCK:
                    List<Node> blockContents = ((RegularBlock) bb).getContents();
                    lastNode = blockContents.get(blockContents.size() - 1);
                    break;
                case EXCEPTION_BLOCK:
                    lastNode = ((ExceptionBlock) bb).getNode();
                    break;
                default:
                    lastNode = null;
            }
            if (lastNode != null) {
                this.sbStore.setLength(0);
                this.sbStore.append("\\n~~~~~~~~~\\n");
                this.sbStore.append("After:");
                visualizeStore(analysis.getResult().getStoreAfter(lastNode));
                this.sbBlock.append(this.sbStore);
            }
        }
    }

    @Override
    public void visualizeBlockNode(Node t, @Nullable Analysis<A, S, T> analysis) {
        this.sbBlock
                .append(prepareString(t.toString()))
                .append("   [ ")
                .append(prepareNodeType(t))
                .append(" ]");
        if (analysis != null) {
            A value = analysis.getValue(t);
            if (value != null) {
                this.sbBlock.append("    > ").append(prepareString(value.toString()));
            }
        }
    }

    protected String prepareNodeType(Node t) {
        String name = t.getClass().getSimpleName();
        return name.replace("Node", "");
    }

    /**
     * Escape double quotes.
     *
     * @param s the String to be processed.
     */
    protected String prepareString(String s) {
        return s.replace("\"", "\\\"");
    }

    @Override
    public String visualizeStore(S store) {
        String sbStoreBackup = this.sbStore.toString();
        this.sbStore.setLength(0);
        store.visualize(this);
        String sbStoreReturnValue = this.sbStore.toString();
        this.sbStore.setLength(0);
        this.sbStore.append(sbStoreBackup).append(sbStoreReturnValue);
        return sbStoreReturnValue;
    }

    @Override
    public void visualizeStoreThisVal(A value) {
        this.sbStore.append("  this > ").append(value).append("\\n");
    }

    @Override
    public void visualizeStoreLocalVar(FlowExpressions.LocalVariable localVar, A value) {
        this.sbStore
                .append("  ")
                .append(localVar)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
    }

    @Override
    public void visualizeStoreFieldVals(FlowExpressions.FieldAccess fieldAccess, A value) {
        this.sbStore
                .append("  ")
                .append(fieldAccess)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
    }

    @Override
    public void visualizeStoreArrayVal(FlowExpressions.ArrayAccess arrayValue, A value) {
        this.sbStore
                .append("  ")
                .append(arrayValue)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
    }

    @Override
    public void visualizeStoreMethodVals(FlowExpressions.MethodCall methodCall, A value) {
        this.sbStore
                .append("  ")
                .append(methodCall.toString().replace("\"", "\\\""))
                .append(" > ")
                .append(value)
                .append("\\n");
    }

    @Override
    public void visualizeStoreClassVals(FlowExpressions.ClassName className, A value) {
        this.sbStore
                .append("  ")
                .append(className)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
    }

    @Override
    public void visualizeStoreKeyVal(String keyName, Object value) {
        this.sbStore.append("  ").append(keyName).append(" = ").append(value).append("\\n");
    }

    protected String escapeDoubleQuotes(final String str) {
        return str.replace("\"", "\\\"");
    }

    protected String toStringEscapeDoubleQuotes(final Object obj) {
        return escapeDoubleQuotes(String.valueOf(obj));
    }

    @Override
    public void visualizeStoreHeader(String classCanonicalName) {
        this.sbStore.append(classCanonicalName).append(" (\\n");
    }

    @Override
    public void visualizeStoreFooter() {
        this.sbStore.append(")");
    }
}
