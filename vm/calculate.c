#include "calculate.h"
#include "stack.h"
#include "opcode.h"

void calc_unary(unsigned long opcode, void *operand, void *destination, int size) {
    int index = 0;
    while ((long unsigned int) index < (size * sizeof(union StackItem))) {
        if (opcode == OP_NEG) {
            *(((int *) destination) + index) = -*((((int *) operand)) + index);
            index = index + 2;
        } else if (opcode == OP_F_NEG) {
            *(((double *) destination) + index) = -*((((double *) operand)) + index);
            index = index + 1;
        } else if (opcode == OP_NOT) {
            if (*((((char *) operand)) + index) == 0) {
                *(((char *) destination) + index) = 1;
            } else {
                *(((char *) destination) + index) = 0;
            }
            index = index + 8;
        }
    }
}

void calc_binary(unsigned long opcode, void *operand1, void *operand2, void *destination, int size) {
    int index = 0;
    while ((long unsigned int) index < (size * sizeof(union StackItem))) {
        if (opcode == OP_ADD) {
            *(((int *) destination) + index) = *((((int *) operand1)) + index) + *((((int *) operand2)) + index);
            index = index + 2;
        } else if (opcode == OP_SUB) {
            *(((int *) destination) + index) = *((((int *) operand1)) + index) - *((((int *) operand2)) + index);
            index = index + 2;
        } else if (opcode == OP_MULT) {
            *(((int *) destination) + index) = *((((int *) operand1)) + index) * *((((int *) operand2)) + index);
            index = index + 2;
        } else if (opcode == OP_DIV) {
            *(((int *) destination) + index) = *((((int *) operand1)) + index) / *((((int *) operand2)) + index);
            index = index + 2;
        } else if (opcode == OP_F_ADD) {
            *(((double *) destination) + index) = *((((double *) operand1)) + index) + *((((double *) operand2)) + index);
            index = index + 1;
        } else if (opcode == OP_F_SUB) {
            *(((double *) destination) + index) = *((((double *) operand1)) + index) - *((((double *) operand2)) + index);
            index = index + 1;
        } else if (opcode == OP_F_MULT) {
            *(((double *) destination) + index) = *((((double *) operand1)) + index) * *((((double *) operand2)) + index);
            index = index + 1;
        } else if (opcode == OP_F_DIV) {
            *(((double *) destination) + index) = *((((double *) operand1)) + index) / *((((double *) operand2)) + index);
            index = index + 1;
        } else if (opcode == OP_AND) {
            *(((char *) destination) + index) = *((((char *) operand1)) + index) & *((((char *) operand2)) + index);
            index = index + 8;
        } else if (opcode == OP_OR) {
            *(((char *) destination) + index) = *((((char *) operand1)) + index) | *((((char *) operand2)) + index);
            index = index + 8;
        }
    }
}
