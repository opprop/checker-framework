package org.checkerframework.dataflow.cfg;

import java.util.Map;
import org.checkerframework.dataflow.analysis.*;
import org.checkerframework.dataflow.cfg.block.Block;

/**
 * An abstract class of control flow graph visualizer. It provides the methods to generate a control
 * flow graph in the language of DOT. To achieve this abstract class, you need to override {@link
 * #init(Map)}, {@link #visualize(ControlFlowGraph, Block, Analysis)}, {@link
 * #visualizeStore(Store)} and {@link #shutdown()}.
 *
 * <p>Examples: {@link DOTCFGVisualizer} and {@link StringCFGVisualizer}.
 */
public abstract class AbstractCFGVisualizer<
                A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
        implements CFGVisualizer<A, S, T> {}
