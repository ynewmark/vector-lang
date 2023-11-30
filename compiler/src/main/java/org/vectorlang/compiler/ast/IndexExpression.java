package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.Type;

public class IndexExpression extends Expression {

    private final Expression base, index;

    public IndexExpression(Expression base, Expression index, Type type, int length, int position) {
        super(type, length, position);
        this.base = base;
        this.index = index;
    }

    public IndexExpression(Expression base, Expression index, int length, int position) {
        this(base, index, null, length, position);
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
