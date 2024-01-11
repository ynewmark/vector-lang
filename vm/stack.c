#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "stack.h"
#include "common.h"

union Item *pointer, *head;

struct CallFrame *call_pointer, *call_head;

void init_stack(unsigned int size) {
    pointer = malloc(size * sizeof(union Item));
    head = pointer;
}

void stack_push(union Item item) {
    *pointer = item;
    pointer = pointer + 1;
}

void stack_push_all(void *source, unsigned int size) {
    memcpy(pointer, source, size * sizeof(union Item));
    pointer = pointer + size;
    union Item item;
    item.data.flag = 0;
    item.data.size = size;
    stack_push(item);
}

union Item *stack_pop() {
    pointer = pointer - 1;
    return pointer;
}

void *stack_pointer() {
    return pointer;
}

void *stack_drop(unsigned int size) {
    pointer = pointer - size;
    return pointer;
}

void stack_skip(unsigned int size) {
    pointer = pointer + size;
}

void stack_concat() {
    pointer = pointer - 1;
    struct Metadata data = pointer->data;
    unsigned int size = data.size;
    union Item *temp = pointer - size - 1;
    data.size += temp->data.size;
    for (unsigned int i = 0; i < size; i++) {
        temp[i] = temp[i + 1];
    }
    temp[size].data = data;
}

void stack_dump() {
    dump(pointer, head, "Stack");
}

void init_call_stack(unsigned int size) {
    call_pointer = malloc(size * sizeof(struct CallFrame));
    call_head = call_pointer;
}

void call_stack_push(struct CallFrame item) {
    *((struct CallFrame *) call_pointer) = item;
    call_pointer = call_pointer + 1;
}

struct CallFrame *call_stack_pop() {
    call_pointer = call_pointer - 1;
    free(call_pointer->locals);
    return call_pointer;
}

struct CallFrame *call_stack_peek() {
    return call_pointer - 1;
}

char call_stack_state() {
    return call_pointer == call_head;
}
