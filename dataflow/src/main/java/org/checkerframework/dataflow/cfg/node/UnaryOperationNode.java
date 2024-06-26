package org.checkerframework.dataflow.cfg.node;

import com.sun.source.tree.UnaryTree;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.javacutil.TreeUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * A node for a postfix or an unary expression.
 *
 * <p>For example:
 *
 * <pre>
 *   <em>operator</em> <em>expressionNode</em>
 *
 *   <em>expressionNode</em> <em>operator</em>
 * </pre>
 */
public abstract class UnaryOperationNode extends Node {

    protected final UnaryTree tree;
    protected final Node operand;

    protected UnaryOperationNode(UnaryTree tree, Node operand) {
        super(TreeUtils.typeOf(tree));
        this.tree = tree;
        this.operand = operand;
    }

    public Node getOperand() {
        return this.operand;
    }

    @Override
    public UnaryTree getTree() {
        return tree;
    }

    @Override
    @SideEffectFree
    public Collection<Node> getOperands() {
        return Collections.singletonList(getOperand());
    }
}
