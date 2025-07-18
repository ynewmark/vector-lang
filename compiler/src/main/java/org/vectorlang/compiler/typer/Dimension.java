package org.vectorlang.compiler.typer;

import java.util.Map;

public interface Dimension {
    
    boolean match(Map<String, Integer> constraints, int value);
    int getValue(Map<String, Integer> constraints);

    default Dimension plus(Dimension other) {
        return new SumDimension(this, other);
    }
}
