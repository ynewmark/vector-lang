#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "vm.h"

int main(int argc, char **argv) {
    if (argc < 2) {
        printf("Provide a program to execute\n");
        exit(1);
    }
    FILE *fp = fopen(argv[1], "r");
    if (fp == NULL) {
        printf("Couldn't read file\n");
        exit(1);
    }
    char debug = 0;
    if (argc >= 3 && strcmp(argv[3], "--debug")) {
        debug = 1;
    }
    void *static_p, *instr_p;
    fseek(fp, 0L, SEEK_END);
    long size = ftell(fp);
    fseek(fp, 0L, SEEK_SET);
    unsigned long *program = malloc(size);
    int program_size = size / sizeof(long);
    fread(program, sizeof(long), program_size, fp);
    fclose(fp);
    static_p = program + program[1];
    instr_p = program + program[2];
    execute(static_p, instr_p, program[3], debug);
}
