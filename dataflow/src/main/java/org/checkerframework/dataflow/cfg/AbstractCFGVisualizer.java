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
