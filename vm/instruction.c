#include "instruction.h"

unsigned long *instr_p;
unsigned long instr_offset;

void instr_load(unsigned long *pointer) {
    instr_p = pointer;
    instr_offset = 0;
}

unsigned long instr_get() {
    return instr_p[instr_offset++];
}

void instr_jump(unsigned long offset) {
    instr_offset = offset;
}
