package org.vectorlang.compiler.typer;

import java.util.Map;
import java.util.Set;

public interface Dimension {
    
    boolean match(Map<String, Integer> constraints, int value);
    int getValue(Map<String, Integer> constraints);
    Set<String> getVars();

    default Dimension plus(Dimension other) {
        return new SumDimension(this, other);
    }
}
