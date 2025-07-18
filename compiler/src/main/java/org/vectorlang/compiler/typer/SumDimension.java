package org.vectorlang.compiler.typer;

import java.util.Map;
import java.util.Objects;

public class SumDimension implements Dimension {

    private final Dimension dimension1, dimension2;

    public SumDimension(Dimension dimension1, Dimension dimension2) {
        this.dimension1 = dimension1;
        this.dimension2 = dimension2;
    }

    @Override
    public boolean match(Map<String, Integer> constraints, int value) {
        int subvalue = dimension1.getValue(constraints);
        if (subvalue == -1 || subvalue >= value) {
            return false;
        } else {
            return dimension2.match(constraints, value - subvalue);
        }
    }

    @Override
    public int getValue(Map<String, Integer> constraints) {
        int value1 = dimension1.getValue(constraints);
        int value2 = dimension2.getValue(constraints);
        if (value1 == -1 || value2 == -1) {
            return -1;
        } else {
            return value1 + value2;
        }
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof SumDimension) {
            return ((SumDimension) other).dimension1.equals(dimension1)
                && ((SumDimension) other).dimension2.equals(dimension2);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension1, dimension2);
    }

    @Override
    public String toString() {
        return dimension1 + " + " + dimension2;
    }
}
