package org.vectorlang.compiler.parser;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String text;
    private int index;
    private final List<Token> tokens;

    public Lexer(String text) {
        this.text = text;
        this.index = 0;
        this.tokens = new ArrayList<>();
    }

    public List<Token> lex() {
        while (hasNext()) {
            char next = next();
            if (TokenType.getDouble(next, peek()) != null) {
                char temp = next();
                addToken(TokenType.getDouble(next, temp), 2);
            } else if (TokenType.getSingle(next) != null) {
                addToken(TokenType.getSingle(next), 1);
            } else if (Character.isDigit(next)) {
                addNumberToken(next);
            } else if (!Character.isWhitespace(next)) {
                addTextToken(next);
            }
        }
        return this.tokens;
    }

    private char next() {
        return text.charAt(index++);
    }

    private char peek() {
        if (!hasNext()) {
            return '\0';
        }
        return text.charAt(index);
    }

    private boolean hasNext() {
        return index < text.length();
    }

    private void addNumberToken(char head) {
        StringBuilder builder = new StringBuilder();
        builder.append(head);
        while (Character.isDigit(peek())) {
            builder.append(next());
        }
        if (peek() == '.') {
            builder.append(next());
            while (Character.isDigit(peek())) {
                builder.append(next());
            }
            addToken(TokenType.FLOAT_LITERAL, builder.toString());
        } else {
            addToken(TokenType.INT_LITERAL, builder.toString());
        }
    }

    private void addTextToken(char head) {
        StringBuilder builder = new StringBuilder();
        builder.append(head);
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            builder.append(next());
        }
        String result = builder.toString();
        if (TokenType.getKeyword(result) != null) {
            addToken(TokenType.getKeyword(result), result.length());
        } else {
            addToken(TokenType.IDENTIFIER, result);
        }
    }

    private void addToken(TokenType type, int length) {
        this.tokens.add(new Token(type, null, index - length, length));
    }

    private void addToken(TokenType type, String value) {
        this.tokens.add(new Token(type, value, index - value.length(), value.length()));
    }
}
