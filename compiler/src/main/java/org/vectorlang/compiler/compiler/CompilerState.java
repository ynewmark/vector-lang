package org.vectorlang.compiler.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompilerState {
    private Map<String, Integer> ids, functionIds;
    private Set<String> parameters;
    private int currentId, parameterId;
    private Counter labelCounter, staticCounter, funcCounter;
    private CompilerState previous;

    public CompilerState(CompilerState previous, Counter labelCounter, Counter staticCounter, Counter funcCounter) {
        this.previous = previous;
        this.ids = new HashMap<>();
        this.functionIds = new HashMap<>();
        this.currentId = (previous != null) ? previous.currentId : 0;
        this.parameterId = (previous != null) ? previous.parameterId : 0;
        this.labelCounter = (labelCounter != null) ? labelCounter : previous.labelCounter;
        this.staticCounter = (staticCounter != null) ? staticCounter : previous.staticCounter;
        this.funcCounter = (funcCounter != null) ? funcCounter : previous.funcCounter;
        this.parameters = new HashSet<>();
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

    public void putParameter(String name) {
        ids.put(name, parameterId++);
        parameters.add(name);
    }

    public boolean isParameter(String name) {
        if (parameters.contains(name)) {
            return true;
        } else if (previous != null) {
            return previous.isParameter(name);
        } else {
            return false;
        }
    }

    public int addLabel() {
        return labelCounter.getAndIncrement();
    }

    public int addStatic() {
        return staticCounter.getAndIncrement();
    }

    public void addFunction(String name) {
        this.functionIds.put(name, funcCounter.getAndIncrement());
    }

    public int getFunction(String name) {
        if (this.functionIds.get(name) == null) {
            if (previous != null) {
                return previous.getFunction(name);
            } else {
                return -1;
            }
        } else {
            return this.functionIds.get(name);
        }
    }

    public void updateCount(CompilerState state) {
        this.currentId = Math.max(this.currentId, state.currentId);
    }

    public int getCount() {
        return currentId;
    }
}
