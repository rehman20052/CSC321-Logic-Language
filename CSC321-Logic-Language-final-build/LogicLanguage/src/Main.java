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

    // -----------------------------------------------------------------------
    // Printer — indents automatically after functions, loops, and conditionals
    // -----------------------------------------------------------------------

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

        // --- if statement ---
        else if (stmt instanceof IfStatement i) {
            System.out.println(space + "IfStatement");
            System.out.println(space + "  Condition:");
            printExpression(i.condition, indent + 2);
            System.out.println(space + "  Then:");
            for (Statement s : i.thenBranch) {
                printStatement(s, indent + 2);
            }
            if (i.elseBranch != null) {
                System.out.println(space + "  Else:");
                for (Statement s : i.elseBranch) {
                    printStatement(s, indent + 2);
                }
            }
        }

        // --- while loop ---
        else if (stmt instanceof WhileStatement w) {
            System.out.println(space + "WhileStatement");
            System.out.println(space + "  Condition:");
            printExpression(w.condition, indent + 2);
            System.out.println(space + "  Body:");
            for (Statement s : w.body) {
                printStatement(s, indent + 2);
            }
        }

        // --- for loop ---
        else if (stmt instanceof ForStatement f) {
            System.out.println(space + "ForStatement");
            System.out.println(space + "  Init:");
            printStatement(f.init, indent + 2);
            System.out.println(space + "  Condition:");
            printExpression(f.condition, indent + 2);
            System.out.println(space + "  Update:");
            printStatement(f.update, indent + 2);
            System.out.println(space + "  Body:");
            for (Statement s : f.body) {
                printStatement(s, indent + 2);
            }
        }

        // --- function declaration ---
        else if (stmt instanceof FunctionDeclaration fd) {
            System.out.println(space + "FunctionDeclaration(" + fd.name + ")");
            if (!fd.params.isEmpty()) {
                System.out.println(space + "  Params: " + String.join(", ", fd.params));
            }
            System.out.println(space + "  Body:");
            for (Statement s : fd.body) {
                printStatement(s, indent + 2);
            }
        }

        // --- return statement ---
        else if (stmt instanceof ReturnStatement r) {
            System.out.println(space + "ReturnStatement");
            if (r.expression != null) {
                printExpression(r.expression, indent + 1);
            }
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

        else if (expr instanceof FunctionCall fc) {
            System.out.println(space + "FunctionCall(" + fc.name + ")");
            for (Expression arg : fc.args) {
                printExpression(arg, indent + 1);
            }
        }
    }
}
