package org.vectorlang.compiler.compiler;

import org.vectorlang.compiler.ast.BinaryOperator;

public class BinaryTable<T> {
    
    @SuppressWarnings("unchecked")
    private T[][][] table = (T[][][]) new Object[BaseType.values().length][BaseType.values().length][BinaryOperator.values().length];

    public T get(BaseType type1, BaseType type2, BinaryOperator operator) {
        return table[type1.ordinal()][type2.ordinal()][operator.ordinal()];
    }

    public void put(BaseType type1, BaseType type2, BinaryOperator operator, T value) {
        table[type1.ordinal()][type2.ordinal()][operator.ordinal()] = value;
    }
}
