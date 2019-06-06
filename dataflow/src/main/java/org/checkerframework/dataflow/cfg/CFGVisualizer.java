package org.checkerframework.dataflow.cfg;

import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.AbstractValue;
import org.checkerframework.dataflow.analysis.Analysis;
import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.analysis.Store;
import org.checkerframework.dataflow.analysis.TransferFunction;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.block.SpecialBlock;
import org.checkerframework.dataflow.cfg.node.Node;

/**
 * Perform some visualization on a control flow graph. The particular operations depend on the
 * implementation.
 */
public interface CFGVisualizer<
        A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>> {
    /**
     * Initialization method guaranteed to be called once before the first invocation of {@link
     * #visualize}.
     *
     * @param args Implementation-dependent options.
     */
    void init(Map<String, Object> args);

    /**
     * Output a visualization representing the control flow graph starting at {@code entry}. The
     * concrete actions are implementation dependent.
     *
     * <p>An invocation {@code visualize(cfg, entry, null);} does not output stores at the beginning
     * of basic blocks.
     *
     * @param cfg The CFG to visualize.
     * @param entry The entry node of the control flow graph to be represented.
     * @param analysis The analysis containing information about the program represented by the CFG.
     *     The information includes {@link Store}s that are valid at the beginning of basic blocks
     *     reachable from {@code entry} and per-node information for value producing {@link Node}s.
     *     Can also be {@code null} to indicate that this information should not be output.
     * @return Possible analysis results, e.g. generated file names({@link DOTCFGVisualizer}) and
     *     String representation of CFG({@link StringCFGVisualizer}).
     */
    @Nullable Map<String, Object> visualize(
            ControlFlowGraph cfg, Block entry, @Nullable Analysis<A, S, T> analysis);

    /**
     * Delegate the visualization responsibility to the passed {@link Store} instance, which will
     * call back to this visualizer instance for sub-components.
     *
     * @param store The Store to visualize.
     * @return The String presentation of the value of Store.
     */
    String visualizeStore(S store);

    /**
     * Called by a {@code CFAbstractStore} to visualize the class name before calling the {@code
     * CFAbstractStore#internalVisualize()} method.
     *
     * @param classCanonicalName The canonical name of the class.
     * @return The String representation of the header of Store.
     */
    String visualizeStoreHeader(String classCanonicalName);

    /**
     * Called by {@code CFAbstractStore#internalVisualize()} to visualize a local variable.
     *
     * @param localVar The local variable.
     * @param value The value of the local variable.
     * @return The String representation of the value of a local variable.
     */
    String visualizeStoreLocalVar(FlowExpressions.LocalVariable localVar, A value);

    /**
     * Called by {@code CFAbstractStore#internalVisualize()} to visualize the value of the current
     * object {@code this} in this Store.
     *
     * @param value The value of the current object this.
     * @return The String representation of the value of current object this.
     */
    String visualizeStoreThisVal(A value);

    /**
     * Called by {@code CFAbstractStore#internalVisualize()} to visualize the value of fields
     * collected by this Store.
     *
     * @param fieldAccess The field.
     * @param value The value of the field.
     * @return The String representation of the value of fields of this Store.
     */
    String visualizeStoreFieldVals(FlowExpressions.FieldAccess fieldAccess, A value);

    /**
     * Called by {@code CFAbstractStore#internalVisualize()} to visualize the value of arrays
     * collected by this Store.
     *
     * @param arrayValue The array.
     * @param value The value of the array.
     * @return The String representation of the value of arrays of this Store.
     */
    String visualizeStoreArrayVal(FlowExpressions.ArrayAccess arrayValue, A value);

    /**
     * Called by {@code CFAbstractStore#internalVisualize()} to visualize the value of pure method
     * calls collected by this Store.
     *
     * @param methodCall The pure method call.
     * @param value The value of the pure method call.
     * @return The String representation of the value of pure method calls of this Store.
     */
    String visualizeStoreMethodVals(FlowExpressions.MethodCall methodCall, A value);

    /**
     * Called by {@code CFAbstractStore#internalVisualize()} to visualize the value of class names
     * collected by this Store.
     *
     * @param className The class name.
     * @param value The value of the class name.
     * @return The String representation of the value of class names of this Store.
     */
    String visualizeStoreClassVals(FlowExpressions.ClassName className, A value);

    /**
     * Called by {@code CFAbstractStore#internalVisualize()} to visualize the specific information
     * collected according to the specific kind of Store. Currently, these Stores call this method:
     * {@code LockStore}, {@code NullnessStore}, and {@code InitializationStore} to visualize
     * additional information.
     *
     * @param keyName The name of the specific information to be visualized.
     * @param value The value of the specific information to be visualized.
     * @return The String representation of the specific information.
     */
    String visualizeStoreKeyVal(String keyName, Object value);

    /**
     * Called by {@code CFAbstractStore} to visualize any information after the invocation of {@code
     * CFAbstractStore#internalVisualize()}.
     *
     * @return The String representation of the value of footer of this Store.
     */
    String visualizeStoreFooter();

    /**
     * Visualize a Block based on the analysis.
     *
     * @param bb The Block.
     * @param analysis The current analysis.
     * @return The String representation of this Block.
     */
    String visualizeBlock(Block bb, @Nullable Analysis<A, S, T> analysis);

    /**
     * Visualize a SpecialBlock.
     *
     * @param sbb The special Block.
     * @return The String representation of the type of this special Block(entry, exit or
     *     exceptional-exit).
     */
    String visualizeSpecialBlock(SpecialBlock sbb);

    /**
     * Visualize the transferInput of a Block based on the analysis.
     *
     * @param bb The block.
     * @param analysis The current analysis.
     * @return The String representation of the transferInput of this Block.
     */
    String visualizeBlockTransferInput(Block bb, Analysis<A, S, T> analysis);

    /**
     * Visualize a Node based on the analysis.
     *
     * @param t The node.
     * @param analysis The current analysis.
     * @return The String representation of this node.
     */
    String visualizeBlockNode(Node t, @Nullable Analysis<A, S, T> analysis);

    /** Shutdown method called once from the shutdown hook of the {@code BaseTypeChecker}. */
    void shutdown();
}
