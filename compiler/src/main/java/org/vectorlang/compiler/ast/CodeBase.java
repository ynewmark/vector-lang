package org.vectorlang.compiler.ast;

public class CodeBase {
    
    private final Statement[] statements;

    public CodeBase(Statement[] statements) {
        this.statements = statements;
    }

    public Statement[] getStatements() {
        return statements;
    }
}
