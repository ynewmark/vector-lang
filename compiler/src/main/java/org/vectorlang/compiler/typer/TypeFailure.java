package org.vectorlang.compiler.typer;

public record TypeFailure(Type type1, Type type2, String info) {
    @Override
    public String toString() {
        return info + " ( " + (type1 == null ? "" : "Type 1: " + type1 + " ")
            + (type2 == null ? "" : "Type 2: " + type2 + " ") + ")";
    }
}
