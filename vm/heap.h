#ifndef H_HEAP
#define H_HEAP

void init_heap(unsigned int count);

void *heap_alloc(int size, short width);

void heap_drop_to(void *pointer);

void *heap_pointer();

void heap_dump();

#endif
