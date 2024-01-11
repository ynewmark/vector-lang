package org.vectorlang.compiler.ast;

public abstract class Statement extends Node {
    
    protected Statement() {
        super();
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T arg) {
        return visitStatement(visitor, arg);
    }

    public abstract <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg);
}
