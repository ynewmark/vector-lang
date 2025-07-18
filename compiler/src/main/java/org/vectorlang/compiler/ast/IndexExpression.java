package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.typer.Type;

public class IndexExpression extends Expression {

    private final Expression base, index;

    public IndexExpression(Expression base, Expression index, Type type) {
        super(type);
        this.base = base;
        this.index = index;
    }

    public IndexExpression(Expression base, Expression index) {
        this(base, index, null);
    }

    public Expression getBase() {
        return this.base;
    }

    public Expression getIndex() {
        return this.index;
    }

    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitIndexExpr(this, arg);
    }
}
