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

    // -----------------------------------------------------------------------
    // Statements
    // -----------------------------------------------------------------------

    private Statement parseStatement() {
        if (check(TokenType.IF))     return parseIfStatement();
        if (check(TokenType.WHILE))  return parseWhileStatement();
        if (check(TokenType.FOR))    return parseForStatement();
        if (check(TokenType.FUNC))   return parseFunctionDeclaration();
        if (check(TokenType.RETURN)) return parseReturnStatement();
        if (check(TokenType.PRINT))  return parsePrintStatement();
        if (check(TokenType.IDENTIFIER)) return parseAssignment();

        throw error("Expected statement.");
    }

    /** if ( condition ) { body } [ else { body } ] */
    private Statement parseIfStatement() {
        consume(TokenType.IF, "Expected 'if'.");
        consume(TokenType.LPAREN, "Expected '(' after 'if'.");
        Expression condition = parseExpression();
        consume(TokenType.RPAREN, "Expected ')' after condition.");

        List<Statement> thenBranch = parseBlock();

        List<Statement> elseBranch = null;
        if (check(TokenType.ELSE)) {
            advance();
            elseBranch = parseBlock();
        }

        return new IfStatement(condition, thenBranch, elseBranch);
    }

    /** while ( condition ) { body } */
    private Statement parseWhileStatement() {
        consume(TokenType.WHILE, "Expected 'while'.");
        consume(TokenType.LPAREN, "Expected '(' after 'while'.");
        Expression condition = parseExpression();
        consume(TokenType.RPAREN, "Expected ')' after condition.");

        List<Statement> body = parseBlock();
        return new WhileStatement(condition, body);
    }

    /**
     * for ( init ; condition ; update ) { body }
     * init and update are assignments without their own semicolons in the
     * for-header; the semicolons separating clauses serve that role.
     */
    private Statement parseForStatement() {
        consume(TokenType.FOR, "Expected 'for'.");
        consume(TokenType.LPAREN, "Expected '(' after 'for'.");

        // init clause — a full assignment statement (consumes its own semicolon)
        Statement init = parseAssignment();

        // condition clause
        Expression condition = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after for-condition.");

        // update clause — assignment without trailing semicolon
        Statement update = parseAssignmentNoSemicolon();

        consume(TokenType.RPAREN, "Expected ')' after for-update.");

        List<Statement> body = parseBlock();
        return new ForStatement(init, condition, update, body);
    }

    /** func name ( params ) { body } */
    private Statement parseFunctionDeclaration() {
        consume(TokenType.FUNC, "Expected 'func'.");
        Token nameToken = consume(TokenType.IDENTIFIER, "Expected function name.");
        consume(TokenType.LPAREN, "Expected '(' after function name.");

        List<String> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            params.add(consume(TokenType.IDENTIFIER, "Expected parameter name.").getLexeme());
            while (match(TokenType.COMMA)) {
                params.add(consume(TokenType.IDENTIFIER, "Expected parameter name.").getLexeme());
            }
        }
        consume(TokenType.RPAREN, "Expected ')' after parameters.");

        List<Statement> body = parseBlock();
        return new FunctionDeclaration(nameToken.getLexeme(), params, body);
    }

    /** return [ expression ] ; */
    private Statement parseReturnStatement() {
        consume(TokenType.RETURN, "Expected 'return'.");
        Expression expr = null;
        if (!check(TokenType.SEMICOLON)) {
            expr = parseExpression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after return.");
        return new ReturnStatement(expr);
    }

    private Statement parsePrintStatement() {
        consume(TokenType.PRINT, "Expected 'print'.");
        Expression expression = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after print statement.");
        return new PrintStatement(expression);
    }

    /** identifier = expression ; */
    private Statement parseAssignment() {
        Token identifierToken = consume(TokenType.IDENTIFIER, "Expected identifier.");
        consume(TokenType.EQUAL, "Expected '=' after identifier.");
        Expression expression = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after assignment statement.");
        return new AssignmentStatement(new Identifier(identifierToken.getLexeme()), expression);
    }

    /** identifier = expression   (no semicolon — used inside for-update) */
    private Statement parseAssignmentNoSemicolon() {
        Token identifierToken = consume(TokenType.IDENTIFIER, "Expected identifier.");
        consume(TokenType.EQUAL, "Expected '=' after identifier.");
        Expression expression = parseExpression();
        return new AssignmentStatement(new Identifier(identifierToken.getLexeme()), expression);
    }

    /** { statement* } */
    private List<Statement> parseBlock() {
        consume(TokenType.LBRACE, "Expected '{'.");
        List<Statement> stmts = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            stmts.add(parseStatement());
        }
        consume(TokenType.RBRACE, "Expected '}'.");
        return stmts;
    }

    // -----------------------------------------------------------------------
    // Expressions
    // -----------------------------------------------------------------------

    private Expression parseExpression() {
        return parseComparison();
    }

    /** comparison handles ==, !=, <, <=, >, >= */
    private Expression parseComparison() {
        Expression expr = parseAddSub();

        while (match(TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL,
                     TokenType.LESS, TokenType.LESS_EQUAL,
                     TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            Token operator = previous();
            Expression right = parseAddSub();
            expr = new BinaryExpression(expr, operator.getLexeme(), right);
        }

        return expr;
    }

    private Expression parseAddSub() {
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

        // Identifier or function call
        if (match(TokenType.IDENTIFIER)) {
            String name = previous().getLexeme();
            if (match(TokenType.LPAREN)) {
                // Function call
                List<Expression> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    args.add(parseExpression());
                    while (match(TokenType.COMMA)) {
                        args.add(parseExpression());
                    }
                }
                consume(TokenType.RPAREN, "Expected ')' after arguments.");
                return new FunctionCall(name, args);
            }
            return new Identifier(name);
        }

        if (match(TokenType.LPAREN)) {
            Expression expr = parseExpression();
            consume(TokenType.RPAREN, "Expected ')' after expression.");
            return expr;
        }

        throw error("Expected integer, identifier, or '(' expression ')'.");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

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
        if (isAtEnd()) return type == TokenType.EOF;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
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
