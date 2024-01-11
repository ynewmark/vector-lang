package org.vectorlang.compiler.compiler;

import org.vectorlang.compiler.ast.AssignStatement;
import org.vectorlang.compiler.ast.BinaryExpression;
import org.vectorlang.compiler.ast.BinaryOperator;
import org.vectorlang.compiler.ast.BlockStatement;
import org.vectorlang.compiler.ast.CallExpression;
import org.vectorlang.compiler.ast.CodeBase;
import org.vectorlang.compiler.ast.DeclareStatement;
import org.vectorlang.compiler.ast.Expression;
import org.vectorlang.compiler.ast.ForStatement;
import org.vectorlang.compiler.ast.FunctionStatement;
import org.vectorlang.compiler.ast.GroupingExpression;
import org.vectorlang.compiler.ast.IdentifierExpression;
import org.vectorlang.compiler.ast.IfStatement;
import org.vectorlang.compiler.ast.IndexExpression;
import org.vectorlang.compiler.ast.LiteralExpression;
import org.vectorlang.compiler.ast.PrintStatement;
import org.vectorlang.compiler.ast.ReturnStatement;
import org.vectorlang.compiler.ast.Statement;
import org.vectorlang.compiler.ast.StaticExpression;
import org.vectorlang.compiler.ast.UnaryExpression;
import org.vectorlang.compiler.ast.UnaryOperator;
import org.vectorlang.compiler.ast.VectorExpression;
import org.vectorlang.compiler.ast.Visitor;
import org.vectorlang.compiler.ast.WhileStatement;

public class Compiler implements Visitor<CompilerState, Chunk> {

    private final static UnaryTable<OpCode> unaryTable = new UnaryTable<>();
    private final static BinaryTable<OpCode> binaryTable = new BinaryTable<>();

    static {
        unaryTable.put(BaseType.BOOL, UnaryOperator.NEGATE, OpCode.NOT);
        unaryTable.put(BaseType.INT, UnaryOperator.INVERSE, OpCode.NEG);
        unaryTable.put(BaseType.FLOAT, UnaryOperator.INVERSE, OpCode.F_NEG);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.AND, OpCode.AND);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.OR, OpCode.OR);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.EQUAL, OpCode.B_EQ);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.NOT_EQUAL, OpCode.B_NEQ);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.ADD, OpCode.ADD);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.SUBTRACT, OpCode.SUB);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.MULTIPLY, OpCode.MULT);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.DIVIDE, OpCode.DIV);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.LESS_THAN, OpCode.LT);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.EQUAL_LESS_THAN, OpCode.LTE);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.GREATER_THAN, OpCode.GT);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.EQUAL_GREATER_THAN, OpCode.GTE);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.EQUAL, OpCode.EQ);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.ADD, OpCode.F_ADD);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.SUBTRACT, OpCode.F_SUB);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.MULTIPLY, OpCode.F_MULT);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.DIVIDE, OpCode.F_DIV);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.EQUAL, OpCode.F_EQ);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.LESS_THAN, OpCode.F_LT);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.EQUAL_LESS_THAN, OpCode.F_LTE);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.GREATER_THAN, OpCode.F_GT);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.EQUAL_GREATER_THAN, OpCode.F_GTE);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.CONCAT, OpCode.CONCAT);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.CONCAT, OpCode.CONCAT);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.CONCAT, OpCode.CONCAT);
    }

    public Chunk[] compile(CodeBase codeBase) {
        Chunk[] functions = new Chunk[codeBase.getFunctions().length];
        CompilerState state = new CompilerState(null, new Counter(), new Counter(), new Counter());
        for (FunctionStatement function : codeBase.getFunctions()) {
            state.addFunction(function.getName());
        }
        for (int i = 0; i < functions.length; i++) {
            functions[i] = codeBase.getFunctions()[i].accept(this, state);
        }
        return functions;
    }

    @Override
    public Chunk visitBinaryExpr(BinaryExpression expression, CompilerState arg) {
        Chunk chunk = expression.getLeft().accept(this, arg);
        chunk = chunk.concat(expression.getRight().accept(this, arg));
        return chunk.concat(new long[]{binaryTable.get(
            expression.getLeft().getType().getBaseType(), expression.getRight().getType().getBaseType(), expression.getOperator()
        ).ordinal()});
    }

    @Override
    public Chunk visitGroupingExpr(GroupingExpression expression, CompilerState arg) {
        return expression.getExpression().accept(this, arg);
    }

    @Override
    public Chunk visitIdentifierExpr(IdentifierExpression expression, CompilerState arg) {
        return new Chunk("", new long[]{
            arg.isParameter(expression.getName()) ? OpCode.ARG.ordinal() : OpCode.LOCAL.ordinal(),
            arg.get(expression.getName()), OpCode.LOAD.ordinal()
        });
    }

    @Override
    public Chunk visitLiteralExpr(LiteralExpression expression, CompilerState arg) {
        return new Chunk("", new long[]{OpCode.PUSHI.ordinal(), expression.getRaw()});
    }

    @Override
    public Chunk visitUnaryExpr(UnaryExpression expression, CompilerState arg) {
        Chunk chunk = expression.getExpression().accept(this, arg);
        return chunk.concat(new long[]{unaryTable.get(
            expression.getExpression().getType().getBaseType(), expression.getOperator()
        ).ordinal()});
    }

    @Override
    public Chunk visitVectorExpr(VectorExpression expression, CompilerState arg) {
        Chunk chunk = expression.getExpressions()[0].accept(this, arg);
        for (int i = 1; i < expression.getExpressions().length; i++) {
            chunk = chunk.concat(expression.getExpressions()[i].accept(this, arg)).concat(
                new long[]{OpCode.CONCAT.ordinal()}
            );
        }
        return chunk;
    }

    @Override
    public Chunk visitIndexExpr(IndexExpression expression, CompilerState arg) {
        Chunk chunk = expression.getBase().accept(this, arg);
        chunk = chunk.concat(expression.getIndex().accept(this, arg));
        return chunk.concat(new long[]{
            OpCode.INDEX.ordinal(), expression.getBase().getType().indexed().getSize()
        });
    }

    @Override
    public Chunk visitStaticExpr(StaticExpression expression, CompilerState arg) {
        return new Chunk("", new long[]{
            OpCode.STATIC.ordinal(), arg.addStatic(), OpCode.LOAD.ordinal()
        },
            new long[0], new long[][]{expression.getRaw()});
    }

    @Override
    public Chunk visitCallExpression(CallExpression expression, CompilerState arg) {
        Chunk chunk = new Chunk("");
        for (Expression expr : expression.getArgs()) {
            chunk = chunk.concat(expr.accept(this, arg));
        }
        return chunk.concat(new long[]{OpCode.CALL.ordinal(), arg.getFunction(expression.getName())});
    }

    @Override
    public Chunk visitAssignStmt(AssignStatement node, CompilerState arg) {
        return node.getRightHand().accept(this, arg).concat(storeInstr(arg, node.getLeftHand()));
    }

    @Override
    public Chunk visitBlockStmt(BlockStatement node, CompilerState arg) {
        CompilerState state = new CompilerState(arg, null, null, null);
        Chunk chunk = new Chunk("");
        for (Statement statement : node.getStatements()) {
            chunk = chunk.concat(statement.accept(this, state));
        }
        arg.updateCount(state);
        return chunk;
    }

    @Override
    public Chunk visitDeclareStmt(DeclareStatement node, CompilerState arg) {
        arg.put(node.getName());
        return node.getInitial().accept(this, arg).concat(new long[]{
            OpCode.ALLOC.ordinal(), node.getType().getSize()
        }).concat(storeInstr(arg, node.getName()));
    }

    @Override
    public Chunk visitPrintStmt(PrintStatement node, CompilerState arg) {
        return node.getExpression().accept(this, arg).concat(new long[]{
            OpCode.PRINT.ordinal(), node.getExpression().getType().getBaseType().ordinal()
        });
    }

    @Override
    public Chunk visitIfStmt(IfStatement node, CompilerState arg) {
        CompilerState ifState = new CompilerState(arg, null, null, null);
        CompilerState elseState = new CompilerState(arg, null, null, null);
        Chunk ifChunk = node.getIfStatement().accept(this, ifState);
        int elseLength = 0;
        Chunk elseChunk = new Chunk("", new long[0]);
        if (node.getElseStatement() != null) {
            elseChunk = node.getElseStatement().accept(this, elseState);
            elseLength = elseChunk.getInstrSize();
        }
        Chunk chunk = node.getCondition().accept(this, arg).concat(new long[]{
            OpCode.JIF.ordinal(), arg.addLabel()
        }, new long[]{2 + elseLength + 2});
        chunk = chunk.concat(elseChunk).concat(new long[]{
            OpCode.JMP.ordinal(), arg.addLabel()
        }, new long[]{2 + ifChunk.getInstrSize()});
        chunk = chunk.concat(ifChunk);
        arg.updateCount(ifState);
        arg.updateCount(elseState);
        return chunk;

    }

    @Override
    public Chunk visitWhileStmt(WhileStatement node, CompilerState arg) {
        Chunk bodyChunk = node.getBody().visitStatement(this, arg);
        Chunk chunk = new Chunk("", new long[]{
            OpCode.JMP.ordinal(), arg.addLabel()
        }, new long[]{2 + bodyChunk.getInstrSize()}).concat(bodyChunk);
        Chunk condChunk = node.getCondition().visitExpression(this, arg);
        chunk = chunk.concat(condChunk).concat(new long[]{
            OpCode.JIF.ordinal(), arg.addLabel()
        }, new long[]{-(condChunk.getInstrSize() + bodyChunk.getInstrSize())});
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
        }, new long[]{2 + bodyChunk.getInstrSize()}).concat(bodyChunk);
        Chunk condChunk = node.getCondition().visitExpression(this, arg);
        chunk = chunk.concat(condChunk).concat(new long[]{
            OpCode.JIF.ordinal(), arg.addLabel()
        }, new long[]{-(condChunk.getInstrSize() + bodyChunk.getInstrSize())});
        return chunk;
    }

    @Override
    public Chunk visitFunctionStmt(FunctionStatement node, CompilerState arg) {
        CompilerState state = new CompilerState(arg, new Counter(), null, null);
        for (int i = node.getParameterNames().length - 1; i >= 0; i--) {
            state.putParameter(node.getParameterNames()[i]);
        }
        Chunk chunk = new Chunk(node.getName());
        for (Statement statement : node.getBody()) {
            chunk = chunk.concat(statement.accept(this, state));
        }
        chunk = new Chunk(node.getName(), new long[]{
            OpCode.INITFRAME.ordinal(), arg.getCount(), OpCode.ARGSET.ordinal(), node.getParameterNames().length
        }).concat(chunk);
        return chunk.concat(new long[]{OpCode.RET.ordinal()});
    }

    @Override
    public Chunk visitReturnStmt(ReturnStatement node, CompilerState arg) {
        return node.getExpression().accept(this, arg).concat(new long[]{OpCode.RET.ordinal()});
    }

    private long[] storeInstr(CompilerState state, String name) {
        return new long[]{
            state.isParameter(name) ? OpCode.ARG.ordinal() : OpCode.LOCAL.ordinal(),
            state.get(name), OpCode.STORE.ordinal()
        };
    }
    
}
