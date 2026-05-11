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

        System.out.println(
            peek().getLexeme() + " -> line " + peek().getLine()
        );
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
        Token identifierToken =
                consume(TokenType.IDENTIFIER, "Expected identifier.");

        consume(TokenType.EQUAL,
                "Expected '=' after identifier.");

        Expression expression = parseExpression();

        consume(TokenType.SEMICOLON,
                "Expected ';' after assignment statement.");

        Identifier id = new Identifier(
                identifierToken.getLexeme(),
                new SourceSpan(
                        identifierToken.getStart(),
                        identifierToken.getEnd()
                )
        );

        AssignmentStatement stmt =
                new AssignmentStatement(id, expression);

        stmt.span = new SourceSpan(
                identifierToken.getStart(),
                expression.span.end
        );

        return stmt;
    }

    private Statement parsePrintStatement() {
        Token printToken =
                consume(TokenType.PRINT, "Expected 'print'.");

        Expression expression = parseExpression();

        consume(TokenType.SEMICOLON,
                "Expected ';' after print statement.");

        PrintStatement stmt = new PrintStatement(expression);

        stmt.span = new SourceSpan(
                printToken.getStart(),
                expression.span.end
        );

        return stmt;
    }

    private Expression parseExpression() {
        Expression expr = parseTerm();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();

            Expression right = parseTerm();

            BinaryExpression bin =
                    new BinaryExpression(
                            expr,
                            operator.getLexeme(),
                            right
                    );

            bin.span = new SourceSpan(
                    expr.span.start,
                    right.span.end
            );

            expr = bin;
        }

        return expr;
    }

    private Expression parseTerm() {
        Expression expr = parseFactor();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();

            Expression right = parseFactor();

            BinaryExpression bin =
                    new BinaryExpression(
                            expr,
                            operator.getLexeme(),
                            right
                    );

            bin.span = new SourceSpan(
                    expr.span.start,
                    right.span.end
            );

            expr = bin;
        }

        return expr;
    }

    private Expression parseFactor() {

        if (match(TokenType.INTEGER)) {
            Token t = previous();

            IntegerLiteral lit =
                    new IntegerLiteral(
                            Integer.parseInt(t.getLexeme())
                    );

            lit.span = new SourceSpan(
                    t.getStart(),
                    t.getEnd()
            );

            return lit;
        }

        if (match(TokenType.IDENTIFIER)) {
            Token t = previous();

            return new Identifier(
                    t.getLexeme(),
                    new SourceSpan(
                            t.getStart(),
                            t.getEnd()
                    )
            );
        }

        if (match(TokenType.LPAREN)) {
            Expression expr = parseExpression();

            consume(TokenType.RPAREN,
                    "Expected ')' after expression.");

            return expr;
        }

        throw error("Expected integer, identifier, or '(' expression ')'."
        );
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
        if (check(type)) return advance();
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
        Token token = peek();

        String found = isAtEnd()
            ? "EOF"
            : token.getLexeme();
        int line = isAtEnd() ? token.getLine() : token.getLine();

        return new RuntimeException(
            "Parse error at line "
                + line
                + " near '"
                + found
                + "': "
                + message
        );
    }  
}