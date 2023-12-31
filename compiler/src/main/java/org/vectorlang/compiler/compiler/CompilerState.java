package org.vectorlang.compiler.compiler;

import java.util.HashMap;
import java.util.Map;

public class CompilerState {
    private Map<String, Integer> ids;
    private int currentId;
    private Counter labelCounter, staticCounter;
    private CompilerState previous;

    public CompilerState(CompilerState previous, Counter labelCounter, Counter statiCounter) {
        this.previous = previous;
        this.ids = new HashMap<>();
        this.currentId = previous != null ? previous.currentId : 0;
        this.labelCounter = labelCounter;
        this.staticCounter = statiCounter;
    }

    public int get(String name) {
        if (ids.containsKey(name)) {
            return ids.get(name);
        } else if (previous != null) {
            return previous.get(name);
        } else {
            return -1;
        }
    }

    public void put(String name) {
        ids.put(name, currentId++);
    }

    public int addLabel() {
        return labelCounter.getAndIncrement();
    }

    public int addStatic() {
        return staticCounter.getAndIncrement();
    }
}
