package org.checkerframework.dataflow.cfg;

import java.util.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.*;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.block.SpecialBlock;

/** Generate a String version of a control flow graph. */
public class StringCFGVisualizer<
                A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
        extends AbstractCFGVisualizer<A, S, T> {

    private final String lineSeparator = System.lineSeparator();
    private final String escapeCharacter = "\n";

    @Override
    public void init(Map<String, Object> args) {
        super.init(args);
    }

    @Override
    public Map<String, Object> visualize(
            ControlFlowGraph cfg, Block entry, @Nullable Analysis<A, S, T> analysis) {
        String stringGraph = generateStringGraph(cfg, entry, analysis);

        Map<String, Object> res = new HashMap<>();
        res.put("stringGraph", stringGraph);

        return res;
    }

    private String generateStringGraph(
            ControlFlowGraph cfg, Block entry, @Nullable Analysis<A, S, T> analysis) {
        return super.generateGraphHelper(cfg, entry, analysis, "then ", "else ");
    }

    @Override
    public String generateNodes(
            Set<Block> visited, ControlFlowGraph cfg, @Nullable Analysis<A, S, T> analysis) {
        StringBuilder sbDotNodes = new StringBuilder();
        sbDotNodes.append("\n");

        IdentityHashMap<Block, List<Integer>> processOrder = getProcessOrder(cfg);

        // definition of all nodes including their labels
        for (Block v : visited) {
            sbDotNodes.append(v.getId()).append(":\n");
            if (verbose) {
                sbDotNodes
                        .append("Process order: ")
                        .append(processOrder.get(v).toString().replaceAll("[\\[\\]]", ""))
                        .append("\n");
            }
            sbDotNodes.append(visualizeBlock(v, analysis));
        }

        return sbDotNodes.toString();
    }

    @Override
    public @Nullable String visualizeBlock(Block bb, @Nullable Analysis<A, S, T> analysis) {
        return super.visualizeBlockHelper(bb, analysis, "\n", "\n", escapeCharacter);
    }

    @Override
    public String visualizeSpecialBlock(SpecialBlock sbb) {
        return super.visualizeSpecialBlockHelper(sbb, lineSeparator);
    }

    @Override
    public String visualizeBlockTransferInput(Block bb, Analysis<A, S, T> analysis) {
        return super.visualizeBlockTransferInputHelper(bb, analysis, escapeCharacter);
    }

    @Override
    public String visualizeStoreThisVal(A value) {
        return "this > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreLocalVar(FlowExpressions.LocalVariable localVar, A value) {
        return localVar + " > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreFieldVals(FlowExpressions.FieldAccess fieldAccess, A value) {
        return fieldAccess + " > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreArrayVal(FlowExpressions.ArrayAccess arrayValue, A value) {
        return arrayValue + " > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreMethodVals(FlowExpressions.MethodCall methodCall, A value) {
        return methodCall + " > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreClassVals(FlowExpressions.ClassName className, A value) {
        return className + " > " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreKeyVal(String keyName, Object value) {
        return keyName + " = " + value + lineSeparator;
    }

    @Override
    public String visualizeStoreHeader(String classCanonicalName) {
        return classCanonicalName + " (" + lineSeparator;
    }

    /** StringCFGVisualizer does not write into file, so left intentionally blank. */
    @Override
    public void shutdown() {}
}
