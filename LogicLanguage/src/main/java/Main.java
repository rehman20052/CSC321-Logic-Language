import parser.Parser;
import lexer.*;
import ast.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String source = "x = 5; print x;";

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        Program program = parser.parseProgram();

        System.out.println("Parsing successful!");
    }
}