package org.vectorlang.compiler.compiler;

import org.vectorlang.compiler.ast.AssignStatement;
import org.vectorlang.compiler.ast.BinaryExpression;
import org.vectorlang.compiler.ast.BinaryOperator;
import org.vectorlang.compiler.ast.BlockStatement;
import org.vectorlang.compiler.ast.DeclareStatement;
import org.vectorlang.compiler.ast.Expression;
import org.vectorlang.compiler.ast.ForStatement;
import org.vectorlang.compiler.ast.GroupingExpression;
import org.vectorlang.compiler.ast.IdentifierExpression;
import org.vectorlang.compiler.ast.IfStatement;
import org.vectorlang.compiler.ast.IndexExpression;
import org.vectorlang.compiler.ast.LiteralExpression;
import org.vectorlang.compiler.ast.Node;
import org.vectorlang.compiler.ast.PrintStatement;
import org.vectorlang.compiler.ast.Statement;
import org.vectorlang.compiler.ast.UnaryExpression;
import org.vectorlang.compiler.ast.UnaryOperator;
import org.vectorlang.compiler.ast.VectorExpression;
import org.vectorlang.compiler.ast.Visitor;
import org.vectorlang.compiler.ast.WhileStatement;

public class Compiler implements Visitor<CompilerState, Chunk> {

    private static UnaryTable<OpCode> unaryTable;
    private static BinaryTable<OpCode> binaryTable;

    private Counter labelCounter;

    static {
        unaryTable = new UnaryTable<>();
        binaryTable = new BinaryTable<>();
        unaryTable.put(BaseType.BOOL, UnaryOperator.NEGATE, OpCode.NOT);
        unaryTable.put(BaseType.INT, UnaryOperator.INVERSE, OpCode.NEG);
        unaryTable.put(BaseType.FLOAT, UnaryOperator.INVERSE, OpCode.F_NEG);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.AND, OpCode.AND);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.OR, OpCode.OR);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.ADD, OpCode.ADD);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.SUBTRACT, OpCode.SUB);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.MULTIPLY, OpCode.MULT);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.DIVIDE, OpCode.DIV);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.ADD, OpCode.F_ADD);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.SUBTRACT, OpCode.F_SUB);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.MULTIPLY, OpCode.F_MULT);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.DIVIDE, OpCode.F_DIV);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.EQUAL, OpCode.EQ);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.NOT_EQUAL, OpCode.NEQ);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.LESS_THAN, OpCode.LT);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.EQUAL_LESS_THAN, OpCode.LTE);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.GREATER_THAN, OpCode.GT);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.EQUAL_GREATER_THAN, OpCode.GTE);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.EQUAL, OpCode.F_EQ);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.NOT_EQUAL, OpCode.F_NEQ);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.LESS_THAN, OpCode.F_LT);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.EQUAL_LESS_THAN, OpCode.F_LTE);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.GREATER_THAN, OpCode.F_GT);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.EQUAL_GREATER_THAN, OpCode.F_GTE);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.EQUAL, OpCode.B_EQ);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.NOT_EQUAL, OpCode.B_NEQ);
    }

    public Compiler() {
        labelCounter = new Counter();
    }

    public Chunk compile(Node node) {
        return node.accept(this, new CompilerState(null, labelCounter)).link();
    }

    @Override
    public Chunk visitBinaryExpr(BinaryExpression expression, CompilerState arg) {
        Chunk left = expression.getLeft().visitExpression(this, arg);
        Chunk right = expression.getRight().visitExpression(this, arg);
        BaseType type = expression.getLeft().getType().getBaseType();
        return left.concat(right).concat(new long[]{
            binaryTable.get(type, type, expression.getOperator()).ordinal()
        });
    }

    @Override
    public Chunk visitGroupingExpr(GroupingExpression expression, CompilerState arg) {
        return expression.getExpression().visitExpression(this, arg);
    }

    @Override
    public Chunk visitIdentifierExpr(IdentifierExpression expression, CompilerState arg) {
        return new Chunk(new long[]{OpCode.LOAD.ordinal(), arg.get(expression.getName())});
    }

    @Override
    public Chunk visitLiteralExpr(LiteralExpression expression, CompilerState arg) {
        return new Chunk(new long[]{
            OpCode.PUSHI.ordinal(), expression.getRaw()
        });
    }

    @Override
    public Chunk visitUnaryExpr(UnaryExpression expression, CompilerState arg) {
        Chunk chunk = expression.getExpression().visitExpression(this, arg);
        return chunk.concat(new long[]{
            unaryTable.get(expression.getType().getBaseType(), expression.getOperator()).ordinal()
        });
    }

    @Override
    public Chunk visitVectorExpr(VectorExpression expression, CompilerState arg) {
        boolean flag = false;
        Chunk chunk = new Chunk(new long[0]);
        for (Expression element : expression.getExpressions()) {
            chunk = chunk.concat(element.visitExpression(this, arg));
            if (flag) {
                chunk = chunk.concat(new long[]{OpCode.CONCAT.ordinal()});
            }
            flag = true;
        }
        return chunk;
    }

    @Override
    public Chunk visitIndexExpr(IndexExpression expression, CompilerState arg) {
        Chunk chunk = expression.getBase().visitExpression(this, arg);
        chunk = chunk.concat(expression.getIndex().visitExpression(this, arg));
        return chunk.concat(new long[]{
            OpCode.INDEX.ordinal(), expression.getBase().getType().indexed().getSize()
        });
    }

    @Override
    public Chunk visitAssignStmt(AssignStatement node, CompilerState arg) {
        Chunk chunk = node.getRightHand().visitExpression(this, arg);
        return chunk.concat(new long[]{
            OpCode.STORE.ordinal(), arg.get(node.getLeftHand())
        });
    }

    @Override
    public Chunk visitBlockStmt(BlockStatement node, CompilerState arg) {
        CompilerState state = new CompilerState(arg, labelCounter);
        Chunk chunk = new Chunk(new long[0]);
        for (Statement statement : node.getStatements()) {
            chunk = chunk.concat(statement.visitStatement(this, state));
        }
        return chunk;
    }

    @Override
    public Chunk visitDeclareStmt(DeclareStatement node, CompilerState arg) {
        Chunk chunk = new Chunk(new long[0]);
        if (node.getInitial() != null) {
            chunk = node.getInitial().visitExpression(this, arg);
        }
        arg.put(node.getName());
        chunk = chunk.concat(new long[]{
            OpCode.ALLOC.ordinal(), node.getInitial().getType().getSize()
        });
        if (node.getInitial() != null) {
            chunk = chunk.concat(new long[]{
                OpCode.STORE.ordinal(), arg.get(node.getName())
            });
        }
        return chunk;
    }

    @Override
    public Chunk visitPrintStmt(PrintStatement node, CompilerState arg) {
        return node.getExpression().visitExpression(this, arg).concat(new long[]{
            OpCode.PRINT.ordinal(), node.getExpression().getType().getBaseType().ordinal()
        });
    }

    @Override
    public Chunk visitIfStmt(IfStatement node, CompilerState arg) {
        Chunk ifChunk = node.getIfStatement().visitStatement(this, arg);
        int elseLength = 0;
        Chunk elseChunk = new Chunk(new long[0]);
        if (node.getElseStatement() != null) {
            elseChunk = node.getElseStatement().visitStatement(this, arg);
            elseLength = elseChunk.length();
        }
        Chunk chunk = node.getCondition().visitExpression(this, arg).concat(new long[]{
            OpCode.JIF.ordinal(), arg.addLabel()
        }, new long[]{2 + elseLength + 2});
        chunk = chunk.concat(elseChunk).concat(new long[]{
            OpCode.JMP.ordinal(), arg.addLabel()
        }, new long[]{2 + ifChunk.length()});
        chunk = chunk.concat(ifChunk);
        return chunk;
    }

    @Override
    public Chunk visitWhileStmt(WhileStatement node, CompilerState arg) {
        Chunk bodyChunk = node.getBody().visitStatement(this, arg);
        Chunk chunk = new Chunk(new long[]{
            OpCode.JMP.ordinal(), arg.addLabel()
        }, new long[]{2 + bodyChunk.length()}).concat(bodyChunk);
        Chunk condChunk = node.getCondition().visitExpression(this, arg);
        chunk = chunk.concat(condChunk).concat(new long[]{
            OpCode.JIF.ordinal(), arg.addLabel()
        }, new long[]{-(condChunk.length() + bodyChunk.length())});
        return chunk;
    }

    @Override
    public Chunk visitForStmt(ForStatement node, CompilerState arg) {
        Chunk chunk = node.getInitial().visitStatement(this, arg);
        Chunk bodyChunk = node.getBody().visitStatement(this, arg).concat(
            node.getEach().visitStatement(this, arg)
        );
        chunk = chunk.concat(new long[]{
            OpCode.JMP.ordinal(), arg.addLabel()
        }, new long[]{2 + bodyChunk.length()}).concat(bodyChunk);
        Chunk condChunk = node.getCondition().visitExpression(this, arg);
        chunk = chunk.concat(condChunk).concat(new long[]{
            OpCode.JIF.ordinal(), arg.addLabel()
        }, new long[]{-(condChunk.length() + bodyChunk.length())});
        return chunk;
    }
    
}
