package org.checkerframework.dataflow.cfg;

import java.util.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.AbstractValue;
import org.checkerframework.dataflow.analysis.Analysis;
import org.checkerframework.dataflow.analysis.Store;
import org.checkerframework.dataflow.analysis.TransferFunction;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.node.Node;

public class StringCFGVisualizer<
                A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
        extends AbstractCFGVisualizer<A, S, T> {

    @Override
    public void init(Map<String, Object> args) {
        super.init(args);
    }

    @Override
    public @Nullable Map<String, Object> visualize(
            ControlFlowGraph cfg, Block entry, @Nullable Analysis<A, S, T> analysis) {
        Set<Block> blocks = cfg.getAllBlocks();
        StringBuilder sbAllBlocks = new StringBuilder();
        for (Block eachBlock : blocks) {
            sbAllBlocks.append(eachBlock.toString()).append("\n");
        }
        Map<String, Object> res = new HashMap<>();
        res.put("allBlocks", sbAllBlocks.toString());
        return res;
    }

    @Override
    public @Nullable String visualizeBlock(Block bb, @Nullable Analysis<A, S, T> analysis) {

        this.sbBlock.setLength(0);

        List<Node> contents = new ArrayList<>();
        loopOverContents(bb, contents, analysis);

        if (analysis != null) {
            visualizeBlockTransferInput(bb, analysis);
        }
        return this.sbBlock.toString();
    }

    /** StringCFGVisualizer does not write into file, so leave it blank. */
    @Override
    public void shutdown() {}
}
