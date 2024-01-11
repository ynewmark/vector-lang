package org.vectorlang.compiler.parser;

import java.util.ArrayList;
import java.util.List;

public class ParserState {

    private List<Token> tokens;
    private List<String> errors;
    private int index;

    public ParserState(List<Token> tokens) {
        this.tokens = new ArrayList<>(tokens);
        this.errors = new ArrayList<>();
        this.index = 0;
    }
    
    public boolean hasNext() {
        return index < tokens.size();
    }

    public Token next() {
        return tokens.get(index++);
    }

    public Token previous() {
        return tokens.get(index - 1);
    }

    public Token peek() {
        if (!hasNext()) {
            return new Token(TokenType.EOF, null, 0, 0);
        }
        return tokens.get(index);
    }

    public boolean matches(TokenType type) {
        if (peek().type() == type) {
            next();
            return true;
        } else {
            return false;
        }
    }

    public boolean matches(TokenType[] types) {
        for (TokenType type : types) {
            if (peek().type() == type) {
                next();
                return true;
            }
        }
        return false;
    }

    public void consume(TokenType expected) {
        int position = getPosition();
        if (next().type() != expected) {
            errors.add("Expected a " + expected + " but got a " + previous() + " at " + position);
        }
    }

    public int getPosition() {
        return peek().position();
    }

    public void recover(TokenType type) {
        while (peek().type() != type && peek().type() != TokenType.EOF) {
            next();
        }
    }
}
