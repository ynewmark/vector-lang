#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "stack.h"

union StackItem *pointer, *head;

void init_stack(unsigned int size) {
    pointer = malloc(size * sizeof(union StackItem));
    head = pointer;
}

void stack_push(union StackItem item) {
    *pointer = item;
    pointer = pointer + 1;
}

void stack_push_all(void *source, unsigned int size) {
    memcpy(pointer, source, size * sizeof(union StackItem));
    pointer = pointer + size;
    union StackItem item;
    item.data.flag = 0;
    item.data.size = size;
    stack_push(item);
}

union StackItem *stack_pop() {
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
    union StackItem *temp = pointer - size - 1;
    data.size += temp->data.size;
    for (unsigned int i = 0; i < size; i++) {
        temp[i] = temp[i + 1];
    }
    temp[size].data = data;
}

void stack_dump() {
    union StackItem *current = pointer - 1;
    printf("[Stack Dump (size: %lu)]\n", pointer - head);
    while (current >= head) {
        if (current->data.flag) {
            printf("Pointer, size: %u\n", current->data.size);
            current--;
            printf(" %p\n", current->pointer);
            current--;
        } else {
            printf("Immediate, size: %u\n", current->data.size);
            int size = current->data.size;
            current -= size;
            for (int i = 0; i < size; i++) {
                printf(" Value: %lu\n", current[i].value);
            }
            current--;
        }
    }
    printf("\n");
}
