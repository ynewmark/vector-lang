#include <stdlib.h>

#include "heap.h"
#include "stack.h"

int current;
unsigned int *sizes;
void **pointers;

void init_heap(unsigned int count) {
    sizes = malloc(count * sizeof(unsigned int));
    pointers = malloc(count * sizeof(void *));
    current = 0;
}

int heap_alloc(int size) {
    sizes[current] = size;
    pointers[current] = malloc(size * sizeof(union StackItem));
    return current++;
}

void heap_dealloc(int id) {
    sizes[id] = 0;
    free(pointers[id]);
    pointers[id] = 0;
}

void *heap_pointer(int id) {
    return pointers[id];
}

int heap_get_size(int id) {
    return sizes[id];
}
