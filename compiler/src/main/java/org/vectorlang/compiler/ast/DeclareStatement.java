package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.Type;

public class DeclareStatement extends Statement {

    private final boolean constant;
    private final String name;
    private final Expression initial;
    private final Type type;

    public DeclareStatement(boolean constant, String name, Expression initial, Type type, int length, int position) {
        super(length, position);
        this.constant = constant;
        this.name = name;
        this.initial = initial;
        this.type = type;
    }

    public DeclareStatement(boolean constant, String name, Expression initial, int length, int position) {
        this(constant, name, initial, null, length, position);
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
