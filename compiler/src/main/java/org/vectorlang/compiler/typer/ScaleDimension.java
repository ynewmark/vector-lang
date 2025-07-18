package org.vectorlang.compiler.typer;

import java.util.Map;
import java.util.Objects;

public class ScaleDimension implements Dimension {

    private final Dimension dimension;
    private final int factor;

    public ScaleDimension(Dimension dimension, int factor) {
        this.dimension = dimension;
        this.factor = factor;
    }

    @Override
    public boolean match(Map<String, Integer> constraints, int value) {
        if (value % factor == 0) {
            return dimension.match(constraints, value / factor);
        } else {
            return false;
        }
    }

    @Override
    public int getValue(Map<String, Integer> constraints) {
        int value = dimension.getValue(constraints);
        if (value == -1) {
            return -1;
        } else {
            return dimension.getValue(constraints) * factor;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ScaleDimension) {
            return ((ScaleDimension) other).dimension.equals(dimension)
                && ((ScaleDimension) other).factor == factor;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, factor);
    }

    @Override
    public String toString() {
        return factor + " " + dimension;
    }
    
}
