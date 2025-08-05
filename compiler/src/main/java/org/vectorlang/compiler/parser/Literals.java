package org.vectorlang.compiler.parser;

public class Literals {
    
    private final static char[] ESCAPED;
    
    static {
        ESCAPED = new char[256];
        ESCAPED['\0'] = '\0';
        ESCAPED['\\'] = '\\';
        ESCAPED['\n'] = '\n';
        ESCAPED['\t'] = '\t';
        ESCAPED['\''] = '\'';
        ESCAPED['\"'] = '\"';
    }

    public static char escape(char character) {
        return ESCAPED[character];
    }
}
