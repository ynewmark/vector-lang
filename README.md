# vector-lang
A vector based programming language

## Layout
- `compiler/` - a Java based compiler for the language
- `examples/` - example programs in the language
- `vm/` - a C virtual machine for the language

## How To Use
1. Run `make all` to make the compiler and vm.
2. Run `java -jar build/compiler.jar {path/to/source.vec} {path/to/compile/to}` (replace with real paths)
3. Run `build/vm {path/to/binary}` (where path/to/binary is the path previously compiled to)

## Optimizations
In order to do basic optimizations, run `java -jar build/compiler.jar {path/to/source.vec} {path/to/compile/to} -o`
These include
- replace operations on literals with result
- place literal vector in static memory

## Debugging
If the VM is run as `build/vm {path/to/binary} --debug`, then the VM will print the instruction and stack / heap dumps after each instruction

## Features
- C-Style Syntax
- Variables
- Functions
- Indexing
- Multidimensional Arrays
- Typing
- Printing
