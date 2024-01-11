#ifndef H_COMMON
#define H_COMMON

struct Metadata {
    unsigned int size;
    unsigned short width;
    unsigned char flag;
};

union Item {
    struct Metadata data;
    unsigned long value;
    void *pointer;
};

void dump(union Item *pointer, union Item *head, char *name);

#endif
