package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.Type;

public class ReturnStatement extends Statement {
    
    private final Expression expression;

    public ReturnStatement(Expression expression, Type type, int length, int position) {
        super(length, position);
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitExpression'");
    }
}
