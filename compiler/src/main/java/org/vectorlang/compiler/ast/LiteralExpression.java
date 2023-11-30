package org.vectorlang.compiler.ast;

import org.vectorlang.compiler.compiler.BaseType;
import org.vectorlang.compiler.compiler.Shape;
import org.vectorlang.compiler.compiler.Type;

public class LiteralExpression extends Expression {

    private final int intValue;
    private final boolean boolValue;
    private final double floatValue;
    private final int type;

    public LiteralExpression(int value, int length, int position) {
        super(new Type(new Shape(BaseType.INT, new int[0]), true), length, position);
        this.intValue = value;
        this.boolValue = false;
        this.floatValue = 0;
        type = 0;
    }

    public LiteralExpression(boolean value, int length, int position) {
        super(new Type(new Shape(BaseType.BOOL, new int[0]), true), length, position);
        this.intValue = 0;
        this.boolValue = value;
        this.floatValue = 0;
        type = 1;
    }

    public LiteralExpression(double value, int length, int position) {
        super(new Type(new Shape(BaseType.FLOAT, new int[0]), true), length, position);
        this.intValue = 0;
        this.boolValue = false;
        this.floatValue = value;
        type = 2;
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
        if (type == 0) {
            return this.intValue;
        } else if (type == 1) {
            return this.boolValue ? 0 : Long.MIN_VALUE;
        } else if (type == 2) {
            return Double.doubleToLongBits(this.floatValue);
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        if (type == 0) {
            return Integer.toString(intValue);
        } else if (type == 1) {
            return Boolean.toString(boolValue);
        } else if (type == 2) {
            return Double.toString(floatValue);
        } else {
            return "?";
        }
    }

    @Override
    public <T, R> R visitExpression(ExpressionVisitor<T, R> visitor, T arg) {
        return visitor.visitLiteralExpr(this, arg);
    }
}
