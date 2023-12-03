#include <string.h>
#include <stdio.h>

#include "calculate.h"
#include "heap.h"
#include "stack.h"
#include "consts.h"
#include "opcode.h"

unsigned long *instr_p, *static_p;
int counter;

int get_operand(void **pointer) {
    struct Metadata item = stack_pop()->data;
    if (item.flag == 0) {
        *pointer = stack_drop(item.size);
        return item.size;
    } else {
        union StackItem *item2 = stack_pop();
        *pointer = item2->pointer;
        return item.size;
    }
}

void step() {
    unsigned long instr = instr_p[counter++];
    if (instr < 24) {
        void *operand1, *operand2, *destination;
        int size;
        size = get_operand(&operand2);
        get_operand(&operand1);
        destination = stack_pointer();
        calc_binary(instr, operand1, operand2, destination, size);
        union StackItem item;
        item.data.flag = 0;
        item.data.size = size;
        stack_skip(size);
        stack_push(item);
    } else if (instr < 27) {
        void *operand, *destination;
        int size;
        size = get_operand(&operand);
        destination = stack_pointer();
        calc_unary(instr, operand, destination, size);
        union StackItem item;
        item.data.size = size;
        item.data.flag = 0;
        stack_skip(size);
        stack_push(item);
    } else if (instr == OP_DEBUG) {
        void *operand;
        int size;
        size = get_operand(&operand);
        for (int i = 0; i < size; i++) {
            printf("0x%lx\n", ((unsigned long *) operand)[i]);
        }
    } else if (instr == OP_CONCAT) {
        stack_concat();
    } else {
        unsigned long arg = instr_p[counter++];
        if (instr == OP_JMP) {
            counter = arg;
        } else if (instr == OP_JIF) {
            unsigned long *condition;
            get_operand((void **) &condition);
            if (*((char *) condition)) {
                counter = arg;
            }
        } else if (instr == OP_STORE) {
            void *source;
            int size;
            size = get_operand(&source);
            memcpy(heap_pointer(arg), source, size * sizeof(union StackItem));
        } else if (instr == OP_LOAD) {
            union StackItem item;
            item.pointer = heap_pointer(arg);
            stack_push(item);
            struct Metadata data;
            data.flag = 1;
            data.size = heap_get_size(arg);
            item.data = data;
            stack_push(item);
        } else if (instr == OP_ALLOC) {
            heap_alloc(arg);
        } else if (instr == OP_INDEX) {
            void *array;
            unsigned long *index;
            get_operand((void **) &index);
            get_operand(&array);
            stack_push_all(((union StackItem *) array) + ((*index) * arg), arg);
        } else if (instr == OP_PUSH) {
            union StackItem item;
            item.value = arg;
            stack_push(item);
        } else if (instr == OP_PUSHI) {
            union StackItem item;
            item.value = arg;
            stack_push(item);
            item.data.flag = 0;
            item.data.size = 1;
            stack_push(item);
        } else if (instr == OP_PRINT) {
            void *operand;
            int size;
            size = get_operand(&operand);
            printf("[Print]\n");
            for (int i = 0; i < size; i++) {
                if (arg == TYPE_INT) {
                    printf("%i\n", ((int *) operand)[i * 2]);
                } else if (arg == TYPE_FLOAT) {
                    printf("%f\n", ((double *) operand)[i]);
                } else if (arg == TYPE_BOOL) {
                    if (((char *) operand)[i * 8]) {
                        printf("true\n");
                    } else {
                        printf("false\n");
                    }
                } else if (arg == TYPE_CHAR) {
                    printf("%c\n", ((char *) operand)[i * 8]);
                }
            }
            printf("\n");
        } else if (instr == OP_LOADS) {
            union StackItem item;
            item.pointer = arg + static_p + 1;
            stack_push(item);
            item.data.flag = 1;
            item.data.size = *(arg + static_p);
            stack_push(item);
        }
    }
}

void execute(unsigned long *stat, unsigned long *instructions, int size) {
    init_stack(100);
    init_heap(10);
    counter = 0;
    static_p = stat;
    instr_p = instructions;
    while (counter < size) {
        step();
        if (DEBUG_MODE) {
            stack_dump();
        }
    }
}
