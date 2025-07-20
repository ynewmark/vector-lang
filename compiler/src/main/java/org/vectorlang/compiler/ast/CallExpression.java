package org.vectorlang.compiler.ast;

import java.util.Map;

import org.vectorlang.compiler.typer.Type;

public class CallExpression extends Expression {
    
    private final String name;
    private final Expression[] args;
    private final Map<String, Integer> typeVars;

    public CallExpression(String name, Expression[] args, Type type, Map<String, Integer> typeVars) {
        super(type);
        this.name = name;
        this.args = args;
        this.typeVars = typeVars;
    }

    public String getName() {
        return name;
    }

    public Expression[] getArgs() {
        return args;
    }

    public Map<String, Integer> getTypeVars() {
        return typeVars;
    }
    
    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitCallExpression(this, arg);
    }
    
}
