package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.typer.Type;

public class DeclareStatement extends Statement {

    private final boolean constant;
    private final String name;
    private final Expression initial;
    private final Type type;

    public DeclareStatement(boolean constant, String name, Expression initial, Type type) {
        super();
        this.constant = constant;
        this.name = name;
        this.initial = initial;
        this.type = type;
    }

    public DeclareStatement(boolean constant, String name, Expression initial) {
        this(constant, name, initial, null);
    }

    public boolean isConst() {
        return this.constant;
    }

    public String getName() {
        return this.name;
    }

    public Expression getInitial() {
        return this.initial;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg) {
        return visitor.visitDeclareStmt(this, arg);
    }
}
