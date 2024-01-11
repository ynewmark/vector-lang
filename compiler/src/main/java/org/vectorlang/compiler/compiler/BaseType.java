package org.vectorlang.compiler.compiler;

public enum BaseType {
    INT(4), FLOAT(8), BOOL(1), CHAR(1);

    private int width;

    private BaseType(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }
}
