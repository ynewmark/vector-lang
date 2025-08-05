package org.vectorlang.compiler.compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.vectorlang.compiler.ast.CodeBase;
import org.vectorlang.compiler.parser.Lexer;
import org.vectorlang.compiler.parser.Parser;

public class ImportManager {
    
    private final Map<String, CodeBase> imports;
    private final Path userPath;

    public ImportManager(Path userPath) {
        this.imports = new HashMap<>();
        this.userPath = userPath;
    }

    public CodeBase getImport(String relative) throws IOException {
        if (!imports.containsKey(relative)) {
            Path path = userPath.resolve(relative + ".vec");
            String code = Files.readString(path);
            CodeBase codeBase = (new Parser()).parse((new Lexer(code)).lex());
            imports.put(relative, codeBase);
        }
        return imports.get(relative);
    }

    public void update(String relative, CodeBase codeBase) {
        imports.put(relative, codeBase);
    }
}
