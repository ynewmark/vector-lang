all: compiler vm

compiler: compiler/**
	mkdir -p build/
	mvn jar:jar -f ./compiler/pom.xml
	cp ./compiler/target/vector-compiler-*.jar build/compiler.jar

vm: vm/*.h vm/*.c
	mkdir -p build/
	gcc -g -o build/vector -Wall -Wextra -pedantic vm/main.c vm/vm.c vm/calculate.c vm/heap.c vm/stack.c

clean:
	rm -rf build/
