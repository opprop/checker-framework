package org.checkerframework.dataflow.analysis;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.node.Node;

/**
 * General Dataflow Analysis Interface This interface defines general behaviors of a data-flow
 * analysis, given a control flow graph and a transfer function. A data-flow analysis should only
 * has one direction, either forward or backward. The direction of corresponding transfer function
 * should be consistent with the analysis, i.e. a forward analysis should be given a forward
 * transfer function, and a backward analysis should be given a backward transfer function.
 *
 * @author charleszhuochen
 * @author Stefan Heule
 * @param <V> The abstract value type to be tracked by the analysis
 * @param <S> The store type used in the analysis.
 * @param <T> The transfer function type that is used to approximated runtime behavior
 */
public interface Analysis<
        V extends AbstractValue<V>, S extends Store<S>, T extends TransferFunction<V, S>> {

    public static enum Direction {
        FORWARD,
        BACKWARD
    }

    Direction getDirection();

    boolean isRunning();

    void performAnalysis(ControlFlowGraph cfg);

    AnalysisResult<V, S> getResult();

    T getTransferFunction();

    void setTransferFunction(T transferFunction);

    Tree getCurrentTree();

    void setCurrentTree(Tree t);

    TransferInput<V, S> getInput(Block b);

    V getValue(Node n);

    V getValue(Tree t);

    Node getNodeForTree(Tree t);

    MethodTree getContainingMethod(Tree t);

    ClassTree getContainingClass(Tree t);

    S getRegularExitStore();

    S getExceptionalExitStore();

    /**
     * Runs the analysis again within the block of {@code node} and returns the store at the
     * location of {@code node}. If {@code before} is true, then the store immediately before the
     * {@link Node} {@code node} is returned. Otherwise, the store after {@code node} is returned.
     */
    S runAnalysisFor(Node node, boolean before, TransferInput<V, S> transferInput);
}
