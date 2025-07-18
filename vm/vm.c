#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "common.h"
#include "calculate.h"
#include "heap.h"
#include "stack.h"
#include "opcode.h"
#include "instruction.h"

unsigned long *static_p;

int get_operand(void **pointer, short *width) {
    struct Metadata item = stack_pop()->data;
    if (width != NULL) {
        *width = item.width;
    }
    if (item.flag == 0) {
        *pointer = stack_drop(item.size);
    } else {
        union Item *item2 = stack_pop();
        *pointer = item2->pointer;
    }
    return item.size;
}

int get_data(struct Metadata *item, void **pointer, short *width) {
    if (width != NULL) {
        *width = item->width;
    }
    if (item->flag == 0) {
        *pointer = item - item->size;
    } else {
        union Item *item2 = item - 1;
        *pointer = item2->pointer;
    }
    return item->size;
}

void binary(unsigned long instr) {
    void *operand1, *operand2, *destination;
    int size;
    short width1, width2;
    size = get_operand(&operand2, &width2);
    get_operand(&operand1, &width1);
    destination = stack_pointer();
    calc_binary(instr, operand1, operand2, destination, width1, width2, 8, size);
    union Item item;
    item.data.flag = 0;
    item.data.width = 8;
    item.data.size = size;
    stack_skip(size);
    stack_push(item);
}

void unary(unsigned long instr) {
    void *operand, *destination;
    int size;
    short width;
    size = get_operand(&operand, &width);
    destination = stack_pointer();
    calc_unary(instr, operand, destination, width, 8, size);
    union Item item;
    item.data.size = size;
    item.data.width = 8;
    item.data.flag = 0;
    stack_skip(size);
    stack_push(item);
}

void concat() {
    void *top;
    int size = get_operand(&top, NULL);
    struct Metadata data = stack_pop()->data;
    if (data.flag == 0) {
        memcpy(stack_pointer(), top, size * sizeof(union Item));
        stack_skip(size);
        data.size += size;
        union Item item;
        item.data = data;
        stack_push(item);
    } else {
        void *bottom = stack_pop()->pointer;
        memcpy(stack_pointer(), bottom, data.size * sizeof(union Item));
        stack_skip(data.size);
        memcpy(stack_pointer(), top, size * sizeof(union Item));
        stack_skip(size);
        data.flag = 0;
        data.size += size;
        union Item item;
        item.data = data;
        stack_push(item);
    }
}

char ret() {
    struct CallFrame *frame = call_stack_pop();
    if (call_stack_state()) {
        return 1;
    }
    void *temp, *source;
    int size = get_operand(&source, NULL);
    for (int i = 0; i < frame->arg_count; i++) {
        get_operand(&temp, NULL);
    }
    stack_push_all(source, size);
    heap_drop_to(frame->heap_p);
    instr_jump(frame->return_addr);
    return 0;
}

void store() {
    void *dest, *source;
    int size;
    short width;
    get_operand(&dest, &width);
    size = get_operand(&source, NULL);
    struct Metadata *pointer = *((struct Metadata **) dest);
    memcpy(pointer - size, source, size * sizeof(union Item));
}

void load() {
    struct Metadata **data;
    void *pointer;
    int size;
    short width;
    get_operand(&data, NULL);
    union Item item;
    size = get_data(*data, &pointer, &width);
    item.pointer = pointer;
    stack_push(item);
    item.data.flag = 1;
    item.data.size = size;
    item.data.width = width;
    stack_push(item);
}

void print(unsigned long arg) {
    char *operand;
    int size, current = 0;
    short width;
    size = get_operand((void **) &operand, &width);
    printf("[Print]\n");
    while (current < size * 8) {
        if (arg == TYPE_INT) {
            printf("%i\n", *((int *) operand));
        } else if (arg == TYPE_FLOAT) {
            printf("%f\n", *((double *) operand));
        } else if (arg == TYPE_BOOL) {
            if (*operand) {
                printf("true\n");
            } else {
                printf("false\n");
            }
        } else if (arg == TYPE_CHAR) {
            printf("%c\n", *operand);
        }
        current += width;
        operand += width;
    }
    printf("\n");
}

void alloc(unsigned long arg) {
    void *pointer = heap_alloc(arg, 8);
    struct CallFrame *frame = call_stack_peek();
    frame->locals[frame->local_index++] = pointer;
}

void instr_index(unsigned long arg) {
    void *array;
    unsigned int *index;
    get_operand((void **) &index, NULL);
    get_operand(&array, NULL);
    stack_push_all(((union Item *) array) + ((*index) * arg), arg);
}

void pushi(unsigned long arg) {
    union Item item;
    item.value = arg;
    stack_push(item);
    item.data.flag = 0;
    item.data.size = 1;
    item.data.width = 8;
    stack_push(item);
}

void stat(unsigned long arg) {
    union Item item;
    item.value = arg + static_p;
    stack_push(item);
    item.data.flag = 0;
    item.data.size = 1;
    item.data.width = 8;
    stack_push(item);
}

void initframe(unsigned long arg) {
    struct CallFrame *frame;
    frame = call_stack_peek();
    frame->stack_p = stack_pointer() - sizeof(union Item);
    frame->heap_p = heap_pointer();
    frame->local_count = arg;
    frame->local_index = 0;
    frame->locals = malloc(sizeof(void *) * arg);
}

void argu(unsigned long arg) {
    struct Metadata *pointer = call_stack_peek()->stack_p;
    for (unsigned int i = 0; i < arg; i++) {
        pointer -= pointer->size + 1;
    }
    union Item item;
    item.pointer = pointer;
    stack_push(item);
    item.data.flag = 0;
    item.data.size = 1;
    item.data.width = 8;
    stack_push(item);
}

void local(unsigned long arg) {
    union Item item;
    item.pointer = ((union Item *) call_stack_peek()->locals[arg]);
    stack_push(item);
    item.data.flag = 0;
    item.data.size = 1;
    item.data.width = 8;
    stack_push(item);
}

void call(unsigned long arg) {
    struct CallFrame frame;
    frame.return_addr = instr_addr();
    call_stack_push(frame);
    instr_jump(arg);
}

char step(char debug) {
    unsigned long instr = instr_get();
    if (instr < 24) {
        binary(instr);
    } else if (instr < 27) {
        unary(instr);
    } else if (instr == OP_CONCAT) {
        concat();
    } else if (instr == OP_RET) {
        return ret();
    } else if (instr == OP_STORE) {
        store();
    } else if (instr == OP_LOAD) {
        load();
    } else {
        unsigned long arg = instr_get();
        if (instr == OP_PRINT) {
            print(arg);
        } else if (instr == OP_ALLOC) {
            alloc(arg);
        } else if (instr == OP_INDEX) {
            instr_index(arg);
        } else if (instr == OP_PUSHI) {
            pushi(arg);
        } else if (instr == OP_STATIC) {
            stat(arg);
        } else if (instr == OP_INITFRAME) {
            initframe(arg);
        } else if (instr == OP_ARG) {
            argu(arg);
        } else if (instr == OP_LOCAL) {
            local(arg);
        } else if (instr == OP_CALL) {
            call(arg);
        } else if (instr == OP_JMP) {
            instr_jump(arg);
        } else if (instr == OP_JIF) {
            unsigned long *condition;
            get_operand((void **) &condition, NULL);
            if (*((char *) condition)) {
                instr_jump(arg);
            }
        } else if (instr == OP_ARGSET) {
            call_stack_peek()->arg_count = arg;
        }
    }
    return 0;
}

void execute(unsigned long *stat, unsigned long *instructions, int start, char debug) {
    init_stack(1000);
    init_call_stack(1000);
    init_heap(1000);
    static_p = stat;
    instr_load(instructions, start);
    struct CallFrame frame;
    frame.return_addr = 0;
    call_stack_push(frame);
    while (!step(debug)) {
        if (debug) {
            stack_dump();
            heap_dump();
        }
    }
}
