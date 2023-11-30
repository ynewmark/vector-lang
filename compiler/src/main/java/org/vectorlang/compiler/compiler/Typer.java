package org.vectorlang.compiler.compiler;

import java.util.ArrayList;
import java.util.List;

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
import org.vectorlang.compiler.ast.Node;
import org.vectorlang.compiler.ast.PrintStatement;
import org.vectorlang.compiler.ast.Statement;
import org.vectorlang.compiler.ast.UnaryExpression;
import org.vectorlang.compiler.ast.UnaryOperator;
import org.vectorlang.compiler.ast.VectorExpression;
import org.vectorlang.compiler.ast.Visitor;

public class Typer implements Visitor<State, Node> {

    private static UnaryTable<BaseType> unaryTable;
    private static BinaryTable<BaseType> binaryTable;

    private List<TypeFailure> failures;

    public Typer() {
        this.failures = new ArrayList<>();
    }

    static {
        unaryTable = new UnaryTable<>();
        binaryTable = new BinaryTable<>();
        unaryTable.put(BaseType.BOOL, UnaryOperator.NEGATE, BaseType.BOOL);
        unaryTable.put(BaseType.INT, UnaryOperator.INVERSE, BaseType.INT);
        unaryTable.put(BaseType.FLOAT, UnaryOperator.INVERSE, BaseType.FLOAT);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.AND, BaseType.BOOL);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.OR, BaseType.BOOL);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.ADD, BaseType.INT);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.SUBTRACT, BaseType.INT);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.MULTIPLY, BaseType.INT);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.DIVIDE, BaseType.INT);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.ADD, BaseType.FLOAT);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.SUBTRACT, BaseType.FLOAT);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.MULTIPLY, BaseType.FLOAT);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.DIVIDE, BaseType.FLOAT);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.EQUAL, BaseType.BOOL);
        binaryTable.put(BaseType.BOOL, BaseType.BOOL, BinaryOperator.NOT_EQUAL, BaseType.BOOL);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.EQUAL, BaseType.BOOL);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.NOT_EQUAL, BaseType.BOOL);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.EQUAL, BaseType.BOOL);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.NOT_EQUAL, BaseType.BOOL);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.LESS_THAN, BaseType.BOOL);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.EQUAL_LESS_THAN, BaseType.BOOL);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.GREATER_THAN, BaseType.BOOL);
        binaryTable.put(BaseType.INT, BaseType.INT, BinaryOperator.EQUAL_GREATER_THAN, BaseType.BOOL);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.LESS_THAN, BaseType.BOOL);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.EQUAL_LESS_THAN, BaseType.BOOL);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.GREATER_THAN, BaseType.BOOL);
        binaryTable.put(BaseType.FLOAT, BaseType.FLOAT, BinaryOperator.EQUAL_GREATER_THAN, BaseType.BOOL);
    }

    public List<TypeFailure> getFailures() {
        return failures;
    }

    @Override
    public Node visitBinaryExpr(BinaryExpression expression, State arg) {
        Expression left = (Expression) expression.getLeft().visitExpression(this, arg);
        Expression right = (Expression) expression.getRight().visitExpression(this, arg);
        BaseType result = binaryTable.get(
            left.getType().getBaseType(), right.getType().getBaseType(), expression.getOperator()
        );
        if (result == null) {
            failures.add(new TypeFailure(left.getType(), right.getType(), "operator " + expression.getOperator()));
            return new BinaryExpression(left, right, expression.getOperator(), 0, 0);
        }
        Type type = new Type(new Shape(result, left.getType().getShape()), true);
        return new BinaryExpression(left, right, expression.getOperator(), type, 0, 0);
    }

    @Override
    public Node visitGroupingExpr(GroupingExpression expression, State arg) {
        Expression newExpression = (Expression) expression.getExpression().visitExpression(this, arg);
        return new GroupingExpression(newExpression, newExpression.getType(), 0, 0);
    }

    @Override
    public Node visitIdentifierExpr(IdentifierExpression expression, State arg) {
        Type type = arg.get(expression.getName());
        if (type == null) {
            failures.add(new TypeFailure(null, null, expression.getName() + " not found"));
            return expression;
        }
        return new IdentifierExpression(expression.getName(), type, 0, 0);
    }

    @Override
    public Node visitLiteralExpr(LiteralExpression expression, State arg) {
        return expression;
    }

    @Override
    public Node visitUnaryExpr(UnaryExpression expression, State arg) {
        Expression expr = (Expression) expression.getExpression().visitExpression(this, arg);
        BaseType result = unaryTable.get(expr.getType().getBaseType(), expression.getOperator());
        if (result == null) {
            failures.add(new TypeFailure(expr.getType(), null, "operator " + expression.getOperator()));
            return new UnaryExpression(expr, expression.getOperator(), 0, 0);
        }
        Type type = new Type(new Shape(result, expr.getType().getShape()), true);
        return new UnaryExpression(expr, expression.getOperator(), type, 0, 0);
    }

    @Override
    public Node visitVectorExpr(VectorExpression expression, State arg) {
        if (expression.getExpressions().length == 0) {
            failures.add(new TypeFailure(null, null, "length of 0"));
            return expression;
        }
        Expression[] expressions = new Expression[expression.getExpressions().length];
        expressions[0] = (Expression) expression.getExpressions()[0].visitExpression(this, arg);
        for (int i = 1; i < expressions.length; i++) {
            expressions[i] = (Expression) expression.getExpressions()[i].visitExpression(this, arg);
            if (!expressions[i].getType().equals(expressions[0].getType())) {
                failures.add(new TypeFailure(expressions[0].getType(), expressions[i].getType(), "mismatched vector"));
            }
        }
        return new VectorExpression(expressions, expressions[0].getType().vectorize(expressions.length).constant(),
            0, 0);
    }

    @Override
    public Node visitIndexExpr(IndexExpression expression, State arg) {
        Expression newBase = (Expression) expression.getBase().visitExpression(this, arg);
        Expression newIndex = (Expression) expression.getIndex().visitExpression(this, arg);
        if (!newIndex.getType().equals(new Type(new Shape(BaseType.INT, new int[0]), false))) {
            failures.add(new TypeFailure(newIndex.getType(), null, "index must be an integer"));
        }
        return new IndexExpression(newBase, newIndex, newBase.getType().indexed(), 0, 0);
    }

    @Override
    public Node visitAssignStmt(AssignStatement node, State arg) {
        Expression expression = (Expression) node.getRightHand().visitExpression(this, arg);
        if (arg.get(node.getLeftHand()) == null) {
            failures.add(new TypeFailure(null, null, node.getLeftHand() + " not found"));
        } else if (!arg.get(node.getLeftHand()).equals(expression.getType())) {
            failures.add(new TypeFailure(arg.get(node.getLeftHand()), expression.getType(), "types mismatch"));
        }
        return new AssignStatement(node.getLeftHand(), expression, 0, 0);
    }

    @Override
    public Node visitBlockStmt(BlockStatement node, State arg) {
        List<Statement> statements = new ArrayList<>();
        State inner = new State(arg);
        for (Statement statement : node.getStatements()) {
            statements.add((Statement) statement.visitStatement(this, inner));
        }
        return new BlockStatement(statements.toArray(new Statement[0]), 0, 0);
    }

    @Override
    public Node visitDeclareStmt(DeclareStatement node, State arg) {
        Expression initial = (Expression) node.getInitial().visitExpression(this, arg);
        if (node.getType() != null && !node.getType().equals(initial.getType())) {
            failures.add(new TypeFailure(node.getType(), initial.getType(), "types mismatch"));
        }
        Type type = node.isConst() ? initial.getType().constant() : initial.getType();
        arg.put(node.getName(), type);
        return new DeclareStatement(node.isConst(), node.getName(), initial, type, 0, 0);
    }

    @Override
    public Node visitPrintStmt(PrintStatement node, State arg) {
        Expression expression = (Expression) node.getExpression().visitExpression(this, arg);
        return new PrintStatement(expression, 0, 0);
    }
    
    @Override
    public Node visitIfStmt(IfStatement node, State arg) {
        Expression condition = (Expression) node.getCondition().visitExpression(this, arg);
        if (condition.getType().equals(new Type(new Shape(BaseType.BOOL, new int[0]), false))) {
            failures.add(new TypeFailure(condition.getType(), null, "condition must be a boolean"));
        }
        Statement ifStatement = (Statement) node.getIfStatement().visitStatement(this, arg);
        Statement elseStatement = (Statement) node.getElseStatement().visitStatement(this, arg);
        return new IfStatement(ifStatement, elseStatement, condition, 0, 0);
    }
}
