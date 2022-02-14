package org.checkerframework.dataflow.reachdefinition;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.BackwardTransferFunction;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.dataflow.cfg.node.*;
import java.util.List;

/** A live variable transfer function. */
public class ReachDefTransfer
        extends AbstractNodeVisitor<
                TransferResult<ReachDefinitionValue, ReachDefinitionStore>,
                TransferInput<ReachDefinitionValue, ReachDefinitionStore>>
        implements BackwardTransferFunction<ReachDefinitionValue, ReachDefinitionStore> {

    @Override
    public ReachDefinitionStore initialNormalExitStore(
            UnderlyingAST underlyingAST, @Nullable List<ReturnNode> returnNodes) {
        return new ReachDefinitionStore();
    }

    @Override
    public ReachDefinitionStore initialExceptionalExitStore(UnderlyingAST underlyingAST) {
        return new ReachDefinitionStore();
    }

    @Override
    public RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore> visitNode(
            Node n, TransferInput<ReachDefinitionValue, ReachDefinitionStore> p) {
        return new RegularTransferResult<>(null, p.getRegularStore());
    }

    @Override
    public RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore> visitAssignment(
            AssignmentNode n, TransferInput<ReachDefinitionValue, ReachDefinitionStore> p) {
        RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore> transferResult =
                (RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore>) super.visitAssignment(n, p);
        processLiveVarInAssignment(
                n.getTarget(), n.getExpression(), transferResult.getRegularStore());
        return transferResult;
    }

    @Override
    public RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore> visitStringConcatenateAssignment(
            StringConcatenateAssignmentNode n, TransferInput<ReachDefinitionValue, ReachDefinitionStore> p) {
        RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore> transferResult =
                (RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore>)
                        super.visitStringConcatenateAssignment(n, p);
        processLiveVarInAssignment(
                n.getLeftOperand(), n.getRightOperand(), transferResult.getRegularStore());
        return transferResult;
    }

    @Override
    public RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore> visitMethodInvocation(
            MethodInvocationNode n, TransferInput<ReachDefinitionValue, ReachDefinitionStore> p) {
        RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore> transferResult =
                (RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore>)
                        super.visitMethodInvocation(n, p);
        ReachDefinitionStore store = transferResult.getRegularStore();
        for (Node arg : n.getArguments()) {
            store.addUseInExpression(arg);
        }
        return transferResult;
    }

    @Override
    public RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore> visitObjectCreation(
            ObjectCreationNode n, TransferInput<ReachDefinitionValue, ReachDefinitionStore> p) {
        RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore> transferResult =
                (RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore>) super.visitObjectCreation(n, p);
        ReachDefinitionStore store = transferResult.getRegularStore();
        for (Node arg : n.getArguments()) {
            store.addUseInExpression(arg);
        }
        return transferResult;
    }

    @Override
    public RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore> visitReturn(
            ReturnNode n, TransferInput<ReachDefinitionValue, ReachDefinitionStore> p) {
        RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore> transferResult =
                (RegularTransferResult<ReachDefinitionValue, ReachDefinitionStore>) super.visitReturn(n, p);
        Node result = n.getResult();
        if (result != null) {
            ReachDefinitionStore store = transferResult.getRegularStore();
            store.addUseInExpression(result);
        }
        return transferResult;
    }

    /**
     * Update the information of live variables from an assignment statement.
     *
     * @param variable the variable that should be killed
     * @param expression the expression in which the variables should be added
     * @param store the live variable store
     */
    private void processLiveVarInAssignment(Node variable, Node expression, ReachDefinitionStore store) {
        store.killDef(new ReachDefinitionValue(variable));
        store.addUseInExpression(expression);
    }
}
