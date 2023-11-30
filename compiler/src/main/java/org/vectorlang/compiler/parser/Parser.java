package org.vectorlang.compiler.parser;

import java.util.ArrayList;
import java.util.List;

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
import org.vectorlang.compiler.ast.PrintStatement;
import org.vectorlang.compiler.ast.Statement;
import org.vectorlang.compiler.ast.UnaryExpression;
import org.vectorlang.compiler.ast.UnaryOperator;
import org.vectorlang.compiler.ast.VectorExpression;
import org.vectorlang.compiler.ast.WhileStatement;
import org.vectorlang.compiler.compiler.BaseType;
import org.vectorlang.compiler.compiler.Shape;
import org.vectorlang.compiler.compiler.Type;

public class Parser {
    private final List<Token> tokens;
    private int position;

    public Parser(List<Token> tokens) {
        this.tokens = new ArrayList<>(tokens);
        this.position = 0;
    }

    public BlockStatement parse() throws ParseException {
        List<Statement> statements = new ArrayList<>();
        while (hasNext()) {
            statements.add(statement());
        }
        return new BlockStatement(statements.toArray(new Statement[0]), 0, 0);
    }

    private Statement statement() throws ParseException {
        if (matches(TokenType.OPEN_BRACE)) {
            List<Statement> statements = new ArrayList<>();
            while (!matches(TokenType.CLOSE_BRACE)) {
                statements.add(statement());
            }
            expect(TokenType.CLOSE_BRACE, null);
            return new BlockStatement(statements.toArray(new Statement[0]), 0, 0);
        } else if (matches(TokenType.PRINT)) {
            Expression expression = expression();
            consume(TokenType.SEMICOLON, null);
            return new PrintStatement(expression, 0, 0);
        } else if (matches(TokenType.CONST) || matches(TokenType.LET)) {
            boolean constant = previous().type() == TokenType.CONST;
            consume(TokenType.IDENTIFIER, null);
            String name = previous().value();
            Expression expression = null;
            if (matches(TokenType.EQUALS)) {
                expression = expression();
            }
            consume(TokenType.SEMICOLON, null);
            Type type = type();
            return new DeclareStatement(constant, name, expression, type, 0, 0);
        } else if (peek().type() == TokenType.IF) {
            consume(TokenType.IF, null);
            consume(TokenType.OPEN_PAREN, null);
            Expression condition = expression();
            consume(TokenType.CLOSE_PAREN, null);
            Statement ifStatement = statement();
            Statement elseStatement = null;
            if (matches(TokenType.ELSE)) {
                elseStatement = statement();
            }
            return new IfStatement(ifStatement, elseStatement, condition, 0, 0);
        } else if (peek().type() == TokenType.IDENTIFIER) {
            return assignStatement(true);
        } else if (peek().type() == TokenType.WHILE) {
            return whileStatement();
        } else if (peek().type() == TokenType.FOR) {
            return forStatement();
        } else {
            throw new ParseException(null, 0);
        }
    }

    private AssignStatement assignStatement(boolean semicolon) throws ParseException {
        consume(TokenType.IDENTIFIER, null);
        String name = previous().value();
        Expression expression = null;
        if (matches(TokenType.EQUALS)) {
            expression = expression();
        } else if (matches(TokenType.PLUS_PLUS)) {
            expression = new BinaryExpression(
                new IdentifierExpression(name, 0, 0),
                new LiteralExpression(1, 0, 0), BinaryOperator.ADD,
            0, 0);
        } else if (matches(TokenType.MINUS_MINUS)) {
            expression = new BinaryExpression(
                new IdentifierExpression(name, 0, 0),
                new LiteralExpression(1, 0, 0), BinaryOperator.SUBTRACT,
            0, 0);
        } else {
            BinaryOperator operator = null;
            if (matches(TokenType.PLUS_EQUALs)) {
                operator = BinaryOperator.ADD;
            } else if (matches(TokenType.MINUS_EQUALS)) {
                operator = BinaryOperator.SUBTRACT;
            } else if (matches(TokenType.STAR_EQUALS)) {
                operator = BinaryOperator.MULTIPLY;
            } else if (matches(TokenType.SLASH_EQUALS)) {
                operator = BinaryOperator.DIVIDE;
            } else if (matches(TokenType.BAR_EQUALS)) {
                operator = BinaryOperator.OR;
            } else if (matches(TokenType.AMPERSAND_EQUALS)) {
                operator = BinaryOperator.AND;
            } else {
                throw new ParseException(null, 0);
            }
            expression = new BinaryExpression(
                new IdentifierExpression(name, 0, 0),
                expression(), operator, 0, 0);
        }
        if (semicolon) {
            consume(TokenType.SEMICOLON, null);
        }
        return new AssignStatement(name, expression, 0, 0);
    }

    private WhileStatement whileStatement() throws ParseException {
        consume(TokenType.WHILE, null);
        consume(TokenType.OPEN_PAREN, null);
        Expression condition = expression();
        consume(TokenType.CLOSE_PAREN, null);
        Statement body = statement();
        return new WhileStatement(condition, body, 0, 0);
    }

    private ForStatement forStatement() throws ParseException {
        consume(TokenType.FOR, null);
        consume(TokenType.OPEN_PAREN, null);
        Statement initial = statement();
        Expression condition = expression();
        consume(TokenType.SEMICOLON, null);
        AssignStatement each = assignStatement(false);
        consume(TokenType.CLOSE_PAREN, null);
        Statement body = statement();
        return new ForStatement(condition, initial, each, body, 0, 0);
    }

    private Expression expression() throws ParseException {
        return or();
    }

    private Expression or() throws ParseException {
        Expression expression = and();
        while (matches(TokenType.BAR)) {
            expression = new BinaryExpression(expression, and(), BinaryOperator.OR, 0, 0);
        }
        return expression;
    }

    private Expression and() throws ParseException {
        Expression expression = equality();
        while (matches(TokenType.AMPERSAND)) {
            expression = new BinaryExpression(expression, equality(), BinaryOperator.AND, 0, 0);
        }
        return expression;
    }

    private Expression equality() throws ParseException {
        Expression expression = comparison();
        while (matches(new TokenType[]{TokenType.EQUALS_EQUALS, TokenType.BANG_EQUALS})) {
            BinaryOperator operator = switch (previous().type()) {
                case EQUALS_EQUALS -> BinaryOperator.EQUAL;
                case BANG_EQUALS -> BinaryOperator.NOT_EQUAL;
                default -> null;
            };
            expression = new BinaryExpression(expression, comparison(), operator, 0, 0);
        }
        return expression;
    }

    private Expression comparison() throws ParseException {
        Expression expression = term();
        while (matches(new TokenType[]{TokenType.LEFT_ARROW, TokenType.LEFT_ARROW_EQUALS, TokenType.RIGHT_ARROW, TokenType.RIGHT_ARROW_EQUALS})) {
            BinaryOperator operator = switch (previous().type()) {
                case LEFT_ARROW -> BinaryOperator.LESS_THAN;
                case RIGHT_ARROW -> BinaryOperator.GREATER_THAN;
                case LEFT_ARROW_EQUALS -> BinaryOperator.EQUAL_LESS_THAN;
                case RIGHT_ARROW_EQUALS -> BinaryOperator.EQUAL_GREATER_THAN;
                default -> null;
            };
            expression = new BinaryExpression(expression, term(), operator, 0, 0);
        }
        return expression;
    }

    private Expression term() throws ParseException {
        Expression expression = factor();
        while (matches(new TokenType[]{TokenType.PLUS, TokenType.DASH, TokenType.DOT_PLUS, TokenType.DOT_DASH})) {
            BinaryOperator operator = switch (previous().type()) {
                case PLUS -> BinaryOperator.ADD;
                case DASH -> BinaryOperator.SUBTRACT;
                case DOT_PLUS -> BinaryOperator.DOT_ADD;
                case DOT_DASH -> BinaryOperator.DOT_SUBTRACT;
                default -> null;
            };
            expression = new BinaryExpression(expression, factor(), operator, 0, 0);
        }
        return expression;
    }

    private Expression factor() throws ParseException {
        Expression expression = unary();
        while (matches(new TokenType[]{TokenType.STAR, TokenType.SLASH, TokenType.DOT_STAR, TokenType.DOT_SLASH})) {
            BinaryOperator operator = switch (previous().type()) {
                case STAR -> BinaryOperator.MULTIPLY;
                case SLASH -> BinaryOperator.DIVIDE;
                case DOT_STAR -> BinaryOperator.DOT_MULTIPLY;
                case DOT_SLASH -> BinaryOperator.DOT_DIVIDE;
                default -> null;
            };
            expression = new BinaryExpression(expression, unary(), operator, 0, 0);
        }
        return expression;
    }

    private Expression unary() throws ParseException {
        if (matches(TokenType.BANG)) {
            return new UnaryExpression(unary(), UnaryOperator.NEGATE, 0, 0);
        } else if (matches(TokenType.DASH)) {
            return new UnaryExpression(unary(), UnaryOperator.INVERSE, 0, 0);
        } else {
            return indexedValue();
        }
    }

    private Expression indexedValue() throws ParseException {
        Expression expression = value();
        while (matches(TokenType.OPEN_BRACKET)) {
            Expression index = expression();
            expression = new IndexExpression(expression, index, 0, 0);
            consume(TokenType.CLOSE_BRACKET, null);
        }
        return expression;
    }

    private Expression value() throws ParseException {
        if (peek().type() == TokenType.OPEN_PAREN) {
            return groupingExpression();
        } else if (peek().type() == TokenType.IDENTIFIER) {
            return identifierExpression();
        } else if (peek().type() == TokenType.OPEN_BRACKET) {
            return vectorExpression();
        } else {
            return literalExpression();
        }
    }

    private GroupingExpression groupingExpression() throws ParseException {
        consume(TokenType.OPEN_PAREN, null);
        Expression expression = expression();
        consume(TokenType.CLOSE_PAREN, null);
        return new GroupingExpression(expression, 0, 0);
    }

    private IdentifierExpression identifierExpression() throws ParseException {
        consume(TokenType.IDENTIFIER, null);
        String name = previous().value();
        return new IdentifierExpression(name, 0, 0);
    }

    private LiteralExpression literalExpression() throws ParseException {
        if (matches(TokenType.TRUE)) {
            return new LiteralExpression(true, 0, 0);
        } else if (matches(TokenType.FALSE)) {
            return new LiteralExpression(false, 0, 0);
        } else if (matches(TokenType.INT_LITERAL)) {
            return new LiteralExpression(Integer.parseInt(previous().value()), 0, 0);
        } else if (matches(TokenType.FLOAT_LITERAL)) {
            return new LiteralExpression(Double.parseDouble(previous().value()), 0, 0);
        } else {
            throw new ParseException(null, 0);
        }
    }

    private VectorExpression vectorExpression() throws ParseException {
        List<Expression> expressions = new ArrayList<>();
        consume(TokenType.OPEN_BRACKET, null);
        do {
            expressions.add(expression());
        } while (matches(TokenType.COMMA));
        consume(TokenType.CLOSE_BRACKET, null);
        return new VectorExpression(expressions.toArray(new Expression[0]), 0, 0);
    }

    private Type type() throws ParseException {
        if (matches(TokenType.COLON)) {
            consume(TokenType.IDENTIFIER, null);
            BaseType baseType = switch(previous().value()) {
                case "int" -> BaseType.INT;
                case "float" -> BaseType.FLOAT;
                case "bool" -> BaseType.BOOL;
                case "char" -> BaseType.CHAR;
                default -> null;
            };
            if (baseType == null) {
                throw new ParseException(null, 0);
            }
            List<Integer> list = new ArrayList<>();
            while (matches(TokenType.OPEN_BRACKET)) {
                consume(TokenType.INT_LITERAL, null);
                list.add(Integer.parseInt(previous().value()));
                consume(TokenType.CLOSE_BRACKET, null);
            }
            int[] shape = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                shape[i] = list.get(i);
            }
            return new Type(new Shape(baseType, shape), false);
        } else {
            return null;
        }
    }

    private void consume(TokenType expected, String message) throws ParseException {
        if (next().type() != expected) {
            throw new ParseException(message, peek().position());
        }
    }

    private void expect(TokenType expected, String message) throws ParseException {
        if (previous().type() != expected) {
            throw new ParseException(message, previous().position());
        }
    }

    private boolean matches(TokenType type) throws ParseException {
        if (peek().type() == type) {
            next();
            return true;
        } else {
            return false;
        }
    }

    private boolean matches(TokenType[] types) throws ParseException {
        for (TokenType type : types) {
            if (matches(type)) {
                return true;
            }
        }
        return false;
    }

    private Token next() throws ParseException {
        if (!hasNext()) {
            throw new ParseException("unexpected end of file", 0);
        }
        return tokens.get(position++);
    }

    private Token peek() {
        if (!hasNext()) {
            return new Token(TokenType.EOF, null, 0, 0);
        }
        return tokens.get(position);
    }

    private Token previous() {
        return tokens.get(position - 1);
    }

    private boolean hasNext() {
        return position < tokens.size();
    }
}
