#ifndef H_CALCULATE
#define H_CALCULATE

void calc_unary(unsigned long opcode, void *operand, void *destination, int size);

void calc_binary(unsigned long opcode, void *operand1, void *operand2, void *destination, int size);

#endif
