package org.vectorlang.compiler.ast;

public class ForStatement extends Statement {

    private final Statement initial, body, each;
    private final Expression condition;

    public ForStatement(Expression condition, Statement initial, Statement each, Statement body) {
        super();
        this.initial = initial;
        this.each = each;
        this.body = body;
        this.condition = condition;
    }

    public Statement getInitial() {
        return initial;
    }

    public Statement getEach() {
        return each;
    }

    public Statement getBody() {
        return body;
    }

    public Expression getCondition() {
        return condition;
    }

    @Override
    public <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg) {
        return visitor.visitForStmt(this, arg);
    }
    
}
