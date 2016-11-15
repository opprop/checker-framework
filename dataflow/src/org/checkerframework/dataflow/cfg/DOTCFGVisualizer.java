package org.checkerframework.dataflow.cfg;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

import com.sun.tools.javac.tree.JCTree;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.dataflow.analysis.AbstractValue;
import org.checkerframework.dataflow.analysis.Analysis;
import org.checkerframework.dataflow.analysis.Analysis.Direction;
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
import org.checkerframework.javacutil.ErrorReporter;

/**
 * Generate a graph description in the DOT language of a control graph.
 *
 * @author Stefan Heule
 */
public class DOTCFGVisualizer<
                A extends AbstractValue<A>, S extends Store<S>, T extends TransferFunction<A, S>>
        implements CFGVisualizer<A, S, T> {

    protected String outdir;
    protected boolean verbose;
    // TODO: This is checker-framework specific field, should be removed or have a better name
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
                    verb == null
                            ? false
                            : verb instanceof String
                                    ? Boolean.getBoolean((String) verb)
                                    : (boolean) verb;
        }
        this.checkerName = (String) args.get("checkerName");

        this.generated = new HashMap<>();

        this.sbDigraph = new StringBuilder();

        this.sbStore = new StringBuilder();

        this.sbBlock = new StringBuilder();
    }

    /** {@inheritDoc} */
    @Override
    public /*@Nullable*/ Map<String, Object> visualize(
            ControlFlowGraph cfg, Block entry, /*@Nullable*/ Analysis<A, S, T> analysis) {

        String dotgraph = generateDotGraph(cfg, entry, analysis);

        String dotfilename = dotOutputFileName(cfg.underlyingAST);
        // System.err.println("Output to DOT file: " + dotfilename);

        try {
            FileWriter fstream = new FileWriter(dotfilename);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(dotgraph);
            out.close();
        } catch (IOException e) {
            ErrorReporter.errorAbort(
                    "Error creating dot file: " + dotfilename + "; ensure the path is valid", e);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("dotFileName", dotfilename);

        return res;
    }

    /** Generate the dot representation as String. */
    protected String generateDotGraph(
            ControlFlowGraph cfg, Block entry, /*@Nullable*/ Analysis<A, S, T> analysis) {
        this.sbDigraph.setLength(0);
        Set<Block> visited = new HashSet<>();

        // header
        this.sbDigraph.append("digraph {\n");

        Block cur = entry;
        Queue<Block> worklist = new LinkedList<>();
        visited.add(entry);
        // traverse control flow graph and define all arrows
        while (true) {
            if (cur == null) {
                break;
            }

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

    protected void generateDotNodes(
            Set<Block> visited, ControlFlowGraph cfg, /*@Nullable*/ Analysis<A, S, T> analysis) {
        IdentityHashMap<Block, List<Integer>> processOrder = getProcessOrder(cfg);
        this.sbDigraph.append("    node [shape=rectangle];\n\n");
        // definition of all nodes including their labels
        for (Block v : visited) {
            this.sbDigraph.append("    " + v.getId() + " [");
            if (v.getType() == BlockType.CONDITIONAL_BLOCK) {
                this.sbDigraph.append("shape=polygon sides=8 ");
            } else if (v.getType() == BlockType.SPECIAL_BLOCK) {
                this.sbDigraph.append("shape=oval ");
            }
            this.sbDigraph.append("label=\"");
            if (verbose) {
                this.sbDigraph.append(
                        "Process order: "
                                + processOrder.get(v).toString().replaceAll("[\\[\\]]", "")
                                + "\\n");
            }
            visualizeBlock(v, analysis);
        }

        this.sbDigraph.append("\n");
    }

    /** @return the file name used for DOT output. */
    protected String dotOutputFileName(UnderlyingAST ast) {
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
            ErrorReporter.errorAbort(
                    "Unexpected AST kind: " + ast.getKind() + " value: " + ast.toString());
            return null;
        }

        if (checkerName != null) {
            outfile.append('-');
            outfile.append(checkerName);
        }

        outfile.append(".dot");

        // make path safe for Windows
        String out = outfile.toString().replace("<", "_").replace(">", "");

        generated.put(srcloc.toString(), out);

        return out;
    }

    protected IdentityHashMap<Block, List<Integer>> getProcessOrder(ControlFlowGraph cfg) {
        IdentityHashMap<Block, List<Integer>> depthFirstOrder = new IdentityHashMap<>();
        int count = 1;
        for (Block b : cfg.getDepthFirstOrderedBlocks()) {
            if (depthFirstOrder.get(b) == null) {
                depthFirstOrder.put(b, new ArrayList<Integer>());
            }
            depthFirstOrder.get(b).add(count++);
        }
        return depthFirstOrder;
    }

    /**
     * Produce a representation of the contests of a basic block.
     *
     * @param bb basic block to visualize
     */
    @Override
    public void visualizeBlock(Block bb, /*@Nullable*/ Analysis<A, S, T> analysis) {

        this.sbBlock.setLength(0);

        // loop over contents
        List<Node> contents = new LinkedList<>();
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

        this.sbDigraph.append(
                (this.sbBlock.toString() + (centered ? "" : "\\n")).replace("\\n", "\\l")
                        + " \",];\n");
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

        visualizeBlockTransferInput(bb, analysis, true);
        if (verbose) {
            visualizeBlockTransferInput(bb, analysis, false);
        }
    }

    /**
     * visualize transfer input of given {@link Block}
     *
     * @param bb given block
     * @param analysis corresponding analysis on this block
     * @param before if true, visualize the transfer input before given Block, otherwise visualize
     *     the transfer input after given Block
     */
    protected void visualizeBlockTransferInput(
            Block bb, Analysis<A, S, T> analysis, boolean before) {
        S regularStore = null;
        S thenStore = null;
        S elseStore = null;
        boolean isTwoStores = false;

        // first reset the sbStore
        this.sbStore.setLength(0);
        String title = before ? "Before:" : "After:";
        String seperator = "\\n~~~~~~~~~\\n";

        Direction analysisDirection = analysis.getDirection();

        if ((before && analysisDirection == Direction.FORWARD)
                || (!before && analysisDirection == Direction.BACKWARD)) {
            TransferInput<A, S> input = analysis.getInput(bb);
            isTwoStores = input.containsTwoStores();
            regularStore = input.getRegularStore();
            thenStore = input.getThenStore();
            elseStore = input.getElseStore();
        } else if (!before && analysisDirection == Direction.FORWARD) {
            isTwoStores = false;
            regularStore = analysis.getResult().getStoreAfter(bb);
        } else if (before && analysisDirection == Direction.BACKWARD) {
            isTwoStores = false;
            regularStore = analysis.getResult().getStoreBefore(bb);
        } else {
            ErrorReporter.errorAbort("this point should never be reached!");
        }

        // split input representation to two lines
        this.sbStore.append(title);

        if (!isTwoStores) {
            this.sbStore.append('[');
            visualizeStore(regularStore);
            this.sbStore.append(']');
        } else {
            this.sbStore.append("[then=");
            visualizeStore(thenStore);
            this.sbStore.append(", else=");
            visualizeStore(elseStore);
            this.sbStore.append("]");
        }

        // insert separator
        if (before) {
            this.sbStore.append(seperator);
            // the transfer input before this block is added before the block content
            this.sbBlock.insert(0, this.sbStore);
        } else {
            this.sbStore.insert(0, seperator);
            this.sbBlock.append(this.sbStore);
        }
    }

    @Override
    public void visualizeBlockNode(Node t, /*@Nullable*/ Analysis<A, S, T> analysis) {
        this.sbBlock.append(prepareString(t.toString()) + "   [ " + prepareNodeType(t) + " ]");
        if (analysis != null) {
            A value = analysis.getValue(t);
            if (value != null) {
                this.sbBlock.append("    > " + prepareString(value.toString()));
            }
        }
    }

    protected String prepareNodeType(Node t) {
        String name = t.getClass().getSimpleName();
        return name.replace("Node", "");
    }

    protected String prepareString(String s) {
        return s.replace("\"", "\\\"");
    }

    protected void addDotEdge(long sId, long eId, String labelContent) {
        this.sbDigraph.append("    " + sId + " -> " + eId + " [label=\"" + labelContent + "\"];\n");
    }

    @Override
    public void visualizeStore(S store) {
        store.visualize(this);
    }

    @Override
    public void visualizeStoreThisVal(A value) {
        this.sbStore.append("  this > " + value + "\\n");
    }

    @Override
    public void visualizeStoreLocalVar(FlowExpressions.LocalVariable localVar, A value) {
        this.sbStore.append("  " + localVar + " > " + toStringEscapeDoubleQuotes(value) + "\\n");
    }

    @Override
    public void visualizeStoreFieldVals(FlowExpressions.FieldAccess fieldAccess, A value) {
        this.sbStore.append("  " + fieldAccess + " > " + toStringEscapeDoubleQuotes(value) + "\\n");
    }

    @Override
    public void visualizeStoreArrayVal(FlowExpressions.ArrayAccess arrayValue, A value) {
        this.sbStore.append("  " + arrayValue + " > " + toStringEscapeDoubleQuotes(value) + "\\n");
    }

    @Override
    public void visualizeStoreMethodVals(FlowExpressions.MethodCall methodCall, A value) {
        this.sbStore.append(
                "  " + methodCall.toString().replace("\"", "\\\"") + " > " + value + "\\n");
    }

    @Override
    public void visualizeStoreClassVals(FlowExpressions.ClassName className, A value) {
        this.sbStore.append("  " + className + " > " + toStringEscapeDoubleQuotes(value) + "\\n");
    }

    @Override
    public void visualizeStoreKeyVal(String keyName, Object value) {
        this.sbStore.append(
                "  " + prepareString(keyName) + " = " + prepareString(value.toString()) + "\\n");
    }

    protected String escapeDoubleQuotes(final String str) {
        return str.replace("\"", "\\\"");
    }

    protected String toStringEscapeDoubleQuotes(final Object obj) {
        return escapeDoubleQuotes(String.valueOf(obj));
    }

    @Override
    public void visualizeStoreHeader(String classCanonicalName) {
        this.sbStore.append(classCanonicalName + " (\\n");
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
            ErrorReporter.errorAbort(
                    "Error creating methods.txt file in: " + outdir + "; ensure the path is valid",
                    e);
        }
    }
}
