package org.vectorlang.compiler.ast;

public abstract class Node {
    private int start;
    private int end;

    protected Node() {
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setPosition(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public abstract <T, R> R accept(Visitor<T, R> visitor, T arg);
}
