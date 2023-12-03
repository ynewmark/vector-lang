package org.vectorlang.compiler.ast;

public interface StatementVisitor<T, R> {
    public R visitAssignStmt(AssignStatement node, T arg);
    public R visitBlockStmt(BlockStatement node, T arg);
    public R visitDeclareStmt(DeclareStatement node, T arg);
    public R visitPrintStmt(PrintStatement node, T arg);
    public R visitIfStmt(IfStatement node, T arg);
    public R visitWhileStmt(WhileStatement node, T arg);
    public R visitForStmt(ForStatement node, T arg);
    public R visitFunctionStmt(FunctionStatement node, T arg);
    public R visitReturnStmt(ReturnStatement node, T arg);
}
