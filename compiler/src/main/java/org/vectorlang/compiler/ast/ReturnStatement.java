package org.vectorlang.compiler.ast;

public class ReturnStatement extends Statement {
    
    private final Expression expression;

    public ReturnStatement(Expression expression, int length, int position) {
        super(length, position);
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg) {
        return visitor.visitReturnStmt(this, arg);
    }
}
