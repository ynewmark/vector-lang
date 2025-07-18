#ifndef H_INSTRUCTION
#define H_INSTRUCTION

void instr_load(unsigned long *instr_p, int offset);

unsigned long instr_get();

int instr_addr();

void instr_jump(int offset);

#endif
