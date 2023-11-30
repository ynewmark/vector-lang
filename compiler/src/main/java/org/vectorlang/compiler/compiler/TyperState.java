package org.vectorlang.compiler.compiler;

import java.util.HashMap;
import java.util.Map;

public class TyperState {
    private Map<String, Type> variables;
    private TyperState previous;
    
    public TyperState(TyperState previous) {
        this.previous = previous;
        this.variables = new HashMap<>();
    }

    public TyperState() {
        this(null);
    }

    public Type get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (previous != null) {
            return previous.get(name);
        } else {
            return null;
        }
    }

    public boolean put(String name, Type type) {
        if (variables.containsKey(name)) {
            return false;
        }
        variables.put(name, type);
        return true;
    }
}
