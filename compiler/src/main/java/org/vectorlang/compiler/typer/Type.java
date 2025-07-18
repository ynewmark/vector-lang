package org.vectorlang.compiler.typer;

import java.util.Arrays;
import java.util.Map;

import org.vectorlang.compiler.compiler.BaseType;

public class Type {
    private Dimension[] shape;
    private BaseType baseType;
    private boolean constant;

    public Type(BaseType baseType, Dimension[] shape, boolean constant) {
        this.shape = shape;
        this.baseType = baseType;
        this.constant = constant;
    }

    public Type vectorize(int length) {
        Dimension[] newShape = new Dimension[shape.length + 1];
        newShape[0] = ConstDimension.getDimension(length);
        for (int i = 0; i < shape.length; i++) {
            newShape[i + 1] = shape[i];
        }
        return new Type(baseType, newShape, constant);
    }

    public Type indexed() {
        if (shape.length > 0) {
            Dimension[] newShape = new Dimension[shape.length - 1];
            for (int i = 0; i < newShape.length; i++) {
                newShape[i] = shape[i + 1];
            }
            return new Type(baseType, newShape, constant);
        } else {
            return null;
        }
    }

    public Type concat(Type type) {
        Dimension[] newShape = new Dimension[shape.length];
        shape[0] = shape[0].plus(type.shape[0]);
        for (int i = 1; i < shape.length; i++) {
            newShape[i] = shape[i];
        }
        return new Type(baseType, newShape, constant);
    }

    public boolean match(Map<String, Integer> constraints, Type other) {
        if (shape.length != other.shape.length) {
            return false;
        } else {
            for (int i = 0; i < shape.length; i++) {
                int value = other.shape[i].getValue(constraints);
                if (value == -1) {
                    return false;
                }
                if (!shape[i].match(constraints, value)) {
                    return false;
                }
            }
            return true;
        }
    }

    public Type constrain(Map<String, Integer> constraints) {
        Dimension[] newShape = new Dimension[shape.length];
        for (int i = 0; i < shape.length; i++) {
            int value = shape[i].getValue(constraints);
            if (value == -1) {
                return null;
            }
            newShape[i] = ConstDimension.getDimension(value);
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
        for (Dimension dimension : shape) {
            int value = dimension.getValue(Map.of());
            if (value == -1) {
                return -1;
            }
            size *= value;
        }
        return size;
    }

    public Dimension[] getShape() {
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
        for (Dimension i : shape) {
            builder.append('[').append(i).append(']');
        }
        return builder.toString();
    }
}
