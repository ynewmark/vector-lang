package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.Type;

public class StaticExpression extends Expression {
    
    private final long[] data;

    public StaticExpression(long[] data, Type type, int length, int position) {
        super(type, length, position);
        this.data = data;
    }

    public long[] getData() {
        return data;
    }

    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitStaticExpr(this, arg);
    }
}
