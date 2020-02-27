package org.checkerframework.dataflow.analysis;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import javax.lang.model.element.Element;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.block.SpecialBlock;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.ElementUtils;

/**
 * Common code base for {@link BackwardAnalysis} and {@link ForwardAnalysis}.
 *
 * @param <V> AbstractValue
 * @param <S> Store
 * @param <T> TransferFunction
 */
public abstract class AbstractAnalysis<
                V extends AbstractValue<V>, S extends Store<S>, T extends TransferFunction<V, S>>
        implements Analysis<V, S, T> {

    /** The direction of this analysis. */
    protected final Direction direction;

    /** Is the analysis currently running? */
    protected boolean isRunning = false;

    /** The transfer function for regular nodes. */
    // TODO: make final. Currently, the transferFunction has a reference to the analysis, so it
    //  can't be created until the Analysis is initialized.
    protected @Nullable T transferFunction;

    /** The current control flow graph to perform the analysis on. */
    protected @Nullable ControlFlowGraph cfg;

    /**
     * The transfer inputs of every basic block (assumed to be 'no information' if not present,
     * inputs before blocks in forward analysis, after blocks in backward analysis).
     */
    protected final IdentityHashMap<Block, TransferInput<V, S>> inputs;

    /** The worklist used for the fix-point iteration. */
    protected final Worklist worklist;

    /** Abstract values of nodes. */
    protected final IdentityHashMap<Node, V> nodeValues;

    /** Map from (effectively final) local variable elements to their abstract value. */
    protected final HashMap<Element, V> finalLocalValues;

    /**
     * The node that is currently handled in the analysis (if it is running). The following
     * invariant holds:
     *
     * <pre>
     *   !isRunning ==&gt; (currentNode == null)
     * </pre>
     */
    protected @Nullable Node currentNode;

    /**
     * The tree that is currently being looked at. The transfer function can set this tree to make
     * sure that calls to {@code getValue} will not return information for this given tree.
     */
    protected @Nullable Tree currentTree;

    /** The current transfer input when the analysis is running. */
    protected @Nullable TransferInput<V, S> currentInput;

    /**
     * @return the tree that is currently being looked at. The transfer function can set this tree
     *     to make sure that calls to {@code getValue} will not return information for this given
     *     tree.
     */
    public @Nullable Tree getCurrentTree() {
        return currentTree;
    }

    /**
     * Set the tree that is currently being looked at.
     *
     * @param currentTree the tree that should be currently looked at
     */
    public void setCurrentTree(Tree currentTree) {
        this.currentTree = currentTree;
    }

    /**
     * Common code base for {@link BackwardAnalysis} and {@link ForwardAnalysis}.
     *
     * @param direction direction of the analysis
     */
    public AbstractAnalysis(Direction direction) {
        this.direction = direction;
        this.inputs = new IdentityHashMap<>();
        this.worklist = new Worklist(this.direction);
        this.nodeValues = new IdentityHashMap<>();
        this.finalLocalValues = new HashMap<>();
    }

    /** Initialize the transfer inputs of every basic block before performing the analysis. */
    protected abstract void initInitialInputs();

    /**
     * Propagate the stores in currentInput to the next block in the direction of analysis,
     * according to the flowRule.
     */
    protected abstract void propagateStoresTo(
            Block nextBlock,
            Node node,
            TransferInput<V, S> currentInput,
            Store.FlowRule flowRule,
            boolean addToWorklistAgain);

    /**
     * Runs the analysis again within the block of {@code node} and returns the store at the
     * location of {@code node}. If {@code before} is true, then the store immediately before the
     * {@link Node} {@code node} is returned. Otherwise, the store after {@code node} is returned.
     * If {@code analysisCaches} is not null, this method uses a cache. {@code analysisCaches} is a
     * map of a block of node to the cached analysis result. If the cache for {@code transferInput}
     * is not in {@code analysisCaches}, this method create new cache and store it in {@code
     * analysisCaches}. The cache is a map of a node to the analysis result of the node.
     */
    protected abstract S runAnalysisFor(
            Node node,
            boolean before,
            TransferInput<V, S> transferInput,
            IdentityHashMap<Node, V> nodeValues,
            Map<TransferInput<V, S>, IdentityHashMap<Node, TransferResult<V, S>>> analysisCaches);

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public AnalysisResult<V, S> getResult() {
        if (isRunning) {
            throw new BugInCF(
                    "AbstractAnalysis::getResult() should not be called when analysis is running!");
        }
        assert cfg != null : "@AssumeAssertion(nullness): invariant";
        return new AnalysisResult<>(
                nodeValues,
                inputs,
                cfg.getTreeLookup(),
                cfg.getUnaryAssignNodeLookup(),
                finalLocalValues);
    }

    @Override
    public @Nullable T getTransferFunction() {
        return transferFunction;
    }

    @Override
    public @Nullable V getValue(Node n) {
        if (isRunning) {
            // We do not yet have a org.checkerframework.dataflow fact about the current node
            if (currentNode == null
                    || currentNode == n
                    || (currentTree != null && currentTree == n.getTree())) {
                return null;
            }
            // Check that 'n' is a subnode of 'node'. Check immediate operands
            // first for efficiency.
            assert !n.isLValue() : "Did not expect an lvalue, but got " + n;

            // Check that 'n' is a subnode of 'node'. Check immediate operands
            // first for efficiency.
            if (currentNode == n
                    || (!currentNode.getOperands().contains(n)
                            && !currentNode.getTransitiveOperands().contains(n))) {
                return null;
            }
            return nodeValues.get(n);
        }
        return nodeValues.get(n);
    }

    /** Return all current node values. */
    public IdentityHashMap<Node, V> getNodeValues() {
        return nodeValues;
    }

    /** Set all current node values to the given map. */
    /*package-private*/ void setNodeValues(IdentityHashMap<Node, V> in) {
        assert !isRunning;
        nodeValues.clear();
        nodeValues.putAll(in);
    }

    @Override
    public @Nullable S getRegularExitStore() {
        assert cfg != null : "@AssumeAssertion(nullness): invariant";
        SpecialBlock regularExitBlock = cfg.getRegularExitBlock();
        if (inputs.containsKey(regularExitBlock)) {
            return inputs.get(regularExitBlock).getRegularStore();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable S getExceptionalExitStore() {
        assert cfg != null : "@AssumeAssertion(nullness): invariant";
        SpecialBlock exceptionalExitBlock = cfg.getExceptionalExitBlock();
        if (inputs.containsKey(exceptionalExitBlock)) {
            return inputs.get(exceptionalExitBlock).getRegularStore();
        }
        return null;
    }

    /**
     * Get the set of {@link Node}s for a given {@link Tree}. Returns null for trees that don't
     * produce a value.
     */
    public @Nullable Set<Node> getNodesForTree(Tree t) {
        if (cfg == null) {
            return null;
        }
        return cfg.getNodesCorrespondingToTree(t);
    }

    /**
     * @param t a {@link Tree}
     * @return the abstract value for {@link Tree} {@code t}, or {@code null} if no information is
     *     available. Note that if the analysis has not finished yet, this value might not represent
     *     the final value for this node.
     */
    public @Nullable V getValue(Tree t) {
        // We do not yet have a org.checkerframework.dataflow fact about the current node
        if (t == currentTree) {
            return null;
        }
        Set<Node> nodesCorrespondingToTree = getNodesForTree(t);
        if (nodesCorrespondingToTree == null) {
            return null;
        }
        V merged = null;
        for (Node aNode : nodesCorrespondingToTree) {
            if (aNode.isLValue()) {
                return null;
            }
            V v = getValue(aNode);
            if (merged == null) {
                merged = v;
            } else if (v != null) {
                merged = merged.leastUpperBound(v);
            }
        }
        return merged;
    }

    /**
     * Get the {@link MethodTree} of the current CFG if the argument {@link Tree} maps to a {@link
     * Node} in the CFG or null otherwise.
     */
    public @Nullable MethodTree getContainingMethod(Tree t) {
        if (cfg == null) {
            return null;
        }
        MethodTree mt = cfg.getContainingMethod(t);
        return mt;
    }

    /**
     * Get the {@link ClassTree} of the current CFG if the argument {@link Tree} maps to a {@link
     * Node} in the CFG or null otherwise.
     */
    public @Nullable ClassTree getContainingClass(Tree t) {
        if (cfg == null) {
            return null;
        }
        ClassTree ct = cfg.getContainingClass(t);
        return ct;
    }

    /**
     * Call the transfer function for node {@code node}, and set that node as current node first.
     */
    protected TransferResult<V, S> callTransferFunction(Node node, TransferInput<V, S> store) {
        assert transferFunction != null : "@AssumeAssertion(nullness): invariant";
        if (node.isLValue()) {
            // TODO: should the default behavior return a regular transfer result, a conditional
            //  transfer result (depending on store.hasTwoStores()), or is the following correct?
            return new RegularTransferResult<>(null, store.getRegularStore());
        }
        store.node = node;
        currentNode = node;
        TransferResult<V, S> transferResult = node.accept(transferFunction, store);
        currentNode = null;
        if (node instanceof AssignmentNode) {
            // Store the flow-refined value effectively for final local variables
            AssignmentNode assignment = (AssignmentNode) node;
            Node lhst = assignment.getTarget();
            if (lhst instanceof LocalVariableNode) {
                LocalVariableNode lhs = (LocalVariableNode) lhst;
                Element elem = lhs.getElement();
                if (ElementUtils.isEffectivelyFinal(elem)) {
                    V resval = transferResult.getResultValue();
                    if (resval != null) {
                        finalLocalValues.put(elem, resval);
                    }
                }
            }
        }
        return transferResult;
    }

    /** Initialize the analysis with a new control flow graph. */
    protected final void init(ControlFlowGraph cfg) {
        initFields(cfg);
        initInitialInputs();
    }

    /**
     * Initialize class fields based on a given control flow graph. Sub-class may override this
     * method to initialize customized fields.
     *
     * @param cfg a given control flow graph
     */
    protected void initFields(ControlFlowGraph cfg) {
        inputs.clear();
        nodeValues.clear();
        finalLocalValues.clear();
        this.cfg = cfg;
    }

    /**
     * Updates the value of node {@code node} to the value of the {@code transferResult}. Returns
     * true if the node's value changed, or a store was updated.
     */
    protected boolean updateNodeValues(Node node, TransferResult<V, S> transferResult) {
        V newVal = transferResult.getResultValue();
        boolean nodeValueChanged = false;

        if (newVal != null) {
            V oldVal = nodeValues.get(node);
            nodeValues.put(node, newVal);
            nodeValueChanged = !Objects.equals(oldVal, newVal);
        }

        return nodeValueChanged || transferResult.storeChanged();
    }

    /**
     * Read the store for a particular basic block from a map of stores (or {@code null} if none
     * exists yet).
     */
    protected static <S> @Nullable S readFromStore(Map<Block, S> stores, Block b) {
        return stores.get(b);
    }

    /**
     * Add a basic block to the Worklist. If {@code b} is already present, the method does nothing.
     */
    protected void addToWorklist(Block b) {
        // TODO: use a more efficient way to check if b is already present
        if (!worklist.contains(b)) {
            worklist.add(b);
        }
    }

    /**
     * A worklist is a priority queue of blocks in which the order is given by depth-first ordering
     * to place non-loop predecessors ahead of successors.
     */
    protected static class Worklist {

        /** Map all blocks in the CFG to their depth-first order. */
        protected final IdentityHashMap<Block, Integer> depthFirstOrder;

        /**
         * Comparators to allow priority queue to order blocks by their depth-first order, using by
         * forward analysis.
         */
        public class ForwardDFOComparator implements Comparator<Block> {
            @Override
            public int compare(Block b1, Block b2) {
                return depthFirstOrder.get(b1) - depthFirstOrder.get(b2);
            }
        }

        /**
         * Comparators to allow priority queue to order blocks by their depth-first order, using by
         * backward analysis.
         */
        public class BackwardDFOComparator implements Comparator<Block> {
            @Override
            public int compare(Block b1, Block b2) {
                return depthFirstOrder.get(b2) - depthFirstOrder.get(b1);
            }
        }

        /** The backing priority queue. */
        protected final PriorityQueue<Block> queue;

        /** The work list. */
        public Worklist(Direction direction) {
            depthFirstOrder = new IdentityHashMap<>();

            if (direction == Direction.FORWARD) {
                queue = new PriorityQueue<>(11, new ForwardDFOComparator());
            } else if (direction == Direction.BACKWARD) {
                queue = new PriorityQueue<>(11, new BackwardDFOComparator());
            } else {
                throw new BugInCF("Unexpected Direction meet: " + direction.name());
            }
        }

        /** Process the control flow graph, add the Blocks to {@link #depthFirstOrder}. */
        public void process(ControlFlowGraph cfg) {
            assert cfg != null : "@AssumeAssertion(nullness): invariant";
            depthFirstOrder.clear();
            int count = 1;
            for (Block b : cfg.getDepthFirstOrderedBlocks()) {
                depthFirstOrder.put(b, count++);
            }

            queue.clear();
        }

        /** @see PriorityQueue#isEmpty */
        @EnsuresNonNullIf(result = false, expression = "poll()")
        @SuppressWarnings("nullness:contracts.conditional.postcondition.not.satisfied") // forwarded
        public boolean isEmpty() {
            return queue.isEmpty();
        }

        /** Check if {@link #queue} contains the block which is passed as the argument. */
        public boolean contains(Block block) {
            return queue.contains(block);
        }

        /** Add Block to {@link #queue}. */
        public void add(Block block) {
            queue.add(block);
        }

        /** @see PriorityQueue#poll */
        public @Nullable Block poll() {
            return queue.poll();
        }

        @Override
        public String toString() {
            return "Worklist(" + queue + ")";
        }
    }
}