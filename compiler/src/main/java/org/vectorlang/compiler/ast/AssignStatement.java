package org.vectorlang.compiler.ast;

public class AssignStatement extends Statement {

    private final String leftHand;
    private final Expression rightHand;

    public AssignStatement(String leftHand, Expression rightHand, int length, int position) {
        super(length, position);
        this.rightHand = rightHand;
        this.leftHand = leftHand;
    }

    public String getLeftHand() {
        return this.leftHand;
    }

    public Expression getRightHand() {
        return this.rightHand;
    }

    @Override
    public <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg) {
        return visitor.visitAssignStmt(this, arg);
    }
}
