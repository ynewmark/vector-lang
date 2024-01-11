package org.vectorlang.compiler.parser;

import java.util.ArrayList;
import java.util.List;

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
import org.vectorlang.compiler.ast.UnaryExpression;
import org.vectorlang.compiler.ast.UnaryOperator;
import org.vectorlang.compiler.ast.VectorExpression;
import org.vectorlang.compiler.ast.WhileStatement;
import org.vectorlang.compiler.compiler.BaseType;
import org.vectorlang.compiler.compiler.Type;

public class Parser {

    private ParseRule<Expression> literalExpression, identifierExpression, value, unary, indexedValue,
        factor, term, comparison, equality, and, or, groupingExpression, vectorExpression, expression;
    
    private ParseRule<Statement> forStatement, whileStatement, assignStatement, assignStatement2, statement;

    private ParseRule<FunctionStatement> function;

    public Parser() {
        literalExpression = new ParseRule<>(this::literalExpression, TokenType.SEMICOLON);
        identifierExpression = new ParseRule<>(this::identifierExpression, TokenType.SEMICOLON);
        value = new ParseRule<>(this::value, TokenType.SEMICOLON);
        unary = new ParseRule<>(this::unary, TokenType.SEMICOLON);
        indexedValue = new ParseRule<>(this::indexedValue, TokenType.SEMICOLON);
        factor = new ParseRule<>(this::factor, TokenType.SEMICOLON);
        term = new ParseRule<>(this::term, TokenType.SEMICOLON);
        comparison = new ParseRule<>(this::comparison, TokenType.SEMICOLON);
        equality = new ParseRule<>(this::equality, TokenType.SEMICOLON);
        and = new ParseRule<>(this::and, TokenType.SEMICOLON);
        or = new ParseRule<>(this::or, TokenType.SEMICOLON);
        groupingExpression = new ParseRule<>(this::groupingExpression, TokenType.SEMICOLON);
        vectorExpression = new ParseRule<>(this::vectorExpression, TokenType.SEMICOLON);
        expression = or;
        forStatement = new ParseRule<>(this::forStatement, TokenType.CLOSE_BRACE);
        whileStatement = new ParseRule<>(this::whileStatement, TokenType.CLOSE_BRACE);
        assignStatement = new ParseRule<>((ParserState state) -> assignStatement(state, true), TokenType.SEMICOLON);
        assignStatement2 = new ParseRule<>((ParserState state) -> assignStatement(state, false), TokenType.SEMICOLON);
        statement = new ParseRule<>(this::statement, TokenType.CLOSE_BRACE);
        function = new ParseRule<>(this::function, TokenType.CLOSE_BRACE);
    }

    private LiteralExpression literalExpression(ParserState state) {
        if (state.matches(TokenType.TRUE)) {
            return new LiteralExpression(true);
        } else if (state.matches(TokenType.FALSE)) {
            return new LiteralExpression(false);
        } else if (state.matches(TokenType.INT_LITERAL)) {
            return new LiteralExpression(Integer.parseInt(state.previous().value()));
        } else if (state.matches(TokenType.FLOAT_LITERAL)) {
            return new LiteralExpression(Double.parseDouble(state.previous().value()));
        } else {
            return null;
        }
    }

    private Expression identifierExpression(ParserState state) {
        state.consume(TokenType.IDENTIFIER);
        String name = state.previous().value();
        if (state.matches(TokenType.OPEN_PAREN)) {
            boolean flag = false;
            List<Expression> expressions = new ArrayList<>();
            while (!state.matches(TokenType.CLOSE_PAREN)) {
                if (flag) {
                    state.consume(TokenType.COMMA);
                }
                expressions.add(expression.apply(state));
                flag = true;
            }
            return new CallExpression(name, expressions.toArray(new Expression[0]), null);
        }
        return new IdentifierExpression(name);
    }

    private Expression value(ParserState state) {
        if (state.peek().type() == TokenType.OPEN_PAREN) {
            return groupingExpression.apply(state);
        } else if (state.peek().type() == TokenType.IDENTIFIER) {
            return identifierExpression.apply(state);
        } else if (state.peek().type() == TokenType.OPEN_BRACKET) {
            return vectorExpression.apply(state);
        } else {
            return literalExpression.apply(state);
        }
    }

    private Expression indexedValue(ParserState state) {
        Expression expr = value.apply(state);
        while (state.matches(TokenType.OPEN_BRACKET)) {
            Expression index = expression.apply(state);
            expr = new IndexExpression(expr, index);
            state.consume(TokenType.CLOSE_BRACKET);
        }
        return expr;
    }

    private Expression unary(ParserState state) {
        if (state.matches(TokenType.BANG)) {
            return new UnaryExpression(unary.apply(state), UnaryOperator.NEGATE);
        } else if (state.matches(TokenType.DASH)) {
            return new UnaryExpression(unary.apply(state), UnaryOperator.INVERSE);
        } else {
            return indexedValue.apply(state);
        }
    }

    private Expression factor(ParserState state) {
        Expression expression = unary.apply(state);
        while (state.matches(new TokenType[]{TokenType.STAR, TokenType.SLASH, TokenType.DOT_STAR, TokenType.DOT_SLASH})) {
            BinaryOperator operator = switch (state.previous().type()) {
                case STAR -> BinaryOperator.MULTIPLY;
                case SLASH -> BinaryOperator.DIVIDE;
                case DOT_STAR -> BinaryOperator.DOT_MULTIPLY;
                case DOT_SLASH -> BinaryOperator.DOT_DIVIDE;
                case DOT_DOT -> BinaryOperator.CONCAT;
                default -> null;
            };
            expression = new BinaryExpression(expression, unary.apply(state), operator);
        }
        return expression;
    }

    private Expression term(ParserState state) {
        Expression expression = factor.apply(state);
        while (state.matches(new TokenType[]{TokenType.PLUS, TokenType.DASH, TokenType.DOT_PLUS, TokenType.DOT_DASH})) {
            BinaryOperator operator = switch (state.previous().type()) {
                case PLUS -> BinaryOperator.ADD;
                case DASH -> BinaryOperator.SUBTRACT;
                case DOT_PLUS -> BinaryOperator.DOT_ADD;
                case DOT_DASH -> BinaryOperator.DOT_SUBTRACT;
                default -> null;
            };
            expression = new BinaryExpression(expression, factor.apply(state), operator);
        }
        return expression;
    }

    private Expression comparison(ParserState state) {
        Expression expression = term.apply(state);
        while (state.matches(new TokenType[]{TokenType.LEFT_ARROW, TokenType.LEFT_ARROW_EQUALS, TokenType.RIGHT_ARROW, TokenType.RIGHT_ARROW_EQUALS})) {
            BinaryOperator operator = switch (state.previous().type()) {
                case LEFT_ARROW -> BinaryOperator.LESS_THAN;
                case RIGHT_ARROW -> BinaryOperator.GREATER_THAN;
                case LEFT_ARROW_EQUALS -> BinaryOperator.EQUAL_LESS_THAN;
                case RIGHT_ARROW_EQUALS -> BinaryOperator.EQUAL_GREATER_THAN;
                default -> null;
            };
            expression = new BinaryExpression(expression, term.apply(state), operator);
        }
        return expression;
    }

    private Expression or(ParserState state) {
        Expression expression = and.apply(state);
        while (state.matches(TokenType.BAR)) {
            expression = new BinaryExpression(expression, and.apply(state), BinaryOperator.OR);
        }
        return expression;
    }

    private Expression and(ParserState state) {
        Expression expression = equality.apply(state);
        while (state.matches(TokenType.AMPERSAND)) {
            expression = new BinaryExpression(expression, equality.apply(state), BinaryOperator.AND);
        }
        return expression;
    }

    private Expression equality(ParserState state) {
        Expression expression = comparison.apply(state);
        while (state.matches(new TokenType[]{TokenType.EQUALS_EQUALS, TokenType.BANG_EQUALS})) {
            BinaryOperator operator = switch (state.previous().type()) {
                case EQUALS_EQUALS -> BinaryOperator.EQUAL;
                case BANG_EQUALS -> BinaryOperator.NOT_EQUAL;
                default -> null;
            };
            expression = new BinaryExpression(expression, comparison.apply(state), operator);
        }
        return expression;
    }

    private GroupingExpression groupingExpression(ParserState state) {
        state.consume(TokenType.OPEN_PAREN);
        Expression expr = expression.apply(state);
        state.consume(TokenType.CLOSE_PAREN);
        return new GroupingExpression(expr);
    }

    private VectorExpression vectorExpression(ParserState state) {
        List<Expression> expressions = new ArrayList<>();
        state.consume(TokenType.OPEN_BRACKET);
        do {
            expressions.add(expression.apply(state));
        } while (state.matches(TokenType.COMMA));
        state.consume(TokenType.CLOSE_BRACKET);
        return new VectorExpression(expressions.toArray(new Expression[0]));
    }

    private ForStatement forStatement(ParserState state) {
        state.consume(TokenType.FOR);
        state.consume(TokenType.OPEN_PAREN);
        Statement initial = statement.apply(state);
        Expression condition = expression.apply(state);
        state.consume(TokenType.SEMICOLON);
        Statement each = assignStatement2.apply(state);
        state.consume(TokenType.CLOSE_PAREN);
        Statement body = statement.apply(state);
        return new ForStatement(condition, initial, each, body);
    }

    private WhileStatement whileStatement(ParserState state) {
        state.consume(TokenType.WHILE);
        state.consume(TokenType.OPEN_PAREN);
        Expression condition = expression.apply(state);
        state.consume(TokenType.CLOSE_PAREN);
        Statement body = statement.apply(state);
        return new WhileStatement(condition, body);
    }

    private AssignStatement assignStatement(ParserState state, boolean semicolon) {
        state.consume(TokenType.IDENTIFIER);
        String name = state.previous().value();
        Expression expr = null;
        if (state.matches(TokenType.EQUALS)) {
            expr = expression.apply(state);
        } else if (state.matches(TokenType.PLUS_PLUS)) {
            expr = new BinaryExpression(
                new IdentifierExpression(name),
                new LiteralExpression(1), BinaryOperator.ADD
            );
        } else if (state.matches(TokenType.MINUS_MINUS)) {
            expr = new BinaryExpression(
                new IdentifierExpression(name),
                new LiteralExpression(1), BinaryOperator.SUBTRACT
            );
        } else {
            BinaryOperator operator = null;
            if (state.matches(TokenType.PLUS_EQUALs)) {
                operator = BinaryOperator.ADD;
            } else if (state.matches(TokenType.MINUS_EQUALS)) {
                operator = BinaryOperator.SUBTRACT;
            } else if (state.matches(TokenType.STAR_EQUALS)) {
                operator = BinaryOperator.MULTIPLY;
            } else if (state.matches(TokenType.SLASH_EQUALS)) {
                operator = BinaryOperator.DIVIDE;
            } else if (state.matches(TokenType.BAR_EQUALS)) {
                operator = BinaryOperator.OR;
            } else if (state.matches(TokenType.AMPERSAND_EQUALS)) {
                operator = BinaryOperator.AND;
            } else {
                return null;
            }
            expr = new BinaryExpression(
                new IdentifierExpression(name),
                expression.apply(state), operator);
        }
        if (semicolon) {
            state.consume(TokenType.SEMICOLON);
        }
        return new AssignStatement(name, expr);
    }

    private Statement statement(ParserState state) {
        if (state.matches(TokenType.OPEN_BRACE)) {
            List<Statement> statements = new ArrayList<>();
            while (!state.matches(TokenType.CLOSE_BRACE)) {
                statements.add(statement.apply(state));
            }
            return new BlockStatement(statements.toArray(new Statement[0]));
        } else if (state.matches(TokenType.PRINT)) {
            Expression expr = expression.apply(state);
            state.consume(TokenType.SEMICOLON);
            return new PrintStatement(expr);
        } else if (state.matches(new TokenType[]{TokenType.LET, TokenType.CONST})) {
            boolean constant = state.previous().type() == TokenType.CONST;
            state.consume(TokenType.IDENTIFIER);
            String name = state.previous().value();
            Expression expr = null;
            if (state.matches(TokenType.EQUALS)) {
                expr = expression.apply(state);
            }
            Type type = type(state);
            state.consume(TokenType.SEMICOLON);
            return new DeclareStatement(constant, name, expr, type);
        } else if (state.matches(TokenType.IF)) {
            state.consume(TokenType.OPEN_PAREN);
            Expression condition = expression.apply(state);
            state.consume(TokenType.CLOSE_PAREN);
            Statement ifStatement = statement.apply(state);
            Statement elseStatement = null;
            if (state.matches(TokenType.ELSE)) {
                elseStatement = statement.apply(state);
            }
            return new IfStatement(ifStatement, elseStatement, condition);
        } else if (state.peek().type() == TokenType.IDENTIFIER) {
            return assignStatement.apply(state);
        } else if (state.peek().type() == TokenType.WHILE) {
            return whileStatement.apply(state);
        } else if (state.peek().type() == TokenType.FOR) {
            return forStatement.apply(state);
        } else if (state.matches(TokenType.RETURN)) {
            Expression expr = expression.apply(state);
            state.consume(TokenType.SEMICOLON);
            return new ReturnStatement(expr);
        } else {
            return null;
        }
    }

    private FunctionStatement function(ParserState state) {
        List<String> names = new ArrayList<>();
        List<Type> types = new ArrayList<>();
        List<Statement> statements = new ArrayList<>();
        state.consume(TokenType.FUNC);
        state.consume(TokenType.IDENTIFIER);
        String name = state.previous().value();
        state.consume(TokenType.OPEN_PAREN);
        boolean flag = false;
        while (!state.matches(TokenType.CLOSE_PAREN)) {
            if (flag) {
                state.consume(TokenType.COMMA);
            }
            state.consume(TokenType.IDENTIFIER);
            names.add(state.previous().value());
            types.add(type(state));
            flag = true;
        }
        Type type = null;
        if (state.peek().type() == TokenType.COLON) {
            type = type(state);
        }
        state.consume(TokenType.OPEN_BRACE);
        while (!state.matches(TokenType.CLOSE_BRACE)) {
            statements.add(statement.apply(state));
        }
        return new FunctionStatement(
            name, names.toArray(new String[0]), types.toArray(new Type[0]), statements.toArray(new Statement[0]), type
        );
    }

    private Type type(ParserState state) {
        if (state.matches(TokenType.COLON)) {
            state.consume(TokenType.IDENTIFIER);
            BaseType baseType = switch(state.previous().value()) {
                case "int" -> BaseType.INT;
                case "float" -> BaseType.FLOAT;
                case "bool" -> BaseType.BOOL;
                case "char" -> BaseType.CHAR;
                default -> null;
            };
            if (baseType == null) {
                return null;
            }
            List<Integer> list = new ArrayList<>();
            while (state.matches(TokenType.OPEN_BRACKET)) {
                state.consume(TokenType.INT_LITERAL);
                list.add(Integer.parseInt(state.previous().value()));
                state.consume(TokenType.CLOSE_BRACKET);
            }
            int[] shape = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                shape[i] = list.get(i);
            }
            return new Type(baseType, shape, false);
        } else {
            return null;
        }
    }

    public CodeBase parse(List<Token> tokens) {
        List<FunctionStatement> functions = new ArrayList<>();
        ParserState state = new ParserState(tokens);
        while (state.hasNext()) {
            functions.add(function.apply(state));
        }
        return new CodeBase(functions.toArray(new FunctionStatement[0]));
    }
}
