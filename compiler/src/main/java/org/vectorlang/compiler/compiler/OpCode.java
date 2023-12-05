package org.vectorlang.compiler.compiler;

public enum OpCode {
    ADD, SUB, MULT, DIV, F_ADD, F_SUB, F_MULT, F_DIV, AND, OR, EQ, NEQ,
    B_EQ, B_NEQ, F_EQ, F_NEQ, LT, LTE, GT, GTE, F_LT, F_LTE, F_GT, F_GTE,
    NEG, F_NEG, NOT,
    DEBUG, CONCAT, RET, JMP, JIF, STORE, LOAD, ALLOC, INDEX, PUSH, PUSHI,
    PRINT, LOADS, ARG, CALL
}
