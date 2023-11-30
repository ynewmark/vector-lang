# vector-lang
A vector based programming language

## Layout
- `compiler/` - a Java based compiler for the language
- `examples/` - example programs in the language
- `vm/` - a C virtual machine for the language
- `vm/consts.h` - allows for configuring the vm (for now just enabling internal debugging)

## How To Use
1. Run `make all` to make the compiler and vm.
2. Run `java -jar build/compiler.jar {path/to/source.vec} {path/to/compile/to}` (replace with real paths)
3. Run `build/vm {path/to/binary}` (where path/to/binary is the path previously compiled to)

## Features
- C-Style Syntax
- Variables
- Indexing
- Multidimensional Arrays
- Typing
- Printing
