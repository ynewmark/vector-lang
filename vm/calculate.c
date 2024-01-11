#include "calculate.h"
#include "opcode.h"

void calc_unary(unsigned long opcode, char *operand, char *destination, int width, int dest_width, int size) {
    int current = 0;
    while (current < size * 8) {
        if (opcode == OP_NEG) {
            *((int *) destination) = -*((int *) operand);
        } else if (opcode == OP_F_NEG) {
            *((double *) destination) = -*((double *) operand);
        } else if (opcode == OP_NOT) {
            if (*((char *) operand) == 0) {
                *((char *) destination) = 1;
            } else {
                *((char *) destination) = 0;
            }
        }
        current += width;
        operand += width;
        destination += dest_width;
    }
}

void calc_binary(unsigned long opcode, char *operand1, char *operand2, char *destination, int width1, int width2, int dest_width, int size) {
    int current = 0;
    while (current < size * 8) {
        if (opcode == OP_ADD) {
            *((int *) destination) = *((int *) operand1) + *((int *) operand2);
        } else if (opcode == OP_SUB) {
            *((int *) destination) = *((int *) operand1) - *((int *) operand2);
        } else if (opcode == OP_MULT) {
            *((int *) destination) = *((int *) operand1) * *((int *) operand2);
        } else if (opcode == OP_DIV) {
            *((int *) destination) = *((int *) operand1) / *((int *) operand2);
        } else if (opcode == OP_F_ADD) {
            *((double *) destination) = *((double *) operand1) + *((double *) operand2);
        } else if (opcode == OP_F_SUB) {
            *((double *) destination) = *((double *) operand1) - *((double *) operand2);
        } else if (opcode == OP_F_MULT) {
            *((double *) destination) = *((double *) operand1) * *((double *) operand2);
        } else if (opcode == OP_F_DIV) {
            *((double *) destination) = *((double *) operand1) / *((double *) operand2);
        } else if (opcode == OP_AND) {
            *((char *) destination) = *((char *) operand1) & *((char *) operand2);
        } else if (opcode == OP_OR) {
            *((char *) destination) = *((char *) operand1) | *((char *) operand2);
        } else if (opcode == OP_EQ) {
            *((char *) destination) = *((int *) operand1) == *((int *) operand2);
        } else if (opcode == OP_NEQ) {
            *((char *) destination) = *((int *) operand1) != *((int *) operand2);
        } else if (opcode == OP_B_EQ) {
            *((char *) destination) = *((char *) operand1) == *((char *) operand2);
        } else if (opcode == OP_B_NEQ) {
            *((char *) destination) = *((char *) operand1) != *((char *) operand2);
        } else if (opcode == OP_F_EQ) {
            *((char *) destination) = *((double *) operand1) == *((double *) operand2);
        } else if (opcode == OP_F_NEQ) {
            *((char *) destination) = *((double *) operand1) != *((double *) operand2);
        } else if (opcode == OP_LT) {
            *((char *) destination) = *((int *) operand1) < *((int *) operand2);
        } else if (opcode == OP_LTE) {
            *((char *) destination) = *((int *) operand1) <= *((int *) operand2);
        } else if (opcode == OP_GT) {
            *((char *) destination) = *((int *) operand1) > *((int *) operand2);
        } else if (opcode == OP_GTE) {
            *((char *) destination) = *((int *) operand1) >= *((int *) operand2);
        } else if (opcode == OP_F_LT) {
            *((char *) destination) = *((double *) operand1) < *((double *) operand2);
        } else if (opcode == OP_F_LTE) {
            *((char *) destination) = *((double *) operand1) <= *((double *) operand2);
        } else if (opcode == OP_F_GT) {
            *((char *) destination) = *((double *) operand1) > *((double *) operand2);
        } else if (opcode == OP_F_GTE) {
            *((char *) destination) = *((double *) operand1) >= *((double *) operand2);
        }
        current += width1;
        operand1 += width1;
        operand2 += width2;
        destination += dest_width;
    }
}
