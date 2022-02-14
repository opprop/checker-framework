package org.checkerframework.dataflow.reachdefinition;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.Store;
import org.checkerframework.dataflow.cfg.node.*;
import org.checkerframework.dataflow.cfg.visualize.CFGVisualizer;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.javacutil.BugInCF;

/** A live variable store contains a set of live variables represented by nodes. */
public class ReachDefinitionStore implements Store<ReachDefinitionStore> {

    /** A set of live variable abstract values. */
    private final Set<ReachDefinitionValue> reachDefSet;

    /** Create a new LiveVarStore. */
    public ReachDefinitionStore() {
        reachDefSet = new LinkedHashSet<>();
    }

    /**
     * Create a new LiveVarStore.
     *
     * @param reachDefSet a set of live variable abstract values
     */
    public ReachDefinitionStore(Set<ReachDefinitionValue> reachDefSet) {
        this.reachDefSet = reachDefSet;
    }

    /**
     * Add the information of a live variable into the live variable set.
     *
     * @param variable a live variable
     */
    public void putLiveVar(ReachDefinitionValue variable) {
        reachDefSet.add(variable);
    }

    /**
     * Remove the information of a live variable from the live variable set.
     *
     * @param variable a live variable
     */
    public void killDef(ReachDefinitionValue variable) {
        reachDefSet.remove(variable);
    }

    /**
     * Add the information of live variables in an expression to the live variable set.
     *
     * @param expression a node
     */
    public void addUseInExpression(Node expression) {
        // TODO Do we need a AbstractNodeScanner to do the following job?
        if (expression instanceof LocalVariableNode || expression instanceof FieldAccessNode) {
            ReachDefinitionValue liveVarValue = new ReachDefinitionValue(expression);
            putLiveVar(liveVarValue);
        } else if (expression instanceof UnaryOperationNode) {
            UnaryOperationNode unaryNode = (UnaryOperationNode) expression;
            addUseInExpression(unaryNode.getOperand());
        } else if (expression instanceof TernaryExpressionNode) {
            TernaryExpressionNode ternaryNode = (TernaryExpressionNode) expression;
            addUseInExpression(ternaryNode.getConditionOperand());
            addUseInExpression(ternaryNode.getThenOperand());
            addUseInExpression(ternaryNode.getElseOperand());
        } else if (expression instanceof TypeCastNode) {
            TypeCastNode typeCastNode = (TypeCastNode) expression;
            addUseInExpression(typeCastNode.getOperand());
        } else if (expression instanceof InstanceOfNode) {
            InstanceOfNode instanceOfNode = (InstanceOfNode) expression;
            addUseInExpression(instanceOfNode.getOperand());
        } else if (expression instanceof BinaryOperationNode) {
            BinaryOperationNode binaryNode = (BinaryOperationNode) expression;
            addUseInExpression(binaryNode.getLeftOperand());
            addUseInExpression(binaryNode.getRightOperand());
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof ReachDefinitionStore)) {
            return false;
        }
        ReachDefinitionStore other = (ReachDefinitionStore) obj;
        return other.reachDefSet.equals(this.reachDefSet);
    }

    @Override
    public int hashCode() {
        return this.reachDefSet.hashCode();
    }

    @Override
    public ReachDefinitionStore copy() {
        return new ReachDefinitionStore(new HashSet<>(reachDefSet));
    }

    @Override
    public ReachDefinitionStore leastUpperBound(ReachDefinitionStore other) {
        Set<ReachDefinitionValue> reachDefSetLub =
                new HashSet<>(this.reachDefSet.size() + other.reachDefSet.size());
        reachDefSetLub.addAll(this.reachDefSet);
        reachDefSetLub.addAll(other.reachDefSet);
        return new ReachDefinitionStore(reachDefSetLub);
    }

    /** It should not be called since it is not used by the backward analysis. */
    @Override
    public ReachDefinitionStore widenedUpperBound(ReachDefinitionStore previous) {
        throw new BugInCF("wub of LiveVarStore get called!");
    }

    @Override
    public boolean canAlias(JavaExpression a, JavaExpression b) {
        return true;
    }

    @Override
    public String visualize(CFGVisualizer<?, ReachDefinitionStore, ?> viz) {
        String key = "live variables";
        if (reachDefSet.isEmpty()) {
            return viz.visualizeStoreKeyVal(key, "none");
        }
        StringJoiner sjStoreVal = new StringJoiner(", ");
        for (ReachDefinitionValue liveVarValue : reachDefSet) {
            sjStoreVal.add(liveVarValue.toString());
        }
        return viz.visualizeStoreKeyVal(key, sjStoreVal.toString());
    }

    @Override
    public String toString() {
        return reachDefSet.toString();
    }
}
