package org.vectorlang.compiler.ast;

public abstract class Node {
    protected final int start;
    protected final int end;

    protected Node(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public abstract <T, R> R accept(Visitor<T, R> visitor, T arg);
}
