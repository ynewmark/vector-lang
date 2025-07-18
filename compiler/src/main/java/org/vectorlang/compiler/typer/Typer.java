package org.vectorlang.compiler.typer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.vectorlang.compiler.compiler.BaseType;
import org.vectorlang.compiler.compiler.BinaryTable;
import org.vectorlang.compiler.compiler.UnaryTable;

public class Typer implements Visitor<TyperState, Node> {

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

    public CodeBase type(CodeBase codeBase) {
        TyperState state = new TyperState();
        for (FunctionStatement statement : codeBase.getFunctions()) {
            state.putFunc(statement.getName(), statement.getParameterTypes(), statement.getReturnType());
        }
        FunctionStatement[] functions = new FunctionStatement[codeBase.getFunctions().length];
        for (int i = 0; i < functions.length; i++) {
            functions[i] = (FunctionStatement) codeBase.getFunctions()[i].accept(this, state);
        }
        return new CodeBase(functions);
    }

    @Override
    public Node visitBinaryExpr(BinaryExpression expression, TyperState arg) {
        Expression left = (Expression) expression.getLeft().visitExpression(this, arg);
        Expression right = (Expression) expression.getRight().visitExpression(this, arg);
        if (expression.getOperator() == BinaryOperator.CONCAT) {
            if (left.getType().indexed().equals(right.getType().indexed())) {
                return new BinaryExpression(left, right, expression.getOperator(),
                    left.getType().concat(right.getType()));
            } else {
                failures.add(new TypeFailure(left.getType(), right.getType(), "cannot concat"));
                return new BinaryExpression(left, right, expression.getOperator());
            }
        }
        BaseType result = binaryTable.get(
            left.getType().getBaseType(), right.getType().getBaseType(), expression.getOperator()
        );
        if (result == null) {
            failures.add(new TypeFailure(left.getType(), right.getType(), "operator " + expression.getOperator()));
            return new BinaryExpression(left, right, expression.getOperator());
        }
        Type type = new Type(result, left.getType().getShape(), true);
        return new BinaryExpression(left, right, expression.getOperator(), type);
    }

    @Override
    public Node visitGroupingExpr(GroupingExpression expression, TyperState arg) {
        Expression newExpression = (Expression) expression.getExpression().visitExpression(this, arg);
        return new GroupingExpression(newExpression, newExpression.getType());
    }

    @Override
    public Node visitIdentifierExpr(IdentifierExpression expression, TyperState arg) {
        Type type = arg.get(expression.getName());
        if (type == null) {
            failures.add(new TypeFailure(null, null, expression.getName() + " not found"));
            return expression;
        }
        return new IdentifierExpression(expression.getName(), type);
    }

    @Override
    public Node visitLiteralExpr(LiteralExpression expression, TyperState arg) {
        return expression;
    }

    @Override
    public Node visitUnaryExpr(UnaryExpression expression, TyperState arg) {
        Expression expr = (Expression) expression.getExpression().visitExpression(this, arg);
        BaseType result = unaryTable.get(expr.getType().getBaseType(), expression.getOperator());
        if (result == null) {
            failures.add(new TypeFailure(expr.getType(), null, "operator " + expression.getOperator()));
            return new UnaryExpression(expr, expression.getOperator());
        }
        Type type = new Type(result, expr.getType().getShape(), true);
        return new UnaryExpression(expr, expression.getOperator(), type);
    }

    @Override
    public Node visitVectorExpr(VectorExpression expression, TyperState arg) {
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
        return new VectorExpression(expressions, expressions[0].getType().vectorize(expressions.length).constant());
    }

    @Override
    public Node visitIndexExpr(IndexExpression expression, TyperState arg) {
        Expression newBase = (Expression) expression.getBase().visitExpression(this, arg);
        Expression newIndex = (Expression) expression.getIndex().visitExpression(this, arg);
        if (!newIndex.getType().equals(new Type(BaseType.INT, new Dimension[0], false))) {
            failures.add(new TypeFailure(newIndex.getType(), null, "index must be an integer"));
        }
        return new IndexExpression(newBase, newIndex, newBase.getType().indexed());
    }

    @Override
    public Node visitAssignStmt(AssignStatement node, TyperState arg) {
        Expression expression = (Expression) node.getRightHand().visitExpression(this, arg);
        if (arg.get(node.getLeftHand()) == null) {
            failures.add(new TypeFailure(null, null, node.getLeftHand() + " not found"));
        } else if (!arg.get(node.getLeftHand()).equals(expression.getType())) {
            failures.add(new TypeFailure(arg.get(node.getLeftHand()), expression.getType(), "types mismatch"));
        }
        return new AssignStatement(node.getLeftHand(), expression);
    }

    @Override
    public Node visitBlockStmt(BlockStatement node, TyperState arg) {
        List<Statement> statements = new ArrayList<>();
        TyperState inner = new TyperState(arg);
        for (Statement statement : node.getStatements()) {
            statements.add((Statement) statement.visitStatement(this, inner));
        }
        return new BlockStatement(statements.toArray(new Statement[0]));
    }

    @Override
    public Node visitDeclareStmt(DeclareStatement node, TyperState arg) {
        Expression initial = (Expression) node.getInitial().visitExpression(this, arg);
        if (node.getType() != null && !node.getType().equals(initial.getType())) {
            failures.add(new TypeFailure(node.getType(), initial.getType(), "types mismatch"));
        }
        Type type = node.isConst() ? initial.getType().constant() : initial.getType();
        arg.put(node.getName(), type);
        return new DeclareStatement(node.isConst(), node.getName(), initial, type);
    }

    @Override
    public Node visitPrintStmt(PrintStatement node, TyperState arg) {
        Expression expression = (Expression) node.getExpression().visitExpression(this, arg);
        return new PrintStatement(expression);
    }
    
    @Override
    public Node visitIfStmt(IfStatement node, TyperState arg) {
        Expression condition = (Expression) node.getCondition().visitExpression(this, arg);
        if (!condition.getType().equals(new Type(BaseType.BOOL, new Dimension[0], false))) {
            failures.add(new TypeFailure(condition.getType(), null, "condition must be a boolean"));
        }
        Statement ifStatement = (Statement) node.getIfStatement().visitStatement(this, arg);
        Statement elseStatement = null;
        if (node.getElseStatement() != null) {
            elseStatement = (Statement) node.getElseStatement().visitStatement(this, arg);
        }
        return new IfStatement(ifStatement, elseStatement, condition);
    }

    @Override
    public Node visitWhileStmt(WhileStatement node, TyperState arg) {
        Expression condition = (Expression) node.getCondition().visitExpression(this, arg);
        if (!condition.getType().equals(new Type(BaseType.BOOL, new Dimension[0], false))) {
            failures.add(new TypeFailure(condition.getType(), null, "condition must be a boolean"));
        }
        Statement body = (Statement) node.getBody().visitStatement(this, arg);
        return new WhileStatement(condition, body);
    }

    @Override
    public Node visitForStmt(ForStatement node, TyperState arg) {
        TyperState state = new TyperState(arg);
        Statement initial = (Statement) node.getInitial().visitStatement(this, state);
        AssignStatement each = (AssignStatement) node.getEach().visitStatement(this, state);
        Expression condition = (Expression) node.getCondition().visitExpression(this, state);
        if (!condition.getType().equals(new Type(BaseType.BOOL, new Dimension[0], false))) {
            failures.add(new TypeFailure(condition.getType(), null, "condition must be a boolean"));
        }
        Statement body = (Statement) node.getBody().visitStatement(this, state);
        return new ForStatement(condition, initial, each, body);
    }

    @Override
    public Node visitStaticExpr(StaticExpression expression, TyperState arg) {
        return expression;
    }

    @Override
    public Node visitCallExpression(CallExpression expression, TyperState arg) {
        Expression[] args = new Expression[expression.getArgs().length];
        Map<String, Integer> constraints = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            args[i] = (Expression) expression.getArgs()[i].visitExpression(this, arg);
        }
        FuncType funcType = arg.getFunc(expression.getName());
        if (funcType == null) {
            failures.add(new TypeFailure(null, null,
                "function " + expression.getName() + " not found")
            );
            return new CallExpression(expression.getName(), args, null);
        }
        if (funcType.argTypes().length != args.length) {
            failures.add(new TypeFailure(null, null, expression.getName() + " wrong number of arguments"));
        } else {
            for (int i = 0; i < args.length; i++) {
                if (!args[i].getType().getBaseType().equals(funcType.argTypes()[i].getBaseType())
                    || !funcType.argTypes()[i].match(constraints, args[i].getType())) {
                    failures.add(
                        new TypeFailure(funcType.argTypes()[i], args[i].getType(), "mismatched argument")
                    );
                }
            }
        }
        Type returnType = funcType.returnType().constrain(constraints);
        if (returnType == null) {
            failures.add(
                new TypeFailure(funcType.returnType(), null, "unable to constrain")
            );
        }
        return new CallExpression(expression.getName(), args, returnType);
    }

    @Override
    public Node visitFunctionStmt(FunctionStatement node, TyperState arg) {
        TyperState state = new TyperState(arg);
        for (int i = 0; i < node.getParameterNames().length; i++) {
            state.put(node.getParameterNames()[i], node.getParameterTypes()[i]);
        }
        Statement[] statements = new Statement[node.getBody().length];
        for (int i = 0; i < statements.length; i++) {
            statements[i] = (Statement) node.getBody()[i].visitStatement(this, state);
        }
        if (node.getReturnType() != null && !returnCheck(statements, node.getReturnType())) {
            failures.add(new TypeFailure(null, null, "function " + node.getName() + " does not return in all cases"));
        }
        return new FunctionStatement(
            node.getName(), node.getParameterNames(), node.getParameterTypes(), statements,
            node.getReturnType()
        );
    }

    @Override
    public Node visitReturnStmt(ReturnStatement node, TyperState arg) {
        Expression expression = (Expression) node.getExpression().visitExpression(this, arg);
        return new ReturnStatement(expression);
    }

    private boolean returnCheck(Statement[] statements, Type returnType) {
        if (statements[statements.length - 1] instanceof ReturnStatement) {
            return ((ReturnStatement) statements[statements.length - 1]).getExpression().getType().equals(returnType);
        } else if (statements[statements.length - 1] instanceof IfStatement) {
            IfStatement ifStatement = (IfStatement) statements[statements.length - 1];
            return returnCheck(rollout(ifStatement.getIfStatement()), returnType)
                && returnCheck(rollout(ifStatement.getElseStatement()), returnType);
        } else {
            return false;
        }
    }

    private Statement[] rollout(Statement statement) {
        if (statement instanceof BlockStatement) {
            return ((BlockStatement) statement).getStatements();
        } else {
            return new Statement[]{statement};
        }
    }
}
