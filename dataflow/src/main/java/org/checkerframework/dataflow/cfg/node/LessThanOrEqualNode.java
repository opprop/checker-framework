package org.checkerframework.dataflow.cfg.node;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.Tree;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * A node for the less than or equal comparison:
 *
 * <pre>
 *   <em>expression</em> &lt;= <em>expression</em>
 * </pre>
 */
public class LessThanOrEqualNode extends BinaryOperationNode {

    /**
     * Constructs a {@link LessThanOrEqualNode}.
     *
     * @param tree the binary tree
     * @param left the left operand
     * @param right the right operand
     */
    public LessThanOrEqualNode(BinaryTree tree, Node left, Node right) {
        super(tree, left, right);
        assert tree.getKind() == Tree.Kind.LESS_THAN_EQUAL;
    }

    @Override
    public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
        return visitor.visitLessThanOrEqual(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeftOperand() + " <= " + getRightOperand() + ")";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof LessThanOrEqualNode)) {
            return false;
        }
        LessThanOrEqualNode other = (LessThanOrEqualNode) obj;
        return getLeftOperand().equals(other.getLeftOperand())
                && getRightOperand().equals(other.getRightOperand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeftOperand(), getRightOperand());
    }
}
