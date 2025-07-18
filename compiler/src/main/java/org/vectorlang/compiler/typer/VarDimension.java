package org.vectorlang.compiler.typer;

import java.util.Map;

public class VarDimension implements Dimension {
    
    private final String var;

    public VarDimension(String var) {
        this.var = var;
    }

    @Override
    public boolean match(Map<String, Integer> constraints, int value) {
        if (constraints.containsKey(var)) {
            return constraints.get(var) == value;
        } else {
            constraints.put(var, value);
            return true;
        }
    }

    @Override
    public int getValue(Map<String, Integer> constraints) {
        return constraints.getOrDefault(var, -1);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof VarDimension) {
            return ((VarDimension) other).var.equals(var);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return var.hashCode();
    }

    @Override
    public String toString() {
        return var;
    }
}
