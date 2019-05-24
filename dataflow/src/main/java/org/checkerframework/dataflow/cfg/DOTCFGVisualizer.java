package org.checkerframework.dataflow.cfg;

import com.sun.tools.javac.tree.JCTree;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.*;
import org.checkerframework.dataflow.cfg.UnderlyingAST.CFGMethod;
import org.checkerframework.dataflow.cfg.UnderlyingAST.CFGStatement;
import org.checkerframework.dataflow.cfg.block.*;
import org.checkerframework.dataflow.cfg.block.Block.BlockType;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.UserError;

/** Generate a graph description in the DOT language of a control graph. */
public class DOTCFGVisualizer<
                A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
        extends AbstractCFGVisualizer<A, S, T> {

    private String outDir;
    private String checkerName;

    /** Mapping from class/method representation to generated dot file. */
    protected Map<String, String> generated;

    @Override
    public void init(Map<String, Object> args) {
        super.init(args);
        this.outDir = (String) args.get("outdir");
        this.checkerName = (String) args.get("checkerName");
        this.generated = new HashMap<>();
    }

    @Override
    public @Nullable Map<String, Object> visualize(
            ControlFlowGraph cfg, Block entry, @Nullable Analysis<A, S, T> analysis) {

        String dotGraph = generateDotGraph(cfg, entry, analysis);
        String dotFileName = dotOutputFileName(cfg.underlyingAST);

        try {
            FileWriter fStream = new FileWriter(dotFileName);
            BufferedWriter out = new BufferedWriter(fStream);
            out.write(dotGraph);
            out.close();
        } catch (IOException e) {
            throw new UserError(
                    "Error creating dot file: " + dotFileName + "; ensure the path is valid", e);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("dotFileName", dotFileName);

        return res;
    }

    /**
     * Generate the dot representation as String.
     *
     * @param cfg the current control flow graph
     * @param entry the entry block of the control flow graph
     * @param analysis the current analysis
     */
    private String generateDotGraph(
            ControlFlowGraph cfg, Block entry, @Nullable Analysis<A, S, T> analysis) {
        Set<Block> visited = new HashSet<>();

        StringBuilder sbDigraph = new StringBuilder();
        sbDigraph.append("digraph {\n");

        Block cur = entry;
        Queue<Block> workList = new ArrayDeque<>();
        visited.add(entry);
        // traverse control flow graph and define all arrows
        while (cur != null) {
            if (cur.getType() == BlockType.CONDITIONAL_BLOCK) {
                ConditionalBlock ccur = ((ConditionalBlock) cur);
                Block thenSuccessor = ccur.getThenSuccessor();
                sbDigraph.append(
                        addDotEdge(
                                ccur.getId(),
                                thenSuccessor.getId(),
                                "then\\n" + ccur.getThenFlowRule()));
                if (!visited.contains(thenSuccessor)) {
                    visited.add(thenSuccessor);
                    workList.add(thenSuccessor);
                }
                Block elseSuccessor = ccur.getElseSuccessor();
                sbDigraph.append(
                        addDotEdge(
                                ccur.getId(),
                                elseSuccessor.getId(),
                                "else\\n" + ccur.getElseFlowRule()));
                if (!visited.contains(elseSuccessor)) {
                    visited.add(elseSuccessor);
                    workList.add(elseSuccessor);
                }
            } else {
                assert cur instanceof SingleSuccessorBlock;
                Block b = ((SingleSuccessorBlock) cur).getSuccessor();
                if (b != null) {
                    sbDigraph.append(
                            addDotEdge(
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
            if (cur.getType() == BlockType.EXCEPTION_BLOCK) {
                ExceptionBlock ecur = (ExceptionBlock) cur;
                for (Entry<TypeMirror, Set<Block>> e : ecur.getExceptionalSuccessors().entrySet()) {
                    Set<Block> blocks = e.getValue();
                    TypeMirror cause = e.getKey();
                    String exception = cause.toString();
                    if (exception.startsWith("java.lang.")) {
                        exception = exception.replace("java.lang.", "");
                    }

                    for (Block b : blocks) {
                        sbDigraph.append(addDotEdge(cur.getId(), b.getId(), exception));
                        if (!visited.contains(b)) {
                            visited.add(b);
                            workList.add(b);
                        }
                    }
                }
            }

            cur = workList.poll();
        }

        sbDigraph.append(generateDotNodes(visited, cfg, analysis));

        // footer
        sbDigraph.append("}\n");

        return sbDigraph.toString();
    }

    /**
     * Generate the nodes of control flow graph as String.
     *
     * @param visited a set of blocks
     * @param cfg the current control flow graph
     * @param analysis the current analysis
     */
    private String generateDotNodes(
            Set<Block> visited, ControlFlowGraph cfg, @Nullable Analysis<A, S, T> analysis) {

        StringBuilder sbDotNodes = new StringBuilder();
        sbDotNodes.append("    node [shape=rectangle];\n\n");

        IdentityHashMap<Block, List<Integer>> processOrder = getProcessOrder(cfg);

        // definition of all nodes including their labels
        for (Block v : visited) {
            sbDotNodes.append("    ").append(v.getId()).append(" [");
            if (v.getType() == BlockType.CONDITIONAL_BLOCK) {
                sbDotNodes.append("shape=polygon sides=8 ");
            } else if (v.getType() == BlockType.SPECIAL_BLOCK) {
                sbDotNodes.append("shape=oval ");
            }
            sbDotNodes.append("label=\"");
            if (verbose) {
                sbDotNodes
                        .append("Process order: ")
                        .append(processOrder.get(v).toString().replaceAll("[\\[\\]]", ""))
                        .append("\\n");
            }
            sbDotNodes.append(visualizeBlock(v, analysis));
        }

        sbDotNodes.append("\n");
        return sbDotNodes.toString();
    }

    /**
     * Create the name of dot file.
     *
     * @param ast an abstract syntax tree
     * @return the file name used for DOT output
     */
    private String dotOutputFileName(UnderlyingAST ast) {
        StringBuilder srcLoc = new StringBuilder();
        StringBuilder outFile = new StringBuilder(outDir);

        outFile.append('/');

        if (ast.getKind() == UnderlyingAST.Kind.ARBITRARY_CODE) {
            CFGStatement cfgStatement = (CFGStatement) ast;
            String clsName = cfgStatement.getClassTree().getSimpleName().toString();
            outFile.append(clsName);
            outFile.append("-initializer-");
            outFile.append(ast.hashCode());

            srcLoc.append('<');
            srcLoc.append(clsName);
            srcLoc.append("::initializer::");
            srcLoc.append(((JCTree) cfgStatement.getCode()).pos);
            srcLoc.append('>');
        } else if (ast.getKind() == UnderlyingAST.Kind.METHOD) {
            CFGMethod cfgMethod = (CFGMethod) ast;
            String clsName = cfgMethod.getClassTree().getSimpleName().toString();
            String methodName = cfgMethod.getMethod().getName().toString();
            outFile.append(clsName);
            outFile.append('-');
            outFile.append(methodName);

            srcLoc.append('<');
            srcLoc.append(clsName);
            srcLoc.append("::");
            srcLoc.append(methodName);
            srcLoc.append('(');
            srcLoc.append(cfgMethod.getMethod().getParameters());
            srcLoc.append(")::");
            srcLoc.append(((JCTree) cfgMethod.getMethod()).pos);
            srcLoc.append('>');
        } else {
            throw new BugInCF("Unexpected AST kind: " + ast.getKind() + " value: " + ast);
        }
        outFile.append('-');
        outFile.append(checkerName);
        outFile.append(".dot");

        // make path safe for Windows
        String out = outFile.toString().replace("<", "_").replace(">", "");

        generated.put(srcLoc.toString(), out);

        return out;
    }

    /**
     * Generate the order of processing blocks.
     *
     * @param cfg the current control flow graph
     */
    private IdentityHashMap<Block, List<Integer>> getProcessOrder(ControlFlowGraph cfg) {
        IdentityHashMap<Block, List<Integer>> depthFirstOrder = new IdentityHashMap<>();
        int count = 1;
        for (Block b : cfg.getDepthFirstOrderedBlocks()) {
            depthFirstOrder.computeIfAbsent(b, k -> new ArrayList<>());
            depthFirstOrder.get(b).add(count++);
        }
        return depthFirstOrder;
    }

    /**
     * Produce a representation of the contests of a basic block.
     *
     * @param bb basic block to visualize
     * @param analysis the current analysis
     */
    @Override
    public String visualizeBlock(Block bb, @Nullable Analysis<A, S, T> analysis) {
        StringBuilder sbBlock = new StringBuilder();
        String lineSeparator = "\\n";
        sbBlock.append(loopOverBlockContents(bb, analysis, lineSeparator));

        // handle case where no contents are present
        boolean centered = false;
        if (sbBlock.length() == 0) {
            centered = true;
            if (bb.getType() == BlockType.SPECIAL_BLOCK) {
                sbBlock.append(visualizeSpecialBlock((SpecialBlock) bb));
            } else if (bb.getType() == BlockType.CONDITIONAL_BLOCK) {
                sbBlock.append(" \",];\n");
                return sbBlock.toString();
            } else {
                sbBlock.append("?? empty ?? \",];\n");
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
                    sbStore.append("\\n~~~~~~~~~\\n");
                    sbStore.append("After:");
                    sbStore.append(visualizeStore(analysis.getResult().getStoreAfter(lastNode)));
                    sbBlock.append(sbStore);
                }
            }
        }

        return (sbBlock.toString() + (centered ? "" : "\\n")).replace("\\n", "\\l") + " \",];\n";
    }

    /**
     * Add an edge to the graph.
     *
     * @param sId Id of current block
     * @param eId Id of successor
     * @param labelContent the flow rule
     */
    private String addDotEdge(long sId, long eId, String labelContent) {
        return "    " + sId + " -> " + eId + " [label=\"" + labelContent + "\"];\n";
    }

    /**
     * {@inheritDoc}
     *
     * <p>Do not call this method by hand. Use {@link StringCFGVisualizer} to see the String version
     * of the value of store.
     */
    @Override
    public String visualizeStore(S store) {
        return store.visualize(this);
    }

    @Override
    public String visualizeStoreThisVal(A value) {
        return "  this > " + value + "\\n";
    }

    @Override
    public String visualizeStoreLocalVar(FlowExpressions.LocalVariable localVar, A value) {
        return "  " + localVar + " > " + toStringEscapeDoubleQuotes(value) + "\\n";
    }

    @Override
    public String visualizeStoreFieldVals(FlowExpressions.FieldAccess fieldAccess, A value) {
        return "  " + fieldAccess + " > " + toStringEscapeDoubleQuotes(value) + "\\n";
    }

    @Override
    public String visualizeStoreArrayVal(FlowExpressions.ArrayAccess arrayValue, A value) {
        return "  " + arrayValue + " > " + toStringEscapeDoubleQuotes(value) + "\\n";
    }

    @Override
    public String visualizeStoreMethodVals(FlowExpressions.MethodCall methodCall, A value) {
        return "  " + methodCall.toString().replace("\"", "\\\"");
    }

    @Override
    public String visualizeStoreClassVals(FlowExpressions.ClassName className, A value) {
        return "  " + className + " > " + toStringEscapeDoubleQuotes(value) + "\\n";
    }

    @Override
    public String visualizeStoreKeyVal(String keyName, Object value) {
        return "  " + keyName + " = " + value + "\\n";
    }

    private String escapeDoubleQuotes(final String str) {
        return str.replace("\"", "\\\"");
    }

    private String toStringEscapeDoubleQuotes(final Object obj) {
        return escapeDoubleQuotes(String.valueOf(obj));
    }

    @Override
    public String visualizeStoreHeader(String classCanonicalName) {
        return classCanonicalName + " (\\n";
    }

    @Override
    public String visualizeStoreFooter() {
        return ")";
    }

    /**
     * Write a file {@code methods.txt} that contains a mapping from source code location to
     * generated dot file.
     */
    @Override
    public void shutdown() {
        try {
            // Open for append, in case of multiple sub-checkers.
            FileWriter fstream = new FileWriter(outDir + "/methods.txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            for (Map.Entry<String, String> kv : generated.entrySet()) {
                out.write(kv.getKey());
                out.append('\t');
                out.write(kv.getValue());
                out.append('\n');
            }
            out.close();
        } catch (IOException e) {
            throw new UserError(
                    "Error creating methods.txt file in: " + outDir + "; ensure the path is valid",
                    e);
        }
    }
}
