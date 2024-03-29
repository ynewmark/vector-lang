package org.vectorlang.compiler.compiler;

import java.util.function.BiFunction;
import java.util.function.Function;

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
import org.vectorlang.compiler.ast.Node;
import org.vectorlang.compiler.ast.PrintStatement;
import org.vectorlang.compiler.ast.ReturnStatement;
import org.vectorlang.compiler.ast.Statement;
import org.vectorlang.compiler.ast.StaticExpression;
import org.vectorlang.compiler.ast.UnaryExpression;
import org.vectorlang.compiler.ast.UnaryOperator;
import org.vectorlang.compiler.ast.VectorExpression;
import org.vectorlang.compiler.ast.Visitor;
import org.vectorlang.compiler.ast.WhileStatement;

public class Pruner implements Visitor<Void, Node> {

    private final static UnaryTable<Function<LiteralExpression, LiteralExpression>> unaryTable;
    private final static BinaryTable<BiFunction<LiteralExpression, LiteralExpression, LiteralExpression>> binaryTable;

    static {
        unaryTable = new UnaryTable<>();
        binaryTable = new BinaryTable<>();
        unaryTable.put(BaseType.BOOL, UnaryOperator.NEGATE, generateBool((Boolean bool) -> !bool));
        unaryTable.put(BaseType.INT, UnaryOperator.INVERSE, generateInt((Integer i) -> -i));
        unaryTable.put(BaseType.FLOAT, UnaryOperator.INVERSE, generateFloat((Double d) -> -d));
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.AND, generateBool(Boolean::logicalAnd));
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.OR, generateBool(Boolean::logicalOr));
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.ADD, generateInt((a, b) -> a + b));
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.SUBTRACT, generateInt((a, b) -> a - b));
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.MULTIPLY, generateInt((a, b) -> a * b));
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.DIVIDE, generateInt((a, b) -> a / b));
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.ADD, generateFloat((a, b) -> a + b));
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.SUBTRACT, generateFloat((a, b) -> a - b));
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.MULTIPLY, generateFloat((a, b) -> a * b));
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.DIVIDE, generateFloat((a, b) -> a / b));
    }

    public CodeBase prune(CodeBase codeBase) {
        FunctionStatement[] functions = new FunctionStatement[codeBase.getFunctions().length];
        for (int i = 0; i < functions.length; i++) {
            functions[i] = (FunctionStatement) codeBase.getFunctions()[i].accept(this, null);
        }
        return new CodeBase(functions);
    }

    @Override
    public Node visitBinaryExpr(BinaryExpression expression, Void arg) {
        Expression left = (Expression) expression.getLeft().visitExpression(this, null);
        Expression right = (Expression) expression.getRight().visitExpression(this, null);
        if (left instanceof LiteralExpression && right instanceof LiteralExpression) {
            BiFunction<LiteralExpression, LiteralExpression, LiteralExpression> function = binaryTable.get(
                left.getType().getBaseType(), right.getType().getBaseType(), expression.getOperator()
            );
            if (function != null) {
                return function.apply((LiteralExpression) left, (LiteralExpression) right);
            }
        }
        if (left instanceof StaticExpression && right instanceof StaticExpression) {
            StaticExpression leftStatic = (StaticExpression) left;
                StaticExpression rightStatic = (StaticExpression) right;
            if (expression.getOperator() == BinaryOperator.CONCAT) {
                LiteralExpression[] expressions = new LiteralExpression[
                    leftStatic.getData().length + rightStatic.getData().length
                ];
                System.arraycopy(leftStatic.getData(), 0, expressions, 0, leftStatic.getData().length);
                System.arraycopy(rightStatic.getData(), 0, expressions, leftStatic.getData().length, rightStatic.getData().length);
                return new StaticExpression(expressions, expression.getType());
            }
            BiFunction<LiteralExpression, LiteralExpression, LiteralExpression> function = binaryTable.get(
                left.getType().getBaseType(), right.getType().getBaseType(), expression.getOperator()
            );
            if (function != null) {
                LiteralExpression[] expressions = new LiteralExpression[leftStatic.getData().length];
                for (int i = 0; i < expressions.length; i++) {
                    expressions[i] = function.apply(leftStatic.getData()[i], rightStatic.getData()[i]);
                }
                return new StaticExpression(expressions, expression.getType());
            }
        }
        return new BinaryExpression(left, right, expression.getOperator(), expression.getType());
    }

    @Override
    public Node visitGroupingExpr(GroupingExpression expression, Void arg) {
        return new GroupingExpression((Expression) expression.getExpression().visitExpression(this, null), expression.getType());
    }

    @Override
    public Node visitIdentifierExpr(IdentifierExpression expression, Void arg) {
        return expression;
    }

    @Override
    public Node visitLiteralExpr(LiteralExpression expression, Void arg) {
        return expression;
    }

    @Override
    public Node visitUnaryExpr(UnaryExpression expression, Void arg) {
        Expression operand = (Expression) expression.getExpression().visitExpression(this, null);
        if (operand instanceof LiteralExpression) {
            Function<LiteralExpression, LiteralExpression> function = unaryTable.get(
                operand.getType().getBaseType(), expression.getOperator()
            );
            if (function != null) {
                return function.apply((LiteralExpression) operand);
            }
        }
        if (operand instanceof StaticExpression) {
            Function<LiteralExpression, LiteralExpression> function = unaryTable.get(
                operand.getType().getBaseType(), expression.getOperator()
            );
            if (function != null) {
                StaticExpression staticExpression = (StaticExpression) operand;
                LiteralExpression[] expressions = new LiteralExpression[staticExpression.getData().length];
                for (int i = 0; i < expressions.length; i++) {
                    expressions[i] = function.apply(staticExpression.getData()[i]);
                }
                return new StaticExpression(expressions, expression.getType());
            }
        }
        return new UnaryExpression(operand, expression.getOperator(), expression.getType());
    }

    @Override
    public Node visitVectorExpr(VectorExpression expression, Void arg) {
        Expression[] elements = new Expression[expression.getExpressions().length];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = (Expression) expression.getExpressions()[i].visitExpression(this, null);
        }
        for (Expression element : elements) {
            if (!(element instanceof LiteralExpression || element instanceof StaticExpression)) {
                return new VectorExpression(elements, expression.getType());
            }
        }
        if (elements[0] instanceof LiteralExpression) {
            long[] data = new long[elements.length];
            LiteralExpression[] literalExpressions = new LiteralExpression[elements.length];
            for (int i = 0; i < elements.length; i++) {
                data[i] = ((LiteralExpression) elements[i]).getRaw();
                literalExpressions[i] = (LiteralExpression) elements[i];
            }
            return new StaticExpression(literalExpressions, elements[0].getType().vectorize(elements.length));
        } else {
            int size = elements[0].getType().getSize();
            LiteralExpression[] data = new LiteralExpression[elements.length * size];
            for (int i = 0; i < elements.length; i++) {
                LiteralExpression[] source = ((StaticExpression) elements[i]).getData();
                System.arraycopy(source, 0, data, i * size, source.length);
            }
            return new StaticExpression(data, elements[0].getType().vectorize(elements.length));
        }
    }

    @Override
    public Node visitIndexExpr(IndexExpression expression, Void arg) {
        Expression base = (Expression) expression.getBase().visitExpression(this, null);
        Expression index = (Expression) expression.getIndex().visitExpression(this, null);
        return new IndexExpression(base, index, expression.getType());
    }

    @Override
    public Node visitStaticExpr(StaticExpression expression, Void arg) {
        return expression;
    }

    @Override
    public Node visitAssignStmt(AssignStatement node, Void arg) {
        Expression rightHand = (Expression) node.getRightHand().visitExpression(this, null);
        return new AssignStatement(node.getLeftHand(), rightHand);
    }

    @Override
    public Node visitBlockStmt(BlockStatement node, Void arg) {
        Statement[] statements = new Statement[node.getStatements().length];
        for (int i = 0; i < statements.length; i++) {
            statements[i] = (Statement) node.getStatements()[i].visitStatement(this, null);
        }
        return new BlockStatement(statements);
    }

    @Override
    public Node visitDeclareStmt(DeclareStatement node, Void arg) {
        Expression initial = (Expression) node.getInitial().visitExpression(this, null);
        return new DeclareStatement(node.isConst(), node.getName(), initial, node.getType());
    }

    @Override
    public Node visitPrintStmt(PrintStatement node, Void arg) {
        Expression expression = (Expression) node.getExpression().visitExpression(this, null);
        return new PrintStatement(expression);
    }

    @Override
    public Node visitIfStmt(IfStatement node, Void arg) {
        Expression condition = (Expression) node.getCondition().visitExpression(this, null);
        Statement ifStatement = (Statement) node.getIfStatement().visitStatement(this, null);
        Statement elseStatement = node.getElseStatement() == null ? null
        : (Statement) node.getElseStatement().visitStatement(this, null);
        return new IfStatement(ifStatement, elseStatement, condition);
    }

    @Override
    public Node visitWhileStmt(WhileStatement node, Void arg) {
        Statement body = (Statement) node.getBody().visitStatement(this, null);
        Expression condition = (Expression) node.getCondition().visitExpression(this, null);
        return new WhileStatement(condition, body);
    }

    @Override
    public Node visitForStmt(ForStatement node, Void arg) {
        Statement body = (Statement) node.getBody().visitStatement(this, null);
        Expression condition = (Expression) node.getCondition().visitExpression(this, null);
        AssignStatement each = (AssignStatement) node.getEach().visitStatement(this, null);
        Statement initial = (Statement) node.getInitial().visitStatement(this, null);
        return new ForStatement(condition, initial, each, body);
    }

    static private Function<LiteralExpression, LiteralExpression> generateBool(Function<Boolean, Boolean> function) {
        return (LiteralExpression expr) -> {
            return new LiteralExpression(function.apply(expr.getBool()));
        };
    }

    static private Function<LiteralExpression, LiteralExpression> generateInt(Function<Integer, Integer> function) {
        return (LiteralExpression expr) -> {
            return new LiteralExpression(function.apply(expr.getInt()));
        };
    }

    static private Function<LiteralExpression, LiteralExpression> generateFloat(Function<Double, Double> function) {
        return (LiteralExpression expr) -> {
            return new LiteralExpression(function.apply(expr.getFloat()));
        };
    }

    static private BiFunction<LiteralExpression, LiteralExpression, LiteralExpression> generateBool(BiFunction<Boolean, Boolean, Boolean> function) {
        return (LiteralExpression expr1, LiteralExpression expr2) -> {
            return new LiteralExpression(function.apply(expr1.getBool(), expr2.getBool()));
        };
    }

    static private BiFunction<LiteralExpression, LiteralExpression, LiteralExpression> generateInt(BiFunction<Integer, Integer, Integer> function) {
        return (LiteralExpression expr1, LiteralExpression expr2) -> {
            return new LiteralExpression(function.apply(expr1.getInt(), expr2.getInt()));
        };
    }

    static private BiFunction<LiteralExpression, LiteralExpression, LiteralExpression> generateFloat(BiFunction<Double, Double, Double> function) {
        return (LiteralExpression expr1, LiteralExpression expr2) -> {
            return new LiteralExpression(function.apply(expr1.getFloat(), expr2.getFloat()));
        };
    }

    @Override
    public Node visitCallExpression(CallExpression expression, Void arg) {
        Expression[] args = new Expression[expression.getArgs().length];
        for (int i = 0; i < args.length; i++) {
            args[i] = (Expression) expression.getArgs()[i].visitExpression(this, null);
        }
        return new CallExpression(expression.getName(), args, expression.getType());
    }

    @Override
    public Node visitFunctionStmt(FunctionStatement node, Void arg) {
        Statement[] statements = new Statement[node.getBody().length];
        for (int i = 0; i < statements.length; i++) {
            statements[i] = (Statement) node.getBody()[i].visitStatement(this, null);
        }
        return new FunctionStatement(
            node.getName(), node.getParameterNames(), node.getParameterTypes(), statements,
            node.getReturnType()
        );
    }

    @Override
    public Node visitReturnStmt(ReturnStatement node, Void arg) {
        Expression expression = (Expression) node.getExpression().visitExpression(this, null);
        return new ReturnStatement(expression);
    }
}
