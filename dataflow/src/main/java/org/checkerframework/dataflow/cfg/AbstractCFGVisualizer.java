package org.checkerframework.dataflow.cfg;

import java.util.*;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.*;
import org.checkerframework.dataflow.cfg.block.*;
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

    protected String generateGraphHelper(
            ControlFlowGraph cfg,
            Block entry,
            @Nullable Analysis<A, S, T> analysis,
            String thenLabelContent,
            String elseLabelContent) {
        Set<Block> visited = new HashSet<>();

        StringBuilder sbDigraph = new StringBuilder();
        sbDigraph.append("digraph {\n");

        Block cur = entry;
        Queue<Block> workList = new ArrayDeque<>();
        visited.add(entry);
        // traverse control flow graph and define all arrows
        while (cur != null) {
            if (cur.getType() == Block.BlockType.CONDITIONAL_BLOCK) {
                ConditionalBlock ccur = ((ConditionalBlock) cur);
                Block thenSuccessor = ccur.getThenSuccessor();
                sbDigraph.append(
                        addEdge(
                                ccur.getId(),
                                thenSuccessor.getId(),
                                thenLabelContent + ccur.getThenFlowRule()));
                if (!visited.contains(thenSuccessor)) {
                    visited.add(thenSuccessor);
                    workList.add(thenSuccessor);
                }
                Block elseSuccessor = ccur.getElseSuccessor();
                sbDigraph.append(
                        addEdge(
                                ccur.getId(),
                                elseSuccessor.getId(),
                                elseLabelContent + ccur.getElseFlowRule()));
                if (!visited.contains(elseSuccessor)) {
                    visited.add(elseSuccessor);
                    workList.add(elseSuccessor);
                }
            } else {
                assert cur instanceof SingleSuccessorBlock;
                Block b = ((SingleSuccessorBlock) cur).getSuccessor();
                if (b != null) {
                    sbDigraph.append(
                            addEdge(
                                    cur.getId(),
                                    b.getId(),
                                    ((SingleSuccessorBlock) cur).getFlowRule().name()));
                    if (!visited.contains(b)) {
                        visited.add(b);
                        workList.add(b);
                    }
                }
            }

            // exceptional edges
            if (cur.getType() == Block.BlockType.EXCEPTION_BLOCK) {
                ExceptionBlock ecur = (ExceptionBlock) cur;
                for (Map.Entry<TypeMirror, Set<Block>> e :
                        ecur.getExceptionalSuccessors().entrySet()) {
                    Set<Block> blocks = e.getValue();
                    TypeMirror cause = e.getKey();
                    String exception = cause.toString();
                    if (exception.startsWith("java.lang.")) {
                        exception = exception.replace("java.lang.", "");
                    }

                    for (Block b : blocks) {
                        sbDigraph.append(addEdge(cur.getId(), b.getId(), exception));
                        if (!visited.contains(b)) {
                            visited.add(b);
                            workList.add(b);
                        }
                    }
                }
            }

            cur = workList.poll();
        }

        sbDigraph.append(generateNodes(visited, cfg, analysis));

        // footer
        sbDigraph.append("}\n");

        return sbDigraph.toString();
    }

    protected abstract String generateNodes(
            Set<Block> visited, ControlFlowGraph cfg, @Nullable Analysis<A, S, T> analysis);

    protected String addEdge(long sId, long eId, String labelContent) {
        return "    " + sId + " -> " + eId + " [label=\"" + labelContent + "\"];\n";
    }

    protected String visualizeBlockHelper(
            Block bb,
            @Nullable Analysis<A, S, T> analysis,
            String footer1,
            String footer2,
            String escapeCharacter) {
        StringBuilder sbBlock = new StringBuilder();
        sbBlock.append(loopOverBlockContents(bb, analysis, escapeCharacter));

        // handle case where no contents are present
        boolean centered = false;
        if (sbBlock.length() == 0) {
            centered = true;
            if (bb.getType() == Block.BlockType.SPECIAL_BLOCK) {
                sbBlock.append(visualizeSpecialBlock((SpecialBlock) bb));
            } else if (bb.getType() == Block.BlockType.CONDITIONAL_BLOCK) {
                sbBlock.append(footer1);
                return sbBlock.toString();
            } else {
                sbBlock.append(footer2);
                return sbBlock.toString();
            }
        }

        // visualize transfer input if necessary
        if (analysis != null) {
            // the transfer input before this block is added before the block content
            sbBlock.insert(0, visualizeBlockTransferInput(bb, analysis));
            if (verbose) {
                Node lastNode = getLastNode(bb);
                if (lastNode != null) {
                    StringBuilder sbStore = new StringBuilder();
                    sbStore.append(escapeCharacter).append("~~~~~~~~~").append(escapeCharacter);
                    sbStore.append("After:[");
                    sbStore.append(visualizeStore(analysis.getResult().getStoreAfter(lastNode)));
                    sbStore.append("]");
                    sbBlock.append(sbStore);
                }
            }
        }

        sbBlock.append((centered ? "" : escapeCharacter));
        sbBlock.append(footer1);

        return sbBlock.toString();
    }

    protected String loopOverBlockContents(
            Block bb, @Nullable Analysis<A, S, T> analysis, String separator) {

        List<Node> contents = new ArrayList<>();
        StringBuilder sbBlockContents = new StringBuilder();
        boolean notFirst = false;

        switchBlockType(bb, contents);

        for (Node t : contents) {
            if (notFirst) {
                sbBlockContents.append(separator);
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

    protected String visualizeBlockTransferInputHelper(
            Block bb, Analysis<A, S, T> analysis, String escapeCharacter) {
        assert analysis != null
                : "analysis should be non-null when visualizing the transfer input of a block.";

        TransferInput<A, S> input = analysis.getInput(bb);
        assert input != null;

        StringBuilder sbStore = new StringBuilder();

        // split input representation to two lines
        sbStore.append("Before:");
        if (!input.containsTwoStores()) {
            S regularStore = input.getRegularStore();
            sbStore.append("[");
            sbStore.append(visualizeStore(regularStore));
            sbStore.append("]");
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
        sbStore.append(escapeCharacter).append("~~~~~~~~~").append(escapeCharacter);

        return sbStore.toString();
    }

    protected String visualizeSpecialBlockHelper(SpecialBlock sbb, String separator) {
        String specialBlock = "";
        switch (sbb.getSpecialType()) {
            case ENTRY:
                specialBlock = "<entry>" + separator;
                break;
            case EXIT:
                specialBlock = "<exit>" + separator;
                break;
            case EXCEPTIONAL_EXIT:
                specialBlock = "<exceptional-exit>" + separator;
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

    /**
     * Generate the order of processing blocks.
     *
     * @param cfg the current control flow graph
     */
    IdentityHashMap<Block, List<Integer>> getProcessOrder(ControlFlowGraph cfg) {
        IdentityHashMap<Block, List<Integer>> depthFirstOrder = new IdentityHashMap<>();
        int count = 1;
        for (Block b : cfg.getDepthFirstOrderedBlocks()) {
            depthFirstOrder.computeIfAbsent(b, k -> new ArrayList<>());
            depthFirstOrder.get(b).add(count++);
        }
        return depthFirstOrder;
    }

    @Override
    public String visualizeStore(S store) {
        return store.visualize(this);
    }

    @Override
    public String visualizeStoreFooter() {
        return ")";
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
}
