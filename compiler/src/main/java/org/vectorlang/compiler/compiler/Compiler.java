package org.vectorlang.compiler.compiler;

import org.vectorlang.compiler.ast.AssignStatement;
import org.vectorlang.compiler.ast.BinaryExpression;
import org.vectorlang.compiler.ast.BinaryOperator;
import org.vectorlang.compiler.ast.BlockStatement;
import org.vectorlang.compiler.ast.DeclareStatement;
import org.vectorlang.compiler.ast.Expression;
import org.vectorlang.compiler.ast.GroupingExpression;
import org.vectorlang.compiler.ast.IdentifierExpression;
import org.vectorlang.compiler.ast.IfStatement;
import org.vectorlang.compiler.ast.IndexExpression;
import org.vectorlang.compiler.ast.LiteralExpression;
import org.vectorlang.compiler.ast.PrintStatement;
import org.vectorlang.compiler.ast.Statement;
import org.vectorlang.compiler.ast.UnaryExpression;
import org.vectorlang.compiler.ast.UnaryOperator;
import org.vectorlang.compiler.ast.VectorExpression;
import org.vectorlang.compiler.ast.Visitor;

public class Compiler implements Visitor<State, Chunk> {

    private static UnaryTable<OpCode> unaryTable;
    private static BinaryTable<OpCode> binaryTable;

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
    }

    @Override
    public Chunk visitBinaryExpr(BinaryExpression expression, State arg) {
        Chunk left = expression.getLeft().visitExpression(this, arg);
        Chunk right = expression.getRight().visitExpression(this, arg);
        BaseType type = expression.getType().getBaseType();
        return left.concat(right).concat(new long[]{
            binaryTable.get(type, type, expression.getOperator()).ordinal()
        });
    }

    @Override
    public Chunk visitGroupingExpr(GroupingExpression expression, State arg) {
        return expression.getExpression().visitExpression(this, arg);
    }

    @Override
    public Chunk visitIdentifierExpr(IdentifierExpression expression, State arg) {
        return new Chunk(new long[]{OpCode.LOAD.ordinal(), arg.getId(expression.getName())});
    }

    @Override
    public Chunk visitLiteralExpr(LiteralExpression expression, State arg) {
        return new Chunk(new long[]{
            OpCode.PUSHI.ordinal(), expression.getRaw()
        });
    }

    @Override
    public Chunk visitUnaryExpr(UnaryExpression expression, State arg) {
        Chunk chunk = expression.getExpression().visitExpression(this, arg);
        return chunk.concat(new long[]{
            unaryTable.get(expression.getType().getBaseType(), expression.getOperator()).ordinal()
        });
    }

    @Override
    public Chunk visitVectorExpr(VectorExpression expression, State arg) {
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
    public Chunk visitIndexExpr(IndexExpression expression, State arg) {
        Chunk chunk = expression.getBase().visitExpression(this, arg);
        chunk = chunk.concat(expression.getIndex().visitExpression(this, arg));
        return chunk.concat(new long[]{
            OpCode.INDEX.ordinal(), expression.getBase().getType().indexed().getSize()
        });
    }

    @Override
    public Chunk visitAssignStmt(AssignStatement node, State arg) {
        Chunk chunk = node.getRightHand().visitExpression(this, arg);
        return chunk.concat(new long[]{
            OpCode.STORE.ordinal(), arg.getId(node.getLeftHand())
        });
    }

    @Override
    public Chunk visitBlockStmt(BlockStatement node, State arg) {
        State state = new State(arg);
        Chunk chunk = new Chunk(new long[0]);
        for (Statement statement : node.getStatements()) {
            chunk = chunk.concat(statement.visitStatement(this, state));
        }
        return chunk;
    }

    @Override
    public Chunk visitDeclareStmt(DeclareStatement node, State arg) {
        Chunk chunk = new Chunk(new long[0]);
        if (node.getInitial() != null) {
            chunk = node.getInitial().visitExpression(this, arg);
        }
        arg.put(node.getName(), null);
        chunk = chunk.concat(new long[]{
            OpCode.ALLOC.ordinal(), node.getInitial().getType().getSize()
        });
        if (node.getInitial() != null) {
            chunk = chunk.concat(new long[]{
                OpCode.STORE.ordinal(), arg.getId(node.getName())
            });
        }
        return chunk;
    }

    @Override
    public Chunk visitPrintStmt(PrintStatement node, State arg) {
        return node.getExpression().visitExpression(this, arg).concat(new long[]{
            OpCode.PRINT.ordinal(), node.getExpression().getType().getBaseType().ordinal()
        });
    }

    @Override
    public Chunk visitIfStmt(IfStatement node, State arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIfStmt'");
    }
    
}
