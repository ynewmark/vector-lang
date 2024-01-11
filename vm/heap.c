#include <stdlib.h>
#include <stdio.h>

#include "heap.h"
#include "stack.h"
#include "common.h"

union Item *heap_p, *heap_head;

void init_heap(unsigned int size) {
    heap_p = malloc(size * 8);
    heap_head = heap_p;
}

void *heap_alloc(int size, short width) {
    void *temp = heap_p;
    heap_p += size + 1;
    temp += (size * 8);
    struct Metadata data;
    data.flag = 0;
    data.size = size;
    data.width = width;
    *((struct Metadata *) temp) = data;
    return temp;
}

void heap_drop_to(void *drop_to) {
    heap_p = drop_to;
}

void *heap_pointer() {
    return heap_p;
}

void heap_dump() {
    dump(heap_p, heap_head, "Heap");
}
