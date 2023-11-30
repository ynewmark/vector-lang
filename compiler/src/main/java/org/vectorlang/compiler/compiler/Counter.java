package org.vectorlang.compiler.compiler;

public class Counter {
    private int current;

    public Counter(int initial) {
        this.current = initial;
    }

    public Counter() {
        this.current = 0;
    }

    public int getAndIncrement() {
        return this.current++;
    }
}
