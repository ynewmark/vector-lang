package org.vectorlang.compiler.compiler;

import java.util.Arrays;

public class Type {
    private int[] shape;
    private BaseType baseType;
    private boolean constant;

    public Type(BaseType baseType, int[] shape, boolean constant) {
        this.shape = shape;
        this.baseType = baseType;
        this.constant = constant;
    }

    public Type vectorize(int length) {
        int[] newShape = new int[shape.length + 1];
        newShape[0] = length;
        for (int i = 0; i < shape.length; i++) {
            newShape[i + 1] = shape[i];
        }
        return new Type(baseType, newShape, constant);
    }

    public Type indexed() {
        if (shape.length > 0) {
            int[] newShape = new int[shape.length - 1];
            for (int i = 0; i < newShape.length; i++) {
                newShape[i] = shape[i + 1];
            }
            return new Type(baseType, newShape, constant);
        } else {
            return null;
        }
    }

    public Type concat(Type type) {
        int[] newShape = new int[shape.length];
        shape[0] = shape[0] + type.shape[0];
        for (int i = 1; i < shape.length; i++) {
            newShape[i] = shape[i];
        }
        return new Type(baseType, newShape, constant);
    }

    public int getSize() {
        int size = 1; /*switch (baseType) {
            case BOOL -> 1;
            case CHAR -> 1;
            case FLOAT -> 8;
            case INT -> 4;
        };*/
        for (int dimension : shape) {
            size *= dimension; 
        }
        return size;
    }

    public int[] getShape() {
        return shape;
    }

    public BaseType getBaseType() {
        return baseType;
    }

    public Type constant() {
        return new Type(baseType, shape, true);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Type) {
            return Arrays.equals(shape, ((Type) object).shape)
                && baseType.equals(((Type) object).baseType);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(switch(baseType) {
            case BOOL -> "bool";
            case CHAR -> "char";
            case FLOAT -> "float";
            case INT -> "int";
        });
        for (int i : shape) {
            builder.append('[').append(i).append(']');
        }
        return builder.toString();
    }
}
