#ifndef H_HEAP
#define H_HEAP

void init_heap(unsigned int count);

int heap_alloc(int size);

void heap_dealloc(int id);

void *heap_pointer(int id);

int heap_get_size(int id);

#endif
