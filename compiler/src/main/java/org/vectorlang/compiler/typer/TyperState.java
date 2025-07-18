package org.vectorlang.compiler.typer;

import java.util.HashMap;
import java.util.Map;

public class TyperState {
    private Map<String, Type> variables;
    private Map<String, FuncType> functions;
    private TyperState previous;
    
    public TyperState(TyperState previous) {
        this.previous = previous;
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
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

    public FuncType getFunc(String name) {
        if (functions.containsKey(name)) {
            return functions.get(name);
        } else if (previous != null) {
            return previous.getFunc(name);
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

    public boolean putFunc(String name, Type[] argTypes, Type returnType) {
        if (functions.containsKey(name)) {
            return false;
        }
        functions.put(name, new FuncType(argTypes, returnType));
        return true;
    }
}
