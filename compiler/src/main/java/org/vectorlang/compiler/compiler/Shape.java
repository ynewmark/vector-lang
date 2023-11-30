package org.vectorlang.compiler.compiler;

import java.util.Arrays;

public record Shape(BaseType baseType, int[] shape) {
    public Shape index() {
        if (shape.length > 0) {
            int[] newShape = new int[shape.length - 1];
            for (int i = 0; i < newShape.length; i++) {
                newShape[i] = shape[i + 1];
            }
            return new Shape(baseType, newShape);
        } else {
            return null;
        }
    }

    public Shape vectorize(int length) {
        int[] newShape = new int[shape.length + 1];
        newShape[0] = length;
        for (int i = 0; i < shape.length; i++) {
            newShape[i + 1] = shape[i];
        }
        return new Shape(baseType, newShape);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Shape) {
            Shape other = (Shape) object;
            return this.baseType.equals(other.baseType) && Arrays.equals(this.shape, other.shape);
        }
        return false;
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
}
