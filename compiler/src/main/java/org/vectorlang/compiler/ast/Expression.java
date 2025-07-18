package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.typer.Type;

public abstract class Expression extends Node {

    private final Type type;

    protected Expression(Type type) {
        super();
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T arg) {
        return visitExpression(visitor, arg);
    }

    public abstract <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg);
}
