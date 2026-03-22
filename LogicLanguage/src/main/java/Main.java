import parser.Parser;
import lexer.*;
import ast.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        List<Token> tokens = List.of(
                new Token(TokenType.IDENTIFIER, "x"),
                new Token(TokenType.EQUAL, "="),
                new Token(TokenType.INTEGER, "5"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.PRINT, "print"),
                new Token(TokenType.IDENTIFIER, "x"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.EOF, "")
        );

        Parser parser = new Parser(tokens);
        Program program = parser.parseProgram();

        System.out.println("Parsing successful!");
    }
}