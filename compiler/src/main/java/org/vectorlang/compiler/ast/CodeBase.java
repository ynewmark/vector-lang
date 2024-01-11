package org.vectorlang.compiler.ast;

public class CodeBase {
    
    private final FunctionStatement[] functions;

    public CodeBase(FunctionStatement[] functions) {
        this.functions = functions;
    }

    public FunctionStatement[] getFunctions() {
        return functions;
    }
}
