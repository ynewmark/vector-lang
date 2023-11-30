package org.vectorlang.compiler.compiler;

import org.vectorlang.compiler.ast.UnaryOperator;

public class UnaryTable<T> {
    
    @SuppressWarnings("unchecked")
    private T[][] table = (T[][]) new Object[BaseType.values().length][UnaryOperator.values().length];

    public T get(BaseType baseType, UnaryOperator unaryOperator) {
        return table[baseType.ordinal()][unaryOperator.ordinal()];
    }

    public void put(BaseType baseType, UnaryOperator unaryOperator, T value) {
        table[baseType.ordinal()][unaryOperator.ordinal()] = value;
    }
}
