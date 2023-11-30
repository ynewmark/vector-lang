package org.vectorlang.compiler.ast;

public interface Visitor<T, R> extends ExpressionVisitor<T, R>, StatementVisitor<T, R> {
}
