package org.vectorlang.compiler.parser;

import java.util.function.Function;

import org.vectorlang.compiler.ast.Node;

public class ParseRule<T extends Node> {
    
    private Function<ParserState, T> function;
    private TokenType conclusion;
    
    public ParseRule(Function<ParserState, T> function, TokenType conclusion) {
        this.function = function;
        this.conclusion = conclusion;
    }

    public T apply(ParserState state) {
        int begin = state.getPosition();
        T result = function.apply(state);
        if (result == null) {
            state.recover(conclusion);
            return null;
        } else {
            result.setPosition(begin, state.getPosition());
            return result;
        }
    }
}
