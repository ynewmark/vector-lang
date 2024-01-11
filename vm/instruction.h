#ifndef H_INSTRUCTION
#define H_INSTRUCTION

void instr_load(unsigned long *instr_p);

unsigned long instr_get();

void instr_jump(unsigned long offset);

#endif
