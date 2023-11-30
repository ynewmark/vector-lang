package org.vectorlang.compiler.parser;

public record Token(TokenType type, String value, int position, int length) {
}
