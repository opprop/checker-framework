package org.checkerframework.dataflow.cfg;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.AbstractValue;
import org.checkerframework.dataflow.analysis.Analysis;
import org.checkerframework.dataflow.analysis.Store;
import org.checkerframework.dataflow.analysis.TransferFunction;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.block.ConditionalBlock;
import org.checkerframework.dataflow.cfg.block.ExceptionBlock;
import org.checkerframework.dataflow.cfg.block.RegularBlock;
import org.checkerframework.dataflow.cfg.block.SingleSuccessorBlock;
import org.checkerframework.dataflow.cfg.block.SpecialBlock;
import org.checkerframework.dataflow.cfg.node.Node;

/**
 * An abstract class of control flow graph visualizer. To achieve this abstract class, you need to
 * implement some of the methods in {@link CFGVisualizer}. Some of the other necessary methods in
 * {@link CFGVisualizer} has already been implemented in this abstract class, override them if
 * necessary.
 *
 * <p>There are some helper methods in the class to make building custom CFGVisualizer easier.
 *
 * <p>Two Examples of the implementation of {@link AbstractCFGVisualizer}: {@link DOTCFGVisualizer}
 * and {@link StringCFGVisualizer}.
 */
public abstract class AbstractCFGVisualizer<
                A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
        implements CFGVisualizer<A, S, T> {

    /**
     * Initialized in {@link #init(Map)}. If its value is {@code true}, {@link CFGVisualizer} will
     * return more detailed information.
     */
    protected boolean verbose;

    @Override
    public void init(Map<String, Object> args) {
        Object verb = args.get("verbose");
        this.verbose =
                verb != null
                        && (verb instanceof String
                                ? Boolean.getBoolean((String) verb)
                                : (boolean) verb);
    }

    /**
     * Helper method to simplify generating a control flow graph, it will be useful when
     * implementing custom CFGVisualizer.
     *
     * @param cfg The control flow graph.
     * @param entry The entry {@link Block}.
     * @param analysis The current analysis.
     * @return The String representation of the control flow graph.
     */
    protected String generateGraphHelper(
            ControlFlowGraph cfg, Block entry, @Nullable Analysis<A, S, T> analysis) {
        Set<Block> visited = new HashSet<>();
        StringBuilder sbDigraph = new StringBuilder();
        Queue<Block> workList = new ArrayDeque<>();

        visited.add(entry);
        visited = traverseControlFlowGraphHelper(visited, entry, workList, sbDigraph);
        sbDigraph.append(generateNodes(visited, cfg, analysis));

        return sbDigraph.toString();
    }

    /**
     * Traverse control flow graph and define all arrows.
     *
     * @param visited The set to store the visited {@link Block}s.
     * @param cur The current conditional {@link Block}.
     * @param workList The working queue.
     * @param sbDigraph The digraph StringBuilder.
     * @return The set that contains all the visited {@link Block}s.
     */
    protected Set<Block> traverseControlFlowGraphHelper(
            Set<Block> visited, Block cur, Queue<Block> workList, StringBuilder sbDigraph) {
        while (cur != null) {
            if (cur.getType() == Block.BlockType.CONDITIONAL_BLOCK) {
                ConditionalBlock ccur = ((ConditionalBlock) cur);
                Block thenSuccessor = ccur.getThenSuccessor();
                sbDigraph.append(
                        addEdge(
                                ccur.getId(),
                                thenSuccessor.getId(),
                                ccur.getThenFlowRule().toString()));
                if (!visited.contains(thenSuccessor)) {
                    visited.add(thenSuccessor);
                    workList.add(thenSuccessor);
                }
                Block elseSuccessor = ccur.getElseSuccessor();
                sbDigraph.append(
                        addEdge(
                                ccur.getId(),
                                elseSuccessor.getId(),
                                ccur.getElseFlowRule().toString()));
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
        return visited;
    }

    /**
     * This is an abstract method which aims to generate a String representation of the nodes of a
     * control flow graph.
     *
     * <p>It needs to be implemented.
     *
     * @param visited The set of the visited {@link Block}s.
     * @param cfg The control flow graph.
     * @param analysis The current analysis.
     * @return The String representation of the {@link Node}s.
     */
    protected abstract String generateNodes(
            Set<Block> visited, ControlFlowGraph cfg, @Nullable Analysis<A, S, T> analysis);

    /**
     * This is an abstract method which aims to generate a String representation of the edge.
     *
     * <p>It needs to be implemented.
     *
     * @param sId The ID of current {@link Block}.
     * @param eId The ID of successor {@link Block}.
     * @param flowRule The content of the edge.
     * @return The String representation of the edge.
     */
    protected abstract String addEdge(long sId, long eId, String flowRule);

    /**
     * Helper method to simplify visualizing a {@link Block}.
     *
     * @param bb The {@link Block}.
     * @param analysis The current analysis.
     * @param cbFooter Footer for conditional {@link Block}.
     * @param osFooter Footer for the other situations.
     * @param escapeCharacter The specific escape character that we want to use(It is necessary for
     *     {@link DOTCFGVisualizer}). For example, "\\l" in {@link DOTCFGVisualizer}.
     * @return The String representation of the {@link Block}.
     */
    protected String visualizeBlockHelper(
            Block bb,
            @Nullable Analysis<A, S, T> analysis,
            String cbFooter,
            String osFooter,
            String escapeCharacter) {
        StringBuilder sbBlock = new StringBuilder();
        sbBlock.append(loopOverBlockContents(bb, analysis, escapeCharacter));

        // Handle case where no contents are present
        boolean notCentered = true;
        if (sbBlock.length() == 0) {
            notCentered = false;
            if (bb.getType() == Block.BlockType.SPECIAL_BLOCK) {
                sbBlock.append(visualizeSpecialBlock((SpecialBlock) bb));
            } else if (bb.getType() == Block.BlockType.CONDITIONAL_BLOCK) {
                sbBlock.append(cbFooter);
                return sbBlock.toString();
            } else {
                sbBlock.append(osFooter);
                return sbBlock.toString();
            }
        }

        // Visualize transfer input if necessary
        if (analysis != null) {
            // The transfer input before this block is added before the block content
            sbBlock.insert(0, visualizeBlockTransferInput(bb, analysis));
            if (verbose) {
                Node lastNode = getLastNode(bb);
                if (lastNode != null) {
                    StringBuilder sbStore = new StringBuilder();
                    sbStore.append(escapeCharacter).append("~~~~~~~~~").append(escapeCharacter);
                    sbStore.append("After:");
                    sbStore.append(visualizeStore(analysis.getResult().getStoreAfter(lastNode)));
                    sbBlock.append(sbStore);
                }
            }
        }
        if (notCentered) {
            sbBlock.append(escapeCharacter);
        }
        sbBlock.append(cbFooter);
        return sbBlock.toString();
    }

    /**
     * Called by {@link #visualizeBlockHelper}, iterate over {@code contents} and visualize all the
     * {@link Node}s in it.
     *
     * @param bb The {@link Block}.
     * @param analysis The current analysis.
     * @param separator The separator character that we want to use.
     * @return The String representation of the contents of the {@link Block}.
     */
    protected String loopOverBlockContents(
            Block bb, @Nullable Analysis<A, S, T> analysis, String separator) {

        List<Node> contents = new ArrayList<>();
        StringBuilder sbBlockContents = new StringBuilder();
        boolean notFirst = false;

        addBlockContent(bb, contents);

        for (Node t : contents) {
            if (notFirst) {
                sbBlockContents.append(separator);
            }
            notFirst = true;
            sbBlockContents.append(visualizeBlockNode(t, analysis));
        }
        return sbBlockContents.toString();
    }

    /**
     * Called by {@link #loopOverBlockContents}. If possible, add a sequence of {@link Node}s to
     * {@code contents} for further processing.
     *
     * @param bb The {@link Block}.
     * @param contents An empty list which will store {@link Node}s.
     */
    protected void addBlockContent(Block bb, List<Node> contents) {
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

    /**
     * Helper method to simplify visualizing the tranferinput of a {@link Block}, it is useful when
     * implementing custom CFGVisualizer.
     *
     * @param bb The {@link Block}.
     * @param analysis The current analysis.
     * @param escapeCharacter The specific escape character that we want to use(It is necessary for
     *     {@link DOTCFGVisualizer}). For example, "\\l" in {@link DOTCFGVisualizer}.
     * @param leftBracket The specific left bracket that we want to use.
     * @param rightBracket The specific right bracket that we want to use.
     * @return The String representation of the tranferinput of a {@link Block}.
     */
    protected String visualizeBlockTransferInputHelper(
            Block bb,
            Analysis<A, S, T> analysis,
            String escapeCharacter,
            String leftBracket,
            String rightBracket) {
        assert analysis != null
                : "analysis should be non-null when visualizing the transfer input of a block.";

        TransferInput<A, S> input = analysis.getInput(bb);
        assert input != null;

        StringBuilder sbStore = new StringBuilder();

        // split input representation to two lines
        sbStore.append("Before:");
        if (!input.containsTwoStores()) {
            S regularStore = input.getRegularStore();
            sbStore.append(leftBracket);
            sbStore.append(visualizeStore(regularStore));
            sbStore.append(rightBracket);
        } else {
            S thenStore = input.getThenStore();
            sbStore.append(leftBracket).append("then=");
            sbStore.append(visualizeStore(thenStore));
            S elseStore = input.getElseStore();
            sbStore.append(", else=");
            sbStore.append(visualizeStore(elseStore));
            sbStore.append(rightBracket);
        }
        sbStore.append(escapeCharacter).append("~~~~~~~~~").append(escapeCharacter);
        return sbStore.toString();
    }

    /**
     * Helper method to simplify visualizing the special Block, it is useful when implementing
     * custom CFGVisualizer.
     *
     * @param sbb The special {@link Block}.
     * @param separator The specific separator character that we want to use.
     * @return The String representation of the special {@link Block}.
     */
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

    /**
     * Called by {@link #visualizeBlockHelper}. If possible, get the last {@link Node}.
     *
     * @param bb The {@link Block}.
     * @return The last {@link Node} or {@code null}.
     */
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

    /**
     * Generate the order of processing {@link Block}s.
     *
     * @param cfg The current control flow graph.
     * @return The IdentityHashMap which maps from {@link Block}s to their orders.
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
}
