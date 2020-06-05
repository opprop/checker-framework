package org.checkerframework.dataflow.analysis;

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

public class ConditionEvaluator<A extends AbstractValue<A>, S extends Store<S>> {

    public enum ConditionalFlow {
        THEN,
        ELSE,
        BOTH
    }

    public ConditionalFlow visit(Node node, TransferInput<A, S> in) {
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
        return ConditionalFlow.BOTH;
    }

    public ConditionalFlow visitGreaterThan(GreaterThanNode node, TransferInput<A, S> in) {
        return ConditionalFlow.BOTH;
    }

    public ConditionalFlow visitGreaterThanOrEqual(
            GreaterThanOrEqualNode node, TransferInput<A, S> in) {
        return ConditionalFlow.BOTH;
    }

    public ConditionalFlow visitLessThanOrEqual(LessThanOrEqualNode node, TransferInput<A, S> in) {
        return ConditionalFlow.BOTH;
    }

    public ConditionalFlow visitLessThan(LessThanNode node, TransferInput<A, S> in) {
        return ConditionalFlow.BOTH;
    }

    public ConditionalFlow visitEqualTo(EqualToNode node, TransferInput<A, S> in) {
        return ConditionalFlow.BOTH;
    }

    public ConditionalFlow visitNotEqual(NotEqualNode node, TransferInput<A, S> in) {
        return ConditionalFlow.BOTH;
    }

    public ConditionalFlow visitConditionalAnd(ConditionalAndNode node, TransferInput<A, S> in) {
        ConditionalFlow left = visit(node.getLeftOperand(), in);
        ConditionalFlow right = visit(node.getRightOperand(), in);
        if (left == ConditionalFlow.THEN && right == ConditionalFlow.THEN) {
            return ConditionalFlow.THEN;
        } else if (left == ConditionalFlow.ELSE || right == ConditionalFlow.ELSE) {
            return ConditionalFlow.ELSE;
        }
        return ConditionalFlow.BOTH;
    }

    public ConditionalFlow visitConditionalOr(ConditionalOrNode node, TransferInput<A, S> in) {
        ConditionalFlow left = visit(node.getLeftOperand(), in);
        ConditionalFlow right = visit(node.getRightOperand(), in);
        if (left == ConditionalFlow.THEN || right == ConditionalFlow.THEN) {
            return ConditionalFlow.THEN;
        } else if (left == ConditionalFlow.ELSE && right == ConditionalFlow.ELSE) {
            return ConditionalFlow.ELSE;
        }
        return ConditionalFlow.BOTH;
    }

    public ConditionalFlow visitConditionalNot(ConditionalNotNode node, TransferInput<A, S> in) {
        ConditionalFlow sub = visit(node.getOperand(), in);
        if (sub == ConditionalFlow.THEN) {
            return ConditionalFlow.ELSE;
        }
        if (sub == ConditionalFlow.ELSE) {
            return ConditionalFlow.THEN;
        }
        return ConditionalFlow.BOTH;
    }
}
