package parser;

import ast.*;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Program parseProgram() {
        List<Statement> statements = new ArrayList<>();

        while (!check(TokenType.EOF)) {
            statements.add(parseStatement());
        }

        consume(TokenType.EOF, "Expected end of file.");
        return new Program(statements);
    }

    private Statement parseStatement() {
        if (check(TokenType.IDENTIFIER)) {
            return parseAssignment();
        }

        if (check(TokenType.PRINT)) {
            return parsePrintStatement();
        }

        throw error("Expected statement.");
    }

    private Statement parseAssignment() {
        Token identifierToken = consume(TokenType.IDENTIFIER, "Expected identifier.");
        consume(TokenType.EQUAL, "Expected '=' after identifier.");

        Expression expression = parseExpression();

        consume(TokenType.SEMICOLON, "Expected ';' after assignment statement.");

        return new AssignmentStatement(
                new Identifier(identifierToken.getLexeme()),
                expression
        );
    }

    private Statement parsePrintStatement() {
        consume(TokenType.PRINT, "Expected 'print'.");

        Expression expression = parseExpression();

        consume(TokenType.SEMICOLON, "Expected ';' after print statement.");

        return new PrintStatement(expression);
    }

    private Expression parseExpression() {
        Expression expr = parseTerm();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = parseTerm();
            expr = new BinaryExpression(expr, operator.getLexeme(), right);
        }

        return expr;
    }

    private Expression parseTerm() {
        Expression expr = parseFactor();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expression right = parseFactor();
            expr = new BinaryExpression(expr, operator.getLexeme(), right);
        }

        return expr;
    }

    private Expression parseFactor() {
        if (match(TokenType.INTEGER)) {
            return new IntegerLiteral(Integer.parseInt(previous().getLexeme()));
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Identifier(previous().getLexeme());
        }

        if (match(TokenType.LPAREN)) {
            Expression expr = parseExpression();
            consume(TokenType.RPAREN, "Expected ')' after expression.");
            return expr;
        }

        throw error("Expected integer, identifier, or '(' expression ')'.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return type == TokenType.EOF;
        }
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private RuntimeException error(String message) {
        String found = isAtEnd() ? "EOF" : peek().getLexeme();
        return new RuntimeException("Parse error near '" + found + "': " + message);
    }
}