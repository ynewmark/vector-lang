package org.vectorlang.compiler.ast;

public interface ExpressionVisitor<T, R> {
    public R visitBinaryExpr(BinaryExpression expression, T arg);
    public R visitGroupingExpr(GroupingExpression expression, T arg);
    public R visitIdentifierExpr(IdentifierExpression expression, T arg);
    public R visitLiteralExpr(LiteralExpression expression, T arg);
    public R visitUnaryExpr(UnaryExpression expression, T arg);
    public R visitVectorExpr(VectorExpression expression, T arg);
    public R visitIndexExpr(IndexExpression expression, T arg);
}
