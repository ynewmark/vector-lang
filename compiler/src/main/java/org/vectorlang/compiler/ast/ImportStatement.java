package org.vectorlang.compiler.ast;

public class ImportStatement extends Statement {

    private final String name;

    public ImportStatement(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    @Override
    public <T, R> R visitStatement(StatementVisitor<T, R> visitor, T arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitStatement'");
    }
    
}
