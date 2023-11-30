package org.vectorlang.compiler.ast;

public class BlockStatement extends Statement {

    private final Statement[] statements;

    public BlockStatement(Statement[] statements, int length, int position) {
        super(length, position);
        this.statements = statements;
    }

    public Statement[] getStatements() {
        return this.statements;
    }

    @Override
    public <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg) {
        return visitor.visitBlockStmt(this, arg);
    }
}
