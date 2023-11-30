package org.vectorlang.compiler.compiler;

import java.util.Arrays;

public class Type {
    private Shape[] typeData;
    private boolean constant;

    public Type(Shape typeData, boolean constant) {
        this(new Shape[]{typeData}, constant);
    }

    public Type(Shape[] typeData, boolean constant) {
        this.typeData = typeData;
        this.constant = constant;
    }

    public Type apply(Shape shape) {
        if (typeData.length > 1) {
            if (typeData[0].equals(shape)) {
                Shape[] result = new Shape[typeData.length - 1];
                for (int i = 0; i < result.length; i++) {
                    result[i] = typeData[i + 1];
                }
                return new Type(result, constant);
            }
        }
        return null;
    }

    public Type vectorize(int size) {
        if (typeData.length == 1) {
            return new Type(typeData[0].vectorize(size), constant);
        }
        return null;
    }

    public Type indexed() {
        if (typeData.length == 1) {
            return new Type(typeData[0].index(), constant);
        }
        return null;
    }

    public int getSize() {
        return this.typeData[0].getSize();
    }

    public int[] getShape() {
        return this.typeData[0].shape();
    }

    public BaseType getBaseType() {
        return this.typeData[0].baseType();
    }

    public Type constant() {
        return new Type(typeData, true);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Type) {
            return Arrays.equals(typeData, ((Type) object).typeData);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(switch(typeData[0].baseType()) {
            case BOOL -> "bool";
            case CHAR -> "char";
            case FLOAT -> "float";
            case INT -> "int";
        });
        for (int i : typeData[0].shape()) {
            builder.append('[').append(i).append(']');
        }
        return builder.toString();
    }
}
