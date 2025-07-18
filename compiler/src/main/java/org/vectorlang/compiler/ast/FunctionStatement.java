package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.typer.Type;

public class FunctionStatement extends Statement {
    
    private final String name;
    private final String[] parameterNames;
    private final Type[] parameterTypes;
    private final Statement[] body;
    private final Type returnType;

    public FunctionStatement(String name, String[] names, Type[] types, Statement[] body, Type type) {
        super();
        this.name = name;
        this.parameterNames = names;
        this.parameterTypes = types;
        this.body = body;
        this.returnType = type;
    }

    public String getName() {
        return name;
    }

    public String[] getParameterNames() {
        return parameterNames;
    }

    public Type[] getParameterTypes() {
        return parameterTypes;
    }

    public Statement[] getBody() {
        return body;
    }

    public Type getReturnType() {
        return returnType;
    }
    
    @Override
    public <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg) {
        return visitor.visitFunctionStmt(this, arg);
    }
}
