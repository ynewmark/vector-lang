#ifndef H_CALCULATE
#define H_CALCULATE

void calc_unary(unsigned long opcode, char *operand, char *destination, int width, int dest_width, int size);

void calc_binary(unsigned long opcode, char *operand1, char *operand2, char *destination, int width1, int width2, int dest_width, int size);

#endif
