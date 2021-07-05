package org.checkerframework.dataflow.analysis;

import org.checkerframework.dataflow.cfg.node.AbstractNodeVisitor;
import org.checkerframework.dataflow.cfg.node.ConditionalAndNode;
import org.checkerframework.dataflow.cfg.node.ConditionalNotNode;
import org.checkerframework.dataflow.cfg.node.ConditionalOrNode;
import org.checkerframework.dataflow.cfg.node.EqualToNode;
import org.checkerframework.dataflow.cfg.node.GreaterThanNode;
import org.checkerframework.dataflow.cfg.node.GreaterThanOrEqualNode;
import org.checkerframework.dataflow.cfg.node.LessThanNode;
import org.checkerframework.dataflow.cfg.node.LessThanOrEqualNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.NotEqualNode;

/**
 * Evaluates the static knowledge which branches will be taken. This evaluator is used for dead
 * branch elimination. TRUE means only then branch is taken, FALSE only else branch, UNKNOWN means
 * both could be taken.
 */
public class ConditionEvaluator<V extends AbstractValue<V>, S extends Store<S>>
        extends AbstractNodeVisitor<ConditionEvaluator.ConditionFlow, TransferInput<V, S>> {

    public enum ConditionFlow {
        TRUE,
        FALSE,
        UNKNOWN
    }

    /**
     * Evaluate a boolean expression and returns the flow at the condition.
     *
     * @param node the boolean node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    @Override
    public ConditionFlow visitNode(Node node, TransferInput<V, S> in) {
        if (node instanceof GreaterThanNode) {
            return visitGreaterThan((GreaterThanNode) node, in);
        }
        if (node instanceof GreaterThanOrEqualNode) {
            return visitGreaterThanOrEqual((GreaterThanOrEqualNode) node, in);
        }
        if (node instanceof LessThanOrEqualNode) {
            return visitLessThanOrEqual((LessThanOrEqualNode) node, in);
        }
        if (node instanceof LessThanNode) {
            return visitLessThan((LessThanNode) node, in);
        }
        if (node instanceof EqualToNode) {
            return visitEqualTo((EqualToNode) node, in);
        }
        if (node instanceof NotEqualNode) {
            return visitNotEqual((NotEqualNode) node, in);
        }
        if (node instanceof ConditionalAndNode) {
            return visitConditionalAnd((ConditionalAndNode) node, in);
        }
        if (node instanceof ConditionalOrNode) {
            return visitConditionalOr((ConditionalOrNode) node, in);
        }
        if (node instanceof ConditionalNotNode) {
            return visitConditionalNot((ConditionalNotNode) node, in);
        }
        return ConditionFlow.UNKNOWN;
    }

    /**
     * Evaluate a greater than node and returns the flow at the condition.
     *
     * @param node the greater than node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    @Override
    public ConditionFlow visitGreaterThan(GreaterThanNode node, TransferInput<V, S> in) {
        return ConditionFlow.UNKNOWN;
    }

    /**
     * Evaluate a greater than or equal node and returns the flow at the condition.
     *
     * @param node the greater than or equal node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    @Override
    public ConditionFlow visitGreaterThanOrEqual(
            GreaterThanOrEqualNode node, TransferInput<V, S> in) {
        return ConditionFlow.UNKNOWN;
    }

    /**
     * Evaluate a less than or equal node and returns the flow at the condition.
     *
     * @param node the less than or equal node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    @Override
    public ConditionFlow visitLessThanOrEqual(LessThanOrEqualNode node, TransferInput<V, S> in) {
        return ConditionFlow.UNKNOWN;
    }

    /**
     * Evaluate a less than node and returns the flow at the condition.
     *
     * @param node the less than node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    @Override
    public ConditionFlow visitLessThan(LessThanNode node, TransferInput<V, S> in) {
        return ConditionFlow.UNKNOWN;
    }

    /**
     * Evaluate a equal to node and returns the flow at the condition.
     *
     * @param node the equal to node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    @Override
    public ConditionFlow visitEqualTo(EqualToNode node, TransferInput<V, S> in) {
        return ConditionFlow.UNKNOWN;
    }

    /**
     * Evaluate a not equal node and returns the flow at the condition.
     *
     * @param node the not equal node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    @Override
    public ConditionFlow visitNotEqual(NotEqualNode node, TransferInput<V, S> in) {
        return ConditionFlow.UNKNOWN;
    }

    /**
     * Evaluate a conditional AND node and returns the flow at the condition. Both right and left
     * expression are individually evaluated to determine the flow of branch.
     *
     * @param node the conditional and node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    @Override
    public ConditionFlow visitConditionalAnd(ConditionalAndNode node, TransferInput<V, S> in) {
        ConditionFlow left = visitNode(node.getLeftOperand(), in);
        ConditionFlow right = visitNode(node.getRightOperand(), in);
        if (left == ConditionFlow.TRUE && right == ConditionFlow.TRUE) {
            return ConditionFlow.TRUE;
        } else if (left == ConditionFlow.FALSE || right == ConditionFlow.FALSE) {
            return ConditionFlow.FALSE;
        }
        return ConditionFlow.UNKNOWN;
    }

    /**
     * Evaluate a conditional OR node and returns the flow at the condition. Both right and left
     * expression are individually evaluated to determine the flow of branch.
     *
     * @param node the conditional and node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    @Override
    public ConditionFlow visitConditionalOr(ConditionalOrNode node, TransferInput<V, S> in) {
        ConditionFlow left = visitNode(node.getLeftOperand(), in);
        ConditionFlow right = visitNode(node.getRightOperand(), in);
        if (left == ConditionFlow.TRUE || right == ConditionFlow.TRUE) {
            return ConditionFlow.TRUE;
        } else if (left == ConditionFlow.FALSE && right == ConditionFlow.FALSE) {
            return ConditionFlow.FALSE;
        }
        return ConditionFlow.UNKNOWN;
    }

    /**
     * Evaluate a conditional NOT node and returns the flow at the condition.
     *
     * @param node the conditional and node to be evaluated on
     * @param in the transfer input storing the values of the node
     * @return the flow at a boolean expression
     */
    @Override
    public ConditionFlow visitConditionalNot(ConditionalNotNode node, TransferInput<V, S> in) {
        ConditionFlow sub = visitNode(node.getOperand(), in);
        if (sub == ConditionFlow.TRUE) {
            return ConditionFlow.FALSE;
        }
        if (sub == ConditionFlow.FALSE) {
            return ConditionFlow.TRUE;
        }
        return ConditionFlow.UNKNOWN;
    }
}
