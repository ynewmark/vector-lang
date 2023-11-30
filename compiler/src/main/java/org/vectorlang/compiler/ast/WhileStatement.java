package org.vectorlang.compiler.ast;

public class WhileStatement extends Statement {

    private final Expression condition;
    private final Statement body;

    public WhileStatement(Expression condition, Statement body, int length, int position) {
        super(length, position);
        this.condition = condition;
        this.body = body;
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getBody() {
        return body;
    }

    @Override
    public <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg) {
        return visitor.visitWhileStmt(this, arg);
    }
    
}
