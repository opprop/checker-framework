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
import org.checkerframework.dataflow.analysis.AbstractValue;
import org.checkerframework.dataflow.analysis.Analysis;
import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.analysis.Store;
import org.checkerframework.dataflow.analysis.TransferFunction;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.cfg.UnderlyingAST.CFGMethod;
import org.checkerframework.dataflow.cfg.UnderlyingAST.CFGStatement;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.block.Block.BlockType;
import org.checkerframework.dataflow.cfg.block.ConditionalBlock;
import org.checkerframework.dataflow.cfg.block.ExceptionBlock;
import org.checkerframework.dataflow.cfg.block.RegularBlock;
import org.checkerframework.dataflow.cfg.block.SingleSuccessorBlock;
import org.checkerframework.dataflow.cfg.block.SpecialBlock;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.UserError;

/** Generate a graph description in the DOT language of a control graph. */
public class DOTCFGVisualizer<
                A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
        extends AbstractCFGVisualizer<A, S, T> {

    protected String outdir;
    protected boolean verbose;
    protected String checkerName;

    protected StringBuilder sbDigraph;
    protected StringBuilder sbStore;
    protected StringBuilder sbBlock;

    /** Mapping from class/method representation to generated dot file. */
    protected Map<String, String> generated;

    @Override
    public void init(Map<String, Object> args) {
        this.outdir = (String) args.get("outdir");
        {
            Object verb = args.get("verbose");
            this.verbose =
                    verb != null
                            && (verb instanceof String
                                    ? Boolean.getBoolean((String) verb)
                                    : (boolean) verb);
        }
        this.checkerName = (String) args.get("checkerName");

        this.generated = new HashMap<>();

        this.sbDigraph = new StringBuilder();

        this.sbStore = new StringBuilder();

        this.sbBlock = new StringBuilder();
    }

    @Override
    public @Nullable Map<String, Object> visualize(
            ControlFlowGraph cfg, Block entry, @Nullable Analysis<A, S, T> analysis) {

        String dotgraph = generateDotGraph(cfg, entry, analysis);
        String dotfilename = dotOutputFileName(cfg.underlyingAST);

        try {
            FileWriter fstream = new FileWriter(dotfilename);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(dotgraph);
            out.close();
        } catch (IOException e) {
            throw new UserError(
                    "Error creating dot file: " + dotfilename + "; ensure the path is valid", e);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("dotFileName", dotfilename);

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
        this.sbDigraph.setLength(0);
        Set<Block> visited = new HashSet<>();

        // header
        this.sbDigraph.append("digraph {\n");

        Block cur = entry;
        Queue<Block> worklist = new ArrayDeque<>();
        visited.add(entry);
        // traverse control flow graph and define all arrows
        while (cur != null) {
            if (cur.getType() == BlockType.CONDITIONAL_BLOCK) {
                ConditionalBlock ccur = ((ConditionalBlock) cur);
                Block thenSuccessor = ccur.getThenSuccessor();
                addDotEdge(ccur.getId(), thenSuccessor.getId(), "then\\n" + ccur.getThenFlowRule());
                if (!visited.contains(thenSuccessor)) {
                    visited.add(thenSuccessor);
                    worklist.add(thenSuccessor);
                }
                Block elseSuccessor = ccur.getElseSuccessor();
                addDotEdge(ccur.getId(), elseSuccessor.getId(), "else\\n" + ccur.getElseFlowRule());
                if (!visited.contains(elseSuccessor)) {
                    visited.add(elseSuccessor);
                    worklist.add(elseSuccessor);
                }
            } else {
                assert cur instanceof SingleSuccessorBlock;
                Block b = ((SingleSuccessorBlock) cur).getSuccessor();
                if (b != null) {
                    addDotEdge(
                            cur.getId(),
                            b.getId(),
                            ((SingleSuccessorBlock) cur).getFlowRule().name());
                    if (!visited.contains(b)) {
                        visited.add(b);
                        worklist.add(b);
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
                        addDotEdge(cur.getId(), b.getId(), exception);
                        if (!visited.contains(b)) {
                            visited.add(b);
                            worklist.add(b);
                        }
                    }
                }
            }

            cur = worklist.poll();
        }

        generateDotNodes(visited, cfg, analysis);

        // footer
        this.sbDigraph.append("}\n");

        return this.sbDigraph.toString();
    }

    /**
     * Generate the nodes of control flow graph as String.
     *
     * @param visited a set of blocks
     * @param cfg the current control flow graph
     * @param analysis the current analysis
     */
    private void generateDotNodes(
            Set<Block> visited, ControlFlowGraph cfg, @Nullable Analysis<A, S, T> analysis) {
        IdentityHashMap<Block, List<Integer>> processOrder = getProcessOrder(cfg);
        this.sbDigraph.append("    node [shape=rectangle];\n\n");
        // definition of all nodes including their labels
        for (Block v : visited) {
            this.sbDigraph.append("    ").append(v.getId()).append(" [");
            if (v.getType() == BlockType.CONDITIONAL_BLOCK) {
                this.sbDigraph.append("shape=polygon sides=8 ");
            } else if (v.getType() == BlockType.SPECIAL_BLOCK) {
                this.sbDigraph.append("shape=oval ");
            }
            this.sbDigraph.append("label=\"");
            if (verbose) {
                this.sbDigraph
                        .append("Process order: ")
                        .append(processOrder.get(v).toString().replaceAll("[\\[\\]]", ""))
                        .append("\\n");
            }
            visualizeBlock(v, analysis);
        }

        this.sbDigraph.append("\n");
    }

    /** @return the file name used for DOT output. */
    private String dotOutputFileName(UnderlyingAST ast) {
        StringBuilder srcloc = new StringBuilder();

        StringBuilder outfile = new StringBuilder(outdir);
        outfile.append('/');
        if (ast.getKind() == UnderlyingAST.Kind.ARBITRARY_CODE) {
            CFGStatement cfgs = (CFGStatement) ast;
            String clsname = cfgs.getClassTree().getSimpleName().toString();
            outfile.append(clsname);
            outfile.append("-initializer-");
            outfile.append(ast.hashCode());

            srcloc.append('<');
            srcloc.append(clsname);
            srcloc.append("::initializer::");
            srcloc.append(((JCTree) cfgs.getCode()).pos);
            srcloc.append('>');
        } else if (ast.getKind() == UnderlyingAST.Kind.METHOD) {
            CFGMethod cfgm = (CFGMethod) ast;
            String clsname = cfgm.getClassTree().getSimpleName().toString();
            String methname = cfgm.getMethod().getName().toString();
            outfile.append(clsname);
            outfile.append('-');
            outfile.append(methname);

            srcloc.append('<');
            srcloc.append(clsname);
            srcloc.append("::");
            srcloc.append(methname);
            srcloc.append('(');
            srcloc.append(cfgm.getMethod().getParameters());
            srcloc.append(")::");
            srcloc.append(((JCTree) cfgm.getMethod()).pos);
            srcloc.append('>');
        } else {
            throw new BugInCF("Unexpected AST kind: " + ast.getKind() + " value: " + ast);
        }
        outfile.append('-');
        outfile.append(checkerName);
        outfile.append(".dot");

        // make path safe for Windows
        String out = outfile.toString().replace("<", "_").replace(">", "");

        generated.put(srcloc.toString(), out);

        return out;
    }

    /**
     * Generate the order of processing blocks.
     *
     * @param cfg the current control flow graph
     */
    protected IdentityHashMap<Block, List<Integer>> getProcessOrder(ControlFlowGraph cfg) {
        IdentityHashMap<Block, List<Integer>> depthFirstOrder = new IdentityHashMap<>();
        int count = 1;
        for (Block b : cfg.getDepthFirstOrderedBlocks()) {
            if (depthFirstOrder.get(b) == null) {
                depthFirstOrder.put(b, new ArrayList<>());
            }
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
    public void visualizeBlock(Block bb, @Nullable Analysis<A, S, T> analysis) {

        this.sbBlock.setLength(0);

        // loop over contents
        List<Node> contents = new ArrayList<>();
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
        boolean notFirst = false;
        for (Node t : contents) {
            if (notFirst) {
                this.sbBlock.append("\\n");
            }
            notFirst = true;
            visualizeBlockNode(t, analysis);
        }

        // handle case where no contents are present
        boolean centered = false;
        if (this.sbBlock.length() == 0) {
            centered = true;
            if (bb.getType() == BlockType.SPECIAL_BLOCK) {
                visualizeSpecialBlock((SpecialBlock) bb);
            } else if (bb.getType() == BlockType.CONDITIONAL_BLOCK) {
                this.sbDigraph.append(" \",];\n");
                return;
            } else {
                this.sbDigraph.append("?? empty ?? \",];\n");
                return;
            }
        }

        // visualize transfer input if necessary
        if (analysis != null) {
            visualizeBlockTransferInput(bb, analysis);
        }

        this.sbDigraph
                .append((this.sbBlock.toString() + (centered ? "" : "\\n")).replace("\\n", "\\l"))
                .append(" \",];\n");
    }

    @Override
    public void visualizeSpecialBlock(SpecialBlock sbb) {
        switch (sbb.getSpecialType()) {
            case ENTRY:
                this.sbBlock.append("<entry>");
                break;
            case EXIT:
                this.sbBlock.append("<exit>");
                break;
            case EXCEPTIONAL_EXIT:
                this.sbBlock.append("<exceptional-exit>");
                break;
        }
    }

    @Override
    public void visualizeBlockTransferInput(Block bb, Analysis<A, S, T> analysis) {
        assert analysis != null
                : "analysis should be non-null when visualizing the transfer input of a block.";

        TransferInput<A, S> input = analysis.getInput(bb);
        assert input != null;

        this.sbStore.setLength(0);

        // split input representation to two lines
        this.sbStore.append("Before:");
        if (!input.containsTwoStores()) {
            S regularStore = input.getRegularStore();
            this.sbStore.append('[');
            visualizeStore(regularStore);
            this.sbStore.append(']');
        } else {
            S thenStore = input.getThenStore();
            this.sbStore.append("[then=");
            visualizeStore(thenStore);
            S elseStore = input.getElseStore();
            this.sbStore.append(", else=");
            visualizeStore(elseStore);
            this.sbStore.append("]");
        }
        // separator
        this.sbStore.append("\\n~~~~~~~~~\\n");

        // the transfer input before this block is added before the block content
        this.sbBlock.insert(0, this.sbStore);

        if (verbose) {
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
            if (lastNode != null) {
                this.sbStore.setLength(0);
                this.sbStore.append("\\n~~~~~~~~~\\n");
                this.sbStore.append("After:");
                visualizeStore(analysis.getResult().getStoreAfter(lastNode));
                this.sbBlock.append(this.sbStore);
            }
        }
    }

    @Override
    public void visualizeBlockNode(Node t, @Nullable Analysis<A, S, T> analysis) {
        this.sbBlock
                .append(prepareString(t.toString()))
                .append("   [ ")
                .append(prepareNodeType(t))
                .append(" ]");
        if (analysis != null) {
            A value = analysis.getValue(t);
            if (value != null) {
                this.sbBlock.append("    > ").append(prepareString(value.toString()));
            }
        }
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

    /**
     * Add an edge to the graph.
     *
     * @param sId Id of current block
     * @param eId Id of successor
     * @param labelContent the flow rule
     */
    private void addDotEdge(long sId, long eId, String labelContent) {
        this.sbDigraph
                .append("    ")
                .append(sId)
                .append(" -> ")
                .append(eId)
                .append(" [label=\"")
                .append(labelContent)
                .append("\"];\n");
    }

    @Override
    public String visualizeStore(S store) {
        String sbStoreBackup = this.sbStore.toString();
        this.sbStore.setLength(0);
        store.visualize(this);
        String sbStoreReturnValue = this.sbStore.toString();
        this.sbStore.setLength(0);
        this.sbStore.append(sbStoreBackup).append(sbStoreReturnValue);
        return sbStoreReturnValue;
    }

    @Override
    public void visualizeStoreThisVal(A value) {
        this.sbStore.append("  this > ").append(value).append("\\n");
    }

    @Override
    public void visualizeStoreLocalVar(FlowExpressions.LocalVariable localVar, A value) {
        this.sbStore
                .append("  ")
                .append(localVar)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
    }

    @Override
    public void visualizeStoreFieldVals(FlowExpressions.FieldAccess fieldAccess, A value) {
        this.sbStore
                .append("  ")
                .append(fieldAccess)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
    }

    @Override
    public void visualizeStoreArrayVal(FlowExpressions.ArrayAccess arrayValue, A value) {
        this.sbStore
                .append("  ")
                .append(arrayValue)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
    }

    @Override
    public void visualizeStoreMethodVals(FlowExpressions.MethodCall methodCall, A value) {
        this.sbStore
                .append("  ")
                .append(methodCall.toString().replace("\"", "\\\""))
                .append(" > ")
                .append(value)
                .append("\\n");
    }

    @Override
    public void visualizeStoreClassVals(FlowExpressions.ClassName className, A value) {
        this.sbStore
                .append("  ")
                .append(className)
                .append(" > ")
                .append(toStringEscapeDoubleQuotes(value))
                .append("\\n");
    }

    @Override
    public void visualizeStoreKeyVal(String keyName, Object value) {
        this.sbStore.append("  ").append(keyName).append(" = ").append(value).append("\\n");
    }

    protected String escapeDoubleQuotes(final String str) {
        return str.replace("\"", "\\\"");
    }

    protected String toStringEscapeDoubleQuotes(final Object obj) {
        return escapeDoubleQuotes(String.valueOf(obj));
    }

    @Override
    public void visualizeStoreHeader(String classCanonicalName) {
        this.sbStore.append(classCanonicalName).append(" (\\n");
    }

    @Override
    public void visualizeStoreFooter() {
        this.sbStore.append(")");
    }

    /**
     * Write a file {@code methods.txt} that contains a mapping from source code location to
     * generated dot file.
     */
    @Override
    public void shutdown() {
        try {
            // Open for append, in case of multiple sub-checkers.
            FileWriter fstream = new FileWriter(outdir + "/methods.txt", true);
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
                    "Error creating methods.txt file in: " + outdir + "; ensure the path is valid",
                    e);
        }
    }
}
