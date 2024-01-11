package org.vectorlang.compiler.ast;

public class PrintStatement extends Statement {

    private final Expression expression;

    public PrintStatement(Expression expression) {
        super();
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg) {
        return visitor.visitPrintStmt(this, arg);
    }
    
}
