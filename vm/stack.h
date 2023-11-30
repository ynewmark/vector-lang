#ifndef H_STACK
#define H_STACK

struct Metadata {
    unsigned int size;
    unsigned int flag;
};

union StackItem {
    struct Metadata data;
    unsigned long value;
    void *pointer;
};

void init_stack(unsigned int size);

void stack_push(union StackItem item);

void stack_push_all(void *source, unsigned int size);

union StackItem *stack_pop();

void *stack_pointer();

void *stack_drop(unsigned int size);

void stack_skip(unsigned int size);

void stack_concat();

void stack_dump();

#endif
