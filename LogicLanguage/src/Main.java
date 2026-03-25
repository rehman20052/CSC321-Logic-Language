import parser.Parser;
import lexer.*;
import ast.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("Usage:");
            System.out.println("  java Main lex <file>");
            System.out.println("  java Main parse <file>");
            return;
        }

        String command = args[0];
        String fileName = args[1];

        String source = Files.readString(Path.of(fileName));

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        if (command.equals("lex")) {
            for (Token token : tokens) {
                System.out.println(token.getType() + "(" + token.getLexeme() + ")");
            }
            return;
        }

        if (command.equals("parse")) {
            Parser parser = new Parser(tokens);
            Program program = parser.parseProgram();

            printProgram(program);
            return;
        }

        System.out.println("Unknown command: " + command);
    }

    private static void printProgram(Program program) {
        System.out.println("Program");

        for (Statement stmt : program.statements) {
            printStatement(stmt, 1);
        }
    }

    private static void printStatement(Statement stmt, int indent) {
        String space = "  ".repeat(indent);

        if (stmt instanceof AssignmentStatement a) {
            System.out.println(space + "AssignmentStatement");
            printExpression(a.identifier, indent + 1);
            printExpression(a.expression, indent + 1);
        }

        else if (stmt instanceof PrintStatement p) {
            System.out.println(space + "PrintStatement");
            printExpression(p.expression, indent + 1);
        }
    }

    private static void printExpression(Expression expr, int indent) {
        String space = "  ".repeat(indent);

        if (expr instanceof BinaryExpression b) {
            System.out.println(space + "BinaryExpression(" + b.operator + ")");
            printExpression(b.left, indent + 1);
            printExpression(b.right, indent + 1);
        }

        else if (expr instanceof IntegerLiteral i) {
            System.out.println(space + "IntegerLiteral(" + i.value + ")");
        }

        else if (expr instanceof Identifier id) {
            System.out.println(space + "Identifier(" + id.name + ")");
        }
    }
}