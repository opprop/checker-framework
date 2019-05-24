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
 * An abstract class of control flow graph visualizer. To achieve this abstract class, you need to
 * implement some of the methods in {@link CFGVisualizer}.
 *
 * <p>Examples: {@link DOTCFGVisualizer} and {@link StringCFGVisualizer}.
 */
public abstract class AbstractCFGVisualizer<
                A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
        implements CFGVisualizer<A, S, T> {

    protected boolean verbose;

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
    }

    protected String loopOverBlockContents(
            Block bb, @Nullable Analysis<A, S, T> analysis, String lineSeparator) {

        List<Node> contents = new ArrayList<>();
        StringBuilder sbBlockContents = new StringBuilder();
        boolean notFirst = false;

        switchBlockType(bb, contents);

        for (Node t : contents) {
            if (notFirst) {
                sbBlockContents.append(lineSeparator);
            }
            notFirst = true;
            sbBlockContents.append(visualizeBlockNode(t, analysis));
        }
        return sbBlockContents.toString();
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
    public String visualizeBlockTransferInput(Block bb, Analysis<A, S, T> analysis) {
        assert analysis != null
                : "analysis should be non-null when visualizing the transfer input of a block.";

        TransferInput<A, S> input = analysis.getInput(bb);
        assert input != null;

        StringBuilder sbStore = new StringBuilder();

        // split input representation to two lines
        sbStore.append("Before:");
        if (!input.containsTwoStores()) {
            S regularStore = input.getRegularStore();
            sbStore.append('[');
            sbStore.append(visualizeStore(regularStore));
            sbStore.append(']');
        } else {
            S thenStore = input.getThenStore();
            sbStore.append("[then=");
            sbStore.append(visualizeStore(thenStore));
            S elseStore = input.getElseStore();
            sbStore.append(", else=");
            sbStore.append(visualizeStore(elseStore));
            sbStore.append("]");
        }
        // separator
        sbStore.append("\\n~~~~~~~~~\\n");

        return sbStore.toString();
    }

    @Override
    public String visualizeSpecialBlock(SpecialBlock sbb) {
        String specialBlock = "";
        switch (sbb.getSpecialType()) {
            case ENTRY:
                specialBlock = "<entry>";
                break;
            case EXIT:
                specialBlock = "<exit>";
                break;
            case EXCEPTIONAL_EXIT:
                specialBlock = "<exceptional-exit>";
                break;
        }
        return specialBlock;
    }

    protected Node getLastNode(Block bb) {
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
        return lastNode;
    }

    @Override
    public String visualizeBlockNode(Node t, @Nullable Analysis<A, S, T> analysis) {
        StringBuilder sbBlockNode = new StringBuilder();
        sbBlockNode
                .append(prepareString(t.toString()))
                .append("   [ ")
                .append(prepareNodeType(t))
                .append(" ]");
        if (analysis != null) {
            A value = analysis.getValue(t);
            if (value != null) {
                sbBlockNode.append("    > ").append(prepareString(value.toString()));
            }
        }
        return sbBlockNode.toString();
    }

    private String prepareNodeType(Node t) {
        String name = t.getClass().getSimpleName();
        return name.replace("Node", "");
    }

    /**
     * Escape double quotes.
     *
     * @param s the String to be processed.
     */
    private String prepareString(String s) {
        return s.replace("\"", "\\\"");
    }
}
