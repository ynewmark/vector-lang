#include "instruction.h"

unsigned long *instr_p;
unsigned long instr_offset;

void instr_load(unsigned long *pointer, int offset) {
    instr_p = pointer;
    instr_offset = offset;
}

unsigned long instr_get() {
    return instr_p[instr_offset++];
}

int instr_addr() {
    return instr_offset;
}

void instr_jump(int offset) {
    instr_offset = offset;
}
