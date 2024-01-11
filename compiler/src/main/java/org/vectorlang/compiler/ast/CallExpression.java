package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.Type;

public class CallExpression extends Expression {
    
    private final String name;
    private final Expression[] args;

    public CallExpression(String name, Expression[] args, Type type) {
        super(type);
        this.name = name;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public Expression[] getArgs() {
        return args;
    }
    
    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitCallExpression(this, arg);
    }
    
}
