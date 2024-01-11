package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.Type;

public class StaticExpression extends Expression {
    
    private final LiteralExpression[] data;

    public StaticExpression(LiteralExpression[] data, Type type) {
        super(type);
        this.data = data;
    }

    public LiteralExpression[] getData() {
        return data;
    }

    public long[] getRaw() {
        long[] raw = new long[data.length + 1];
        for (int i = 0; i < raw.length; i++) {
            raw[i] = data[i].getRaw();
        }
        raw[raw.length - 1] = data.length;
        return raw;
    }

    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitStaticExpr(this, arg);
    }
}
