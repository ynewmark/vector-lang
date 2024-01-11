#ifndef H_STACK
#define H_STACK

struct CallFrame {
    void *heap_p;
    void *stack_p;
    int return_addr;
    int arg_count;
    int local_count;
    int local_index;
    void **locals;
};

void init_stack(unsigned int size);

void stack_push(union Item item);

void stack_push_all(void *source, unsigned int size);

union Item *stack_pop();

void *stack_pointer();

void *stack_drop(unsigned int size);

void stack_skip(unsigned int size);

void stack_concat();

void stack_dump();

void init_call_stack(unsigned int size);

void call_stack_push(struct CallFrame);

struct CallFrame *call_stack_pop();

struct CallFrame *call_stack_peek();

char call_stack_state();

#endif
