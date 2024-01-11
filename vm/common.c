#include <stdio.h>

#include "common.h"

void dump(union Item *pointer, union Item *head, char *name) {
    union Item *current = pointer - 1;
    printf("[%s Dump (size: %lu)]\n", name, pointer - head);
    while (current >= head) {
        if (current->data.flag) {
            printf("Pointer, size: %u, width: %u\n", current->data.size, current->data.width);
            current--;
            printf(" %p\n", current->pointer);
            current--;
        } else {
            printf("Immediate, size: %u, width: %u\n", current->data.size, current->data.width);
            int size = current->data.size;
            current -= size;
            for (int i = 0; i < size; i++) {
                printf(" Value: %lu\n", current[i].value);
            }
            current--;
        }
    }
    printf("Address: %p\n\n", (void *) head);
}
