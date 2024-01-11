package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.Type;

public class UnaryExpression extends Expression {

    private final Expression expression;
    private final UnaryOperator operator;

    public UnaryExpression(Expression expression, UnaryOperator operator, Type type) {
        super(type);
        this.expression = expression;
        this.operator = operator;
    }

    public UnaryExpression(Expression expression, UnaryOperator operator) {
        this(expression, operator, null);
    }

    public Expression getExpression() {
        return this.expression;
    }

    public UnaryOperator getOperator() {
        return this.operator;
    }

    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitUnaryExpr(this, arg);
    }
}
