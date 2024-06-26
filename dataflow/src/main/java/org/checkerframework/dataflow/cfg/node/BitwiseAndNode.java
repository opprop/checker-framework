package org.checkerframework.dataflow.cfg.node;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * A node for the bitwise or logical (single bit) and operation:
 *
 * <pre>
 *   <em>expression</em> &amp; <em>expression</em>
 * </pre>
 */
public class BitwiseAndNode extends BinaryOperationNode {

    /**
     * Constructs a {@link BitwiseAndNode}.
     *
     * @param tree the binary tree
     * @param left the left operand
     * @param right the right operand
     */
    public BitwiseAndNode(BinaryTree tree, Node left, Node right) {
        super(tree, left, right);
        assert tree.getKind() == Tree.Kind.AND;
    }

    @Override
    public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitBitwiseAnd(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeftOperand() + " & " + getRightOperand() + ")";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof BitwiseAndNode)) {
            return false;
        }
        BitwiseAndNode other = (BitwiseAndNode) obj;
        return getLeftOperand().equals(other.getLeftOperand())
                && getRightOperand().equals(other.getRightOperand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeftOperand(), getRightOperand());
    }
}
