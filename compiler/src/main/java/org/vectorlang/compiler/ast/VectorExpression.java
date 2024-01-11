package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.Type;

public class VectorExpression extends Expression {

    private final Expression[] expressions;

    public VectorExpression(Expression[] expressions, Type type) {
        super(type);
        this.expressions = expressions;
    }

    public VectorExpression(Expression[] expressions) {
        this(expressions, null);
    }

    public Expression[] getExpressions() {
        return this.expressions;
    }

    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitVectorExpr(this, arg);
    }
}
