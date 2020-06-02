package org.checkerframework.dataflow.analysis;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.node.Node;

/**
 * This interface defines a dataflow analysis, given a control flow graph and a backward transfer
 * function. A dataflow analysis has a direction, either forward or backward. The direction of
 * corresponding transfer function is consistent with the analysis, i.e. a forward analysis has a
 * forward transfer function, and a backward analysis has a backward transfer function.
 *
 * @param <V> the abstract value type to be tracked by the analysis
 * @param <S> the store type used in the analysis
 * @param <T> the transfer function type that is used to approximated runtime behavior
 */
public interface Analysis<
        V extends AbstractValue<V>, S extends Store<S>, T extends TransferFunction<V, S>> {

    /** The direction of an analysis instance. */
    enum Direction {
        /** The forward direction. */
        FORWARD,
        /** The backward direction. */
        BACKWARD
    }

    /**
     * Get the direction of this analysis.
     *
     * @return the direction of this analysis
     */
    Direction getDirection();

    /**
     * Is the analysis currently running?
     *
     * @return true if the analysis is running currently, else false
     */
    boolean isRunning();

    /**
     * Perform the actual analysis.
     *
     * @param cfg the control flow graph
     */
    void performAnalysis(ControlFlowGraph cfg);

    /**
     * Perform the actual analysis on one block.
     *
     * @param b the block to analyze
     */
    void performAnalysisBlock(Block b);

    /**
     * The result of running the analysis. This is only available once the analysis finished
     * running.
     *
     * @return the result of running the analysis
     */
    AnalysisResult<V, S> getResult();

    /**
     * Get the transfer function of this analysis.
     *
     * @return the transfer function of this analysis
     */
    @Nullable T getTransferFunction();

    /**
     * Get the transfer input of a given {@link Block} b.
     *
     * @param b a given Block
     * @return the transfer input of this Block
     */
    @Nullable TransferInput<V, S> getInput(Block b);

    /**
     * @param n a {@link Node}
     * @return the abstract value for {@link Node} {@code n}, or {@code null} if no information is
     *     available. Note that if the analysis has not finished yet, this value might not represent
     *     the final value for this node.
     */
    @Nullable V getValue(Node n);

    /**
     * Get the regular exit store of this analysis.
     *
     * @return the regular exit store, or {@code null} if there is no such store (because the method
     *     cannot exit through the regular exit block).
     */
    @Nullable S getRegularExitStore();

    /**
     * Get the exceptional exit store of this analysis.
     *
     * @return the exceptional exit store, or {@code null} if there is no such store
     */
    @Nullable S getExceptionalExitStore();
}
