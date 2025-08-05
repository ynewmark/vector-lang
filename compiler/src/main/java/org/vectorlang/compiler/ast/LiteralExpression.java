package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.BaseType;
import org.vectorlang.compiler.typer.Dimension;
import org.vectorlang.compiler.typer.Type;

public class LiteralExpression extends Expression {

    private final int intValue;
    private final boolean boolValue;
    private final double floatValue;
    private final char charValue;

    public LiteralExpression(int value) {
        super(new Type(BaseType.INT, new Dimension[0], true));
        this.intValue = value;
        this.boolValue = false;
        this.floatValue = 0;
        this.charValue = '\0';
    }

    public LiteralExpression(boolean value) {
        super(new Type(BaseType.BOOL, new Dimension[0], true));
        this.intValue = 0;
        this.boolValue = value;
        this.floatValue = 0;
        this.charValue = '\0';
    }

    public LiteralExpression(double value) {
        super(new Type(BaseType.FLOAT, new Dimension[0], true));
        this.intValue = 0;
        this.boolValue = false;
        this.floatValue = value;
        this.charValue = '\0';
    }

    public LiteralExpression(char value) {
        super(new Type(BaseType.CHAR, new Dimension[0], true));
        this.intValue = 0;
        this.boolValue = false;
        this.floatValue = 0;
        this.charValue = value;
    }

    public int getInt() {
        return this.intValue;
    }

    public boolean getBool() {
        return this.boolValue;
    }

    public double getFloat() {
        return this.floatValue;
    }

    public long getRaw() {
        return switch (getType().getBaseType()) {
            case BOOL -> this.boolValue ? 1 : 0;
            case CHAR -> charValue;
            case FLOAT -> Double.doubleToLongBits(this.floatValue);
            case INT -> intValue;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return switch (getType().getBaseType()) {
            case BOOL -> Boolean.toString(boolValue);
            case CHAR -> Character.toString(charValue);
            case FLOAT -> Double.toString(floatValue);
            case INT -> Integer.toString(intValue);
            default -> "?";
        };
    }

    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitLiteralExpr(this, arg);
    }
}
