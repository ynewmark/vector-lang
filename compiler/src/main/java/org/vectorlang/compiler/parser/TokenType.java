package org.vectorlang.compiler.parser;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {
    FLOAT_LITERAL, INT_LITERAL, TRUE, FALSE, IDENTIFIER,
    OPEN_PAREN, CLOSE_PAREN, OPEN_BRACE, CLOSE_BRACE, OPEN_BRACKET, CLOSE_BRACKET, SEMICOLON, EOF,
    WHILE, FUNC, IF, ELSE, FOR, RETURN, COLON, COMMA, LET, CONST, PRINT,
    EQUALS, EQUALS_EQUALS, LEFT_ARROW_EQUALS, LEFT_ARROW, RIGHT_ARROW_EQUALS, RIGHT_ARROW, BANG,
    BANG_EQUALS, DASH, SLASH, STAR, PLUS, AMPERSAND, BAR, DOT_PLUS, DOT_DASH, DOT_SLASH, DOT_STAR,
    PLUS_PLUS, MINUS_MINUS, PLUS_EQUALs, MINUS_EQUALS, STAR_EQUALS, SLASH_EQUALS,
    BAR_EQUALS, AMPERSAND_EQUALS;

    private final static TokenType[] SINGLES;
    private final static TokenType[][] DOUBLES;
    private final static Map<String, TokenType> KEYWORDS;

    public static TokenType getKeyword(String name) {
        return KEYWORDS.get(name);
    }

    public static TokenType getSingle(char character) {
        return SINGLES[character];
    }
    
    public static TokenType getDouble(char first, char second) {
        if (DOUBLES[first] == null) {
            return null;
        }
        return DOUBLES[first][second];
    }

    static {
        SINGLES = new TokenType[256];
        SINGLES['('] = OPEN_PAREN;
        SINGLES[')'] = CLOSE_PAREN;
        SINGLES['{'] = OPEN_BRACE;
        SINGLES['}'] = CLOSE_BRACE;
        SINGLES['['] = OPEN_BRACKET;
        SINGLES[']'] = CLOSE_BRACKET;
        SINGLES[';'] = SEMICOLON;
        SINGLES[':'] = COLON;
        SINGLES['='] = EQUALS;
        SINGLES['<'] = LEFT_ARROW;
        SINGLES['>'] = RIGHT_ARROW;
        SINGLES['!'] = BANG;
        SINGLES['-'] = DASH;
        SINGLES['/'] = SLASH;
        SINGLES['*'] = STAR;
        SINGLES['+'] = PLUS;
        SINGLES['&'] = AMPERSAND;
        SINGLES['|'] = BAR;
        SINGLES[','] = COMMA;
        DOUBLES = new TokenType[256][];
        DOUBLES['<'] = new TokenType[256];
        DOUBLES['<']['='] = LEFT_ARROW_EQUALS;
        DOUBLES['>'] = new TokenType[256];
        DOUBLES['>']['='] = RIGHT_ARROW_EQUALS;
        DOUBLES['!'] = new TokenType[256];
        DOUBLES['!']['='] = BANG_EQUALS;
        DOUBLES['.'] = new TokenType[256];
        DOUBLES['.']['+'] = DOT_PLUS;
        DOUBLES['.']['-'] = DOT_DASH;
        DOUBLES['.']['/'] = DOT_SLASH;
        DOUBLES['.']['*'] = DOT_STAR;
        DOUBLES['='] = new TokenType[256];
        DOUBLES['=']['='] = EQUALS_EQUALS;
        DOUBLES['+'] = new TokenType[256];
        DOUBLES['+']['+'] = PLUS_PLUS;
        DOUBLES['+']['='] = PLUS_EQUALs;
        DOUBLES['-'] = new TokenType[256];
        DOUBLES['-']['-'] = MINUS_MINUS;
        DOUBLES['-']['='] = MINUS_EQUALS;
        DOUBLES['*'] = new TokenType[256];
        DOUBLES['*']['='] = STAR_EQUALS;
        DOUBLES['/'] = new TokenType[256];
        DOUBLES['/']['='] = SLASH_EQUALS;
        DOUBLES['|'] = new TokenType[256];
        DOUBLES['|']['='] = BAR_EQUALS;
        DOUBLES['&'] = new TokenType[256];
        DOUBLES['&']['='] = AMPERSAND_EQUALS;
        KEYWORDS = new HashMap<>();
        KEYWORDS.put("true", TRUE);
        KEYWORDS.put("false", FALSE);
        KEYWORDS.put("while", WHILE);
        KEYWORDS.put("func", FUNC);
        KEYWORDS.put("if", IF);
        KEYWORDS.put("else", ELSE);
        KEYWORDS.put("for", FOR);
        KEYWORDS.put("return", RETURN);
        KEYWORDS.put("let", LET);
        KEYWORDS.put("const", CONST);
        KEYWORDS.put("print", PRINT);
    }
}
