package org.vectorlang.compiler.ast;

public class IfStatement extends Statement {
    
    private final Statement ifStatment, elseStatement;
    private final Expression condition;

    public IfStatement(Statement ifStatement, Statement elseStatement, Expression condition) {
        super();
        this.ifStatment = ifStatement;
        this.elseStatement = elseStatement;
        this.condition = condition;
    }

    public Statement getIfStatement() {
        return this.ifStatment;
    }

    public Statement getElseStatement() {
        return this.elseStatement;
    }

    public Expression getCondition() {
        return this.condition;
    }

    @Override
    public <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg) {
        return visitor.visitIfStmt(this, arg);
    }
}
