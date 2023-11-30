#include <stdlib.h>
#include <stdio.h>

#include "vm.h"

int main(int argc, char **argv) {
    if (argc != 2) {
        printf("Provide a program to execute\n");
        exit(1);
    }
    FILE *fp = fopen(argv[1], "r");
    if (fp == NULL) {
        printf("Couldn't read file\n");
        exit(1);
    }
    fseek(fp, 0L, SEEK_END);
    long size = ftell(fp);
    fseek(fp, 0L, SEEK_SET);
    unsigned long *program = malloc(size);
    int program_size = size / sizeof(long);
    fread(program, sizeof(long), program_size, fp);
    fclose(fp);
    execute(program, program_size);
}
