package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.Type;

public class BinaryExpression extends Expression {

    private final Expression left, right;
    private final BinaryOperator operator;

    public BinaryExpression(Expression left, Expression right, BinaryOperator operator, Type type) {
        super(type);
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public BinaryExpression(Expression left, Expression right, BinaryOperator operator) {
        this(left, right, operator, null);
    }

    public Expression getLeft() {
        return this.left;
    }

    public Expression getRight() {
        return this.right;
    }

    public BinaryOperator getOperator() {
        return this.operator;
    }

    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitBinaryExpr(this, arg);
    }
}
