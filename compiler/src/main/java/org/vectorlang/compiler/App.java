package org.vectorlang.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.vectorlang.compiler.ast.BlockStatement;
import org.vectorlang.compiler.ast.Node;
import org.vectorlang.compiler.compiler.Chunk;
import org.vectorlang.compiler.compiler.Compiler;
import org.vectorlang.compiler.compiler.Pruner;
import org.vectorlang.compiler.compiler.TypeFailure;
import org.vectorlang.compiler.compiler.Typer;
import org.vectorlang.compiler.compiler.TyperState;
import org.vectorlang.compiler.parser.Lexer;
import org.vectorlang.compiler.parser.ParseException;
import org.vectorlang.compiler.parser.Parser;
import org.vectorlang.compiler.parser.Token;

public class App {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Provide a file to compile");
            System.exit(1);
        }
        File file = new File(args[0]);
        FileReader reader;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            bufferedReader.lines().forEach((String line) -> {
                builder.append(line).append('\n');
            });
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("File " + args[0] + " not found");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Failed to close file");
            System.exit(1);
        }
        String code = builder.toString();
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.lex();
        Parser parser = new Parser(tokens);
        BlockStatement block;
        try {
            block = parser.parse();
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("There was a parse error");
            System.exit(1);
            return;
        }
        Typer typer = new Typer();
        Pruner pruner = new Pruner();
        org.vectorlang.compiler.compiler.Compiler compiler = new Compiler();
        Node typed = block.accept(typer, new TyperState());
        if (!typer.getFailures().isEmpty()) {
            System.err.println("There were the following type failures:");
            for (TypeFailure failure : typer.getFailures()) {
                System.err.println(failure);
            }
            System.exit(1);
        }
        if (shouldOptimize(args)) {
            System.out.println("Optimizing...");
            typed = typed.accept(pruner, null);
        }
        Chunk chunk = compiler.compile(typed);
        System.out.println("[Program]");
        System.out.println(chunk);
        if (args.length == 2 || args.length == 3) {
            File destination = new File(args[1]);
            try {
                FileOutputStream stream = new FileOutputStream(destination);
                stream.write(chunk.assemble());
                stream.close();
            } catch (IOException e) {
                System.err.println("Cannot write to file " + destination);
                System.exit(1);
            }
        }
    }

    private static boolean shouldOptimize(String[] args) {
        if (args.length < 3) {
            return false;
        }
        return args[2].equals("-o");
    }
}
