package org.vectorlang.compiler.compiler;

public enum OpCode {
    ADD, SUB, MULT, DIV, F_ADD, F_SUB, F_MULT, F_DIV, C_ADD, C_SUB, AND, OR, EQ, NEQ,
    B_EQ, B_NEQ, F_EQ, F_NEQ, C_EQ, C_NEQ, LT, LTE, GT, GTE, F_LT, F_LTE, F_GT, F_GTE, C_LT, C_LTE, C_GT, C_GTE,
    NEG, F_NEG, NOT,
    CONCAT, RET, STORE, LOAD, PRINT, ALLOC, INDEX, PUSHI,
    STATIC, INITFRAME, ARG, LOCAL, CALL, JMP, JIF, ARGSET
}
