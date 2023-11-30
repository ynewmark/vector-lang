package org.vectorlang.compiler.compiler;

import java.util.HashMap;
import java.util.Map;

public class State {
    private Map<String, Type> variables;
    private Map<String, Integer> ids;
    private int currentId;
    private State previous;

    public State(State previous) {
        this.variables = new HashMap<>();
        this.ids = new HashMap<>();
        this.previous = previous;
        this.currentId = previous == null ? 0 : previous.currentId;
    }

    public State() {
        this(null);
    }

    public Type get(String name) {
        if (this.variables.containsKey(name)) {
            return this.variables.get(name);
        } else if (this.previous != null) {
            return this.previous.get(name);
        } else {
            return null;
        }
    }

    public int getId(String name) {
        if (this.variables.containsKey(name)) {
            return this.ids.get(name);
        } else if (this.previous != null) {
            return this.previous.getId(name);
        } else {
            return -1;
        }
    }

    public boolean put(String name, Type type) {
        if (this.variables.containsKey(name)) {
            return false;
        }
        this.variables.put(name, type);
        this.ids.put(name, this.currentId++);
        return true;
    }
}
