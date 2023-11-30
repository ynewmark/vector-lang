package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.Type;

public class IdentifierExpression extends Expression {

    private final String name;

    public IdentifierExpression(String name, Type type, int length, int position) {
        super(type, length, position);
        this.name = name;
    }

    public IdentifierExpression(String name, int length, int position) {
        this(name, null, length, position);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitIdentifierExpr(this, arg);
    }
}
