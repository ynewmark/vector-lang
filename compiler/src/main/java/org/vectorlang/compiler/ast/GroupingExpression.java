package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.Type;

public class GroupingExpression extends Expression {

    private final Expression expression;

    public GroupingExpression(Expression expression, Type type, int length, int position) {
        super(type, length, position);
        this.expression = expression;
    }

    public GroupingExpression(Expression expression, int length, int position) {
        this(expression, null, length, position);
    }

    public Expression getExpression() {
        return this.expression;
    }

    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitGroupingExpr(this, arg);
    }
    
}
