package org.vectorlang.compiler.typer;

import java.util.Map;

public class ConstDimension implements Dimension {
    
    private final int value;

    private ConstDimension(int value) {
        this.value = value;
    }

    @Override
    public boolean match(Map<String, Integer> constraints, int value) {
        return this.value == value;
    }

    @Override
    public int getValue(Map<String, Integer> constraints) {
        return value;
    }

    @Override
    public Dimension plus(Dimension other) {
        if (other instanceof ConstDimension) {
            return new ConstDimension(value + ((ConstDimension) other).value);
        } else {
            return new SumDimension(this, other);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ConstDimension) {
            return ((ConstDimension) other).value == value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    private static final int CACHE_SIZE = 64;
    private static final ConstDimension[] CACHE = new ConstDimension[CACHE_SIZE];

    public static ConstDimension getDimension(int value) {
        if (value < CACHE_SIZE) {
            if (CACHE[value] == null) {
                CACHE[value] = new ConstDimension(value);
            }
            return CACHE[value];
        } else {
            return new ConstDimension(value);
        }
    }
}
