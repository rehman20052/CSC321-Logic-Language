import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import javax.swing.text.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0];

        if (command.equals("gui")) {
            SwingUtilities.invokeLater(LogicIDE::new);
            return;
        }

        if (args.length < 2) {
            printUsage();
            return;
        }

        String source = Files.readString(Path.of(args[1]));
        runCommand(command, source);
    }

    static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java Main lex <file>");
        System.out.println("  java Main parse <file>");
        System.out.println("  java Main run <file>");
        System.out.println("  java Main gui");
    }

    static void runCommand(String command, String source) {
        try {
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();

            if (command.equals("lex")) {
                for (Token token : tokens) System.out.println(token);
                return;
            }

            Parser parser = new Parser(tokens);
            Program program = parser.parseProgram();

            if (command.equals("parse")) {
                System.out.println(program.toTree(""));
                return;
            }

            if (command.equals("run")) {
                Interpreter interpreter = new Interpreter();
                interpreter.execute(program);
                return;
            }

            System.out.println("Unknown command: " + command);
        } catch (LogicError e) {
            System.out.println("Logic error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

class LogicIDE extends JFrame {
    private final JTextPane codeArea = new JTextPane();
    private final JTextArea outputArea = new JTextArea();
    private final JLabel statusLabel = new JLabel(" Ready");

    private boolean isHighlighting = false;
    private boolean isUpdatingDocument = false; // IMPORTANT FIX

    private final StyleContext sc = new StyleContext();
    private final DefaultStyledDocument doc = new DefaultStyledDocument(sc);

    private final Style keywordStyle = sc.addStyle("Keyword", null);
    private final Style numberStyle = sc.addStyle("Number", null);
    private final Style stringStyle = sc.addStyle("String", null);
    private final Style defaultStyle = sc.addStyle("Default", null);

    private static final Color BG = new Color(18, 22, 31);
    private static final Color PANEL = new Color(28, 34, 47);
    private static final Color EDITOR = new Color(12, 16, 24);
    private static final Color OUTPUT = new Color(9, 13, 20);
    private static final Color TEXT = new Color(230, 236, 245);
    private static final Color MUTED = new Color(145, 156, 175);
    private static final Color ACCENT = new Color(86, 156, 214);
    private static final Color GREEN = new Color(87, 196, 129);
    private static final Color PURPLE = new Color(187, 134, 252);

    LogicIDE() {
        codeArea.setDocument(doc);

        codeArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {
                    e.consume();
                    int caret = codeArea.getCaretPosition();
                    try {
                        doc.insertString(caret, "  ", null);
                    } catch (Exception ignored) {}
                }
            }
        });

        codeArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                if (isUpdatingDocument) return;
                SwingUtilities.invokeLater(() -> applySyntaxHighlighting());
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        setTitle("Logic Language Studio");
        setSize(1180, 760);
        setMinimumSize(new Dimension(960, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildMainContent(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        loadDefaultProgram();
        setVisible(true);
    }

    // ================= HEADER =================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));

        JLabel title = new JLabel("Logic Language Studio");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Mini IDE for CSC321 final build");
        subtitle.setForeground(MUTED);

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(subtitle);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        JButton run = makeButton("Run", GREEN);
        JButton parse = makeButton("AST", PURPLE);
        JButton lex = makeButton("Tokens", ACCENT);
        JButton sample = makeButton("Sample", new Color(64, 72, 92));
        JButton clear = makeButton("Clear", new Color(64, 72, 92));

        run.addActionListener(e -> executeMode("run"));
        parse.addActionListener(e -> executeMode("parse"));
        lex.addActionListener(e -> executeMode("lex"));
        sample.addActionListener(e -> loadDefaultProgram());
        clear.addActionListener(e -> outputArea.setText(""));

        buttons.add(sample);
        buttons.add(lex);
        buttons.add(parse);
        buttons.add(run);
        buttons.add(clear);

        header.add(left, BorderLayout.WEST);
        header.add(buttons, BorderLayout.EAST);
        return header;
    }

    // ================= MAIN CONTENT =================
    private JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.setBorder(BorderFactory.createEmptyBorder(10, 22, 18, 22));

        JPanel editorPanel = makePanel("Source Code");
        styleEditor();
        JScrollPane editorScroll = new JScrollPane(codeArea);
        editorScroll.setRowHeaderView(createLineNumbers());
        editorPanel.add(editorScroll, BorderLayout.CENTER);

        JPanel outputPanel = makePanel("Output");
        styleOutput();
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPanel, outputPanel);
        split.setResizeWeight(0.6);

        main.add(split, BorderLayout.CENTER);
        return main;
    }

    private JTextArea createLineNumbers() {
        JTextArea lineNumbers = new JTextArea("1\n");

        lineNumbers.setBackground(new Color(20, 24, 32));
        lineNumbers.setForeground(MUTED);
        lineNumbers.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        lineNumbers.setEditable(false);
        lineNumbers.setFocusable(false);
        lineNumbers.setMargin(new Insets(3, 6, 0, 6));

        codeArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateLines() {
                SwingUtilities.invokeLater(() -> {
                    int totalLines = codeArea.getText().split("\\n", -1).length;
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i <= totalLines; i++) {
                        sb.append(i).append("\n");
                    }
                    lineNumbers.setText(sb.toString());
                });
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateLines();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateLines();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateLines();
            }
        });
        return lineNumbers;
    }

    // ================= STATUS =================
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PANEL);
        bar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        statusLabel.setForeground(MUTED);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // ================= SYNTAX HIGHLIGHT =================
    private void applySyntaxHighlighting() {
        if (isHighlighting) return;

        isHighlighting = true;
        isUpdatingDocument = true;

        int caret = codeArea.getCaretPosition();

        try {
            String text = codeArea.getText();

            try {
                doc.remove(0, doc.getLength());
            } catch (BadLocationException e) {
                return; // or just ignore safely
            }

            StyleConstants.setForeground(defaultStyle, TEXT);
            StyleConstants.setForeground(keywordStyle, ACCENT);
            StyleConstants.setForeground(numberStyle, GREEN);
            StyleConstants.setForeground(stringStyle, PURPLE);

            StringBuilder token = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);

                if (Character.isLetterOrDigit(c)) {
                    token.append(c);
                } else {
                    flush(token.toString());
                    token.setLength(0);
                    append(String.valueOf(c), defaultStyle);
                }
            }

            flush(token.toString());

            SwingUtilities.invokeLater(() -> {
                codeArea.setCaretPosition(Math.min(caret, doc.getLength()));
            });

        } finally {
            isHighlighting = false;
            isUpdatingDocument = false;
        }
    }

    private void flush(String token) {
        if (token.isEmpty()) return;

        if (isKeyword(token)) append(token, keywordStyle);
        else if (token.matches("\\d+")) append(token, numberStyle);
        else append(token, defaultStyle);
    }

    private boolean isKeyword(String t) {
        return t.equals("print") || t.equals("if") || t.equals("else")
                || t.equals("while") || t.equals("class")
                || t.equals("function") || t.equals("return");
    }

    private void append(String text, Style style) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (Exception ignored) {}
    }

    // ================= EXECUTION =================
    private void executeMode(String mode) {
        StringBuilder sb = new StringBuilder();
        try {
            Lexer lexer = new Lexer(codeArea.getText());
            List<Token> tokens = lexer.tokenize();
            if (mode.equals("lex")) {
                for (Token token : tokens) {
                sb.append(token).append("\n");
            }
            } else {
            Parser parser = new Parser(tokens);
            Program program = parser.parseProgram();
            if (mode.equals("parse")) {
                sb.append(program.toTree(""));
            } else if (mode.equals("run")) {
                Interpreter interpreter = new Interpreter();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream oldOut = System.out;
                System.setOut(new PrintStream(baos));
                try {
                    interpreter.execute(program);
                } finally {
                    System.setOut(oldOut);
                }
                String out = baos.toString();
                sb.append(out.isBlank() ? "(no output)" : out);
                }
            }
            outputArea.setText(sb.toString());
            statusLabel.setText(" Completed: " + mode + " mode");
        } catch (Exception ex) {
            outputArea.setText("Error: " + ex.getMessage());
            statusLabel.setText(" Error in " + mode + " mode");
        }
    }

    // ================= SAMPLE =================
    private void loadDefaultProgram() {
        codeArea.setText(
        "// This is a simple demo program\n" + 
        "module demo;\n\n" + 
        "function add(a, b) {\n" + 
        " return a + b;\n" + 
        "}\n\n" + "x = 5;\n" + 
        "y = add(x, 7);\n" + 
        "print \"Result:\";\n" + 
        "print y;\n\n" + 
        "if (y > 10) {\n" + 
        " print \"y is greater than 10\";\n" + 
        "} else {\n" + 
        " print \"y is 10 or less\";\n" + 
        "}\n\n" + 
        "counter = 0;\n" + 
        "while (counter < 3) {\n" +
        " print counter;\n" + 
        " counter = counter + 1;\n" + 
        "}\n\n" + 
        "class Student {\n" + 
        " name = \"Demo\";\n" + "}\n"
        );
        outputArea.setText("Click Run, AST, or Tokens to test the language.\n"); statusLabel.setText(" Ready — sample program loaded");
    }

    // ================= HELPERS =================
    private JPanel makePanel(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PANEL);
        javax.swing.border.TitledBorder border =
            BorderFactory.createTitledBorder(title);
        border.setTitleColor(Color.white);
        p.setBorder(border);
        return p;
    }

    private JButton makeButton(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        return b;
    }

    private void styleEditor() {
        codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        codeArea.setBackground(EDITOR);
        codeArea.setForeground(TEXT);
    }

    private void styleOutput() {
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        outputArea.setBackground(OUTPUT);
        outputArea.setForeground(TEXT);
        outputArea.setEditable(false);
    }
}

enum TokenType {
    INTEGER, STRING, BOOLEAN,
    IDENTIFIER,
    PRINT, IF, ELSE, WHILE, FUNCTION, RETURN, CLASS, MODULE, IMPORT,
    PLUS, MINUS, STAR, SLASH,
    EQUAL, EQUAL_EQUAL, BANG_EQUAL,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    AND, OR, NOT,
    SEMICOLON, COMMA, DOT,
    LPAREN, RPAREN, LBRACE, RBRACE,
    EOF
}

class Token {
    final TokenType type;
    final String lexeme;

    Token(TokenType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }

    public TokenType getType() { return type; }
    public String getLexeme() { return lexeme; }

    public String toString() {
        if (lexeme == null || lexeme.isEmpty()) return type.name();
        return type + "(" + lexeme + ")";
    }
}

class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int current = 0;

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("print", TokenType.PRINT);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("function", TokenType.FUNCTION);
        keywords.put("return", TokenType.RETURN);
        keywords.put("class", TokenType.CLASS);
        keywords.put("module", TokenType.MODULE);
        keywords.put("import", TokenType.IMPORT);
        keywords.put("true", TokenType.BOOLEAN);
        keywords.put("false", TokenType.BOOLEAN);
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("not", TokenType.NOT);
    }

    Lexer(String source) { this.source = source; }

    List<Token> tokenize() {
        while (!isAtEnd()) scanToken();
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case ' ', '\r', '\t', '\n' -> { }
            case '+' -> add(TokenType.PLUS, "+");
            case '-' -> add(TokenType.MINUS, "-");
            case '*' -> add(TokenType.STAR, "*");
            case '/' -> {
                if (match('/')) {
                    while (!isAtEnd() && peek() != '\n') advance();
                } else add(TokenType.SLASH, "/");
            }
            case '=' -> add(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL, matchWasTwo("=", "=="));
            case '!' -> add(match('=') ? TokenType.BANG_EQUAL : TokenType.NOT, matchWasTwo("!", "!="));
            case '>' -> add(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER, matchWasTwo(">", ">="));
            case '<' -> add(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS, matchWasTwo("<", "<="));
            case ';' -> add(TokenType.SEMICOLON, ";");
            case ',' -> add(TokenType.COMMA, ",");
            case '.' -> add(TokenType.DOT, ".");
            case '(' -> add(TokenType.LPAREN, "(");
            case ')' -> add(TokenType.RPAREN, ")");
            case '{' -> add(TokenType.LBRACE, "{");
            case '}' -> add(TokenType.RBRACE, "}");
            case '"' -> string();
            default -> {
                if (Character.isDigit(c)) number(c);
                else if (Character.isLetter(c) || c == '_') identifier(c);
                else throw new LogicError("Unknown character: " + c);
            }
        }
    }

    private String matchWasTwo(String one, String two) {
        return source.substring(Math.max(0, current - two.length()), current).equals(two) ? two : one;
    }

    private void string() {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && peek() != '"') sb.append(advance());
        if (isAtEnd()) throw new LogicError("Unterminated string literal.");
        advance();
        add(TokenType.STRING, sb.toString());
    }

    private void number(char first) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        while (!isAtEnd() && Character.isDigit(peek())) sb.append(advance());
        add(TokenType.INTEGER, sb.toString());
    }

    private void identifier(char first) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) sb.append(advance());
        String text = sb.toString();
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        add(type, text);
    }

    private void add(TokenType type, String lexeme) { tokens.add(new Token(type, lexeme)); }
    private char advance() { return source.charAt(current++); }
    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }
    private char peek() { return source.charAt(current); }
    private boolean isAtEnd() { return current >= source.length(); }
}

abstract class Node { abstract String toTree(String indent); }
abstract class Statement extends Node {}
abstract class Expression extends Node {}

class Program extends Node {
    final List<Statement> statements;
    Program(List<Statement> statements) { this.statements = statements; }
    String toTree(String indent) {
        StringBuilder sb = new StringBuilder(indent + "Program\n");
        for (Statement s : statements) sb.append(s.toTree(indent + "  "));
        return sb.toString();
    }
}

class BlockStatement extends Statement {
    final List<Statement> statements;
    BlockStatement(List<Statement> statements) { this.statements = statements; }
    String toTree(String indent) {
        StringBuilder sb = new StringBuilder(indent + "Block\n");
        for (Statement s : statements) sb.append(s.toTree(indent + "  "));
        return sb.toString();
    }
}

class AssignmentStatement extends Statement {
    final String name;
    final Expression expression;
    AssignmentStatement(String name, Expression expression) { this.name = name; this.expression = expression; }
    String toTree(String indent) { return indent + "Assignment(" + name + ")\n" + expression.toTree(indent + "  "); }
}

class PrintStatement extends Statement {
    final Expression expression;
    PrintStatement(Expression expression) { this.expression = expression; }
    String toTree(String indent) { return indent + "Print\n" + expression.toTree(indent + "  "); }
}

class IfStatement extends Statement {
    final Expression condition;
    final Statement thenBranch;
    final Statement elseBranch;
    IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) {
        this.condition = condition; this.thenBranch = thenBranch; this.elseBranch = elseBranch;
    }
    String toTree(String indent) {
        String s = indent + "If\n" + condition.toTree(indent + "  ") + thenBranch.toTree(indent + "  ");
        if (elseBranch != null) s += elseBranch.toTree(indent + "  ");
        return s;
    }
}

class WhileStatement extends Statement {
    final Expression condition;
    final Statement body;
    WhileStatement(Expression condition, Statement body) { this.condition = condition; this.body = body; }
    String toTree(String indent) { return indent + "While\n" + condition.toTree(indent + "  ") + body.toTree(indent + "  "); }
}

class FunctionStatement extends Statement {
    final String name;
    final List<String> params;
    final BlockStatement body;
    FunctionStatement(String name, List<String> params, BlockStatement body) { this.name = name; this.params = params; this.body = body; }
    String toTree(String indent) { return indent + "Function(" + name + ", params=" + params + ")\n" + body.toTree(indent + "  "); }
}

class ReturnStatement extends Statement {
    final Expression value;
    ReturnStatement(Expression value) { this.value = value; }
    String toTree(String indent) { return indent + "Return\n" + value.toTree(indent + "  "); }
}

class ClassStatement extends Statement {
    final String name;
    final BlockStatement body;
    ClassStatement(String name, BlockStatement body) { this.name = name; this.body = body; }
    String toTree(String indent) { return indent + "Class(" + name + ")\n" + body.toTree(indent + "  "); }
}

class ModuleStatement extends Statement {
    final String keyword;
    final String name;
    ModuleStatement(String keyword, String name) { this.keyword = keyword; this.name = name; }
    String toTree(String indent) { return indent + keyword + "(" + name + ")\n"; }
}

class BinaryExpression extends Expression {
    final Expression left; final String operator; final Expression right;
    BinaryExpression(Expression left, String operator, Expression right) { this.left = left; this.operator = operator; this.right = right; }
    String toTree(String indent) { return indent + "BinaryExpression(" + operator + ")\n" + left.toTree(indent + "  ") + right.toTree(indent + "  "); }
}

class UnaryExpression extends Expression {
    final String operator; final Expression right;
    UnaryExpression(String operator, Expression right) { this.operator = operator; this.right = right; }
    String toTree(String indent) { return indent + "UnaryExpression(" + operator + ")\n" + right.toTree(indent + "  "); }
}

class LiteralExpression extends Expression {
    final Object value;
    LiteralExpression(Object value) { this.value = value; }
    String toTree(String indent) { return indent + "Literal(" + value + ")\n"; }
}

class IdentifierExpression extends Expression {
    final String name;
    IdentifierExpression(String name) { this.name = name; }
    String toTree(String indent) { return indent + "Identifier(" + name + ")\n"; }
}

class CallExpression extends Expression {
    final String name; final List<Expression> arguments;
    CallExpression(String name, List<Expression> arguments) { this.name = name; this.arguments = arguments; }
    String toTree(String indent) {
        StringBuilder sb = new StringBuilder(indent + "Call(" + name + ")\n");
        for (Expression arg : arguments) sb.append(arg.toTree(indent + "  "));
        return sb.toString();
    }
}

class Parser {
    private final List<Token> tokens;
    private int current = 0;
    Parser(List<Token> tokens) { this.tokens = tokens; }

    Program parseProgram() {
        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.EOF)) statements.add(statement());
        return new Program(statements);
    }

    private Statement statement() {
        if (match(TokenType.MODULE)) return moduleStatement("Module");
        if (match(TokenType.IMPORT)) return moduleStatement("Import");
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.FUNCTION)) return functionStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.CLASS)) return classStatement();
        if (match(TokenType.LBRACE)) return blockStatementAlreadyOpen();
        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.EQUAL)) return assignmentStatement();
        throw error("Expected statement.");
    }

    private Statement moduleStatement(String keyword) {
        Token name = consume(TokenType.IDENTIFIER, "Expected name after " + keyword.toLowerCase() + ".");
        consume(TokenType.SEMICOLON, "Expected ';' after " + keyword.toLowerCase() + " name.");
        return new ModuleStatement(keyword, name.lexeme);
    }

    private Statement printStatement() {
        Expression expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after print statement.");
        return new PrintStatement(expr);
    }

    private Statement assignmentStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Expected identifier.");
        consume(TokenType.EQUAL, "Expected '=' after identifier.");
        Expression expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after assignment.");
        return new AssignmentStatement(name.lexeme, expr);
    }

    private Statement ifStatement() {
        consume(TokenType.LPAREN, "Expected '(' after if.");
        Expression condition = expression();
        consume(TokenType.RPAREN, "Expected ')' after if condition.");
        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(TokenType.ELSE)) elseBranch = statement();
        return new IfStatement(condition, thenBranch, elseBranch);
    }

    private Statement whileStatement() {
        consume(TokenType.LPAREN, "Expected '(' after while.");
        Expression condition = expression();
        consume(TokenType.RPAREN, "Expected ')' after while condition.");
        Statement body = statement();
        return new WhileStatement(condition, body);
    }

    private FunctionStatement functionStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Expected function name.");
        consume(TokenType.LPAREN, "Expected '(' after function name.");
        List<String> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do { params.add(consume(TokenType.IDENTIFIER, "Expected parameter name.").lexeme); }
            while (match(TokenType.COMMA));
        }
        consume(TokenType.RPAREN, "Expected ')' after parameters.");
        consume(TokenType.LBRACE, "Expected '{' before function body.");
        BlockStatement body = blockStatementAlreadyOpen();
        return new FunctionStatement(name.lexeme, params, body);
    }

    private ReturnStatement returnStatement() {
        Expression value = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after return value.");
        return new ReturnStatement(value);
    }

    private ClassStatement classStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Expected class name.");
        consume(TokenType.LBRACE, "Expected '{' before class body.");
        return new ClassStatement(name.lexeme, blockStatementAlreadyOpen());
    }

    private BlockStatement blockStatementAlreadyOpen() {
        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) statements.add(statement());
        consume(TokenType.RBRACE, "Expected '}' after block.");
        return new BlockStatement(statements);
    }

    private Expression expression() { return or(); }
    private Expression or() {
        Expression expr = and();
        while (match(TokenType.OR)) expr = new BinaryExpression(expr, previous().lexeme, and());
        return expr;
    }
    private Expression and() {
        Expression expr = equality();
        while (match(TokenType.AND)) expr = new BinaryExpression(expr, previous().lexeme, equality());
        return expr;
    }
    private Expression equality() {
        Expression expr = comparison();
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) expr = new BinaryExpression(expr, previous().lexeme, comparison());
        return expr;
    }
    private Expression comparison() {
        Expression expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) expr = new BinaryExpression(expr, previous().lexeme, term());
        return expr;
    }
    private Expression term() {
        Expression expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) expr = new BinaryExpression(expr, previous().lexeme, factor());
        return expr;
    }
    private Expression factor() {
        Expression expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH)) expr = new BinaryExpression(expr, previous().lexeme, unary());
        return expr;
    }
    private Expression unary() {
        if (match(TokenType.MINUS, TokenType.NOT)) return new UnaryExpression(previous().lexeme, unary());
        return primary();
    }
    private Expression primary() {
        if (match(TokenType.INTEGER)) return new LiteralExpression(Integer.parseInt(previous().lexeme));
        if (match(TokenType.STRING)) return new LiteralExpression(previous().lexeme);
        if (match(TokenType.BOOLEAN)) return new LiteralExpression(Boolean.parseBoolean(previous().lexeme));
        if (match(TokenType.IDENTIFIER)) {
            Token name = previous();
            if (match(TokenType.LPAREN)) {
                List<Expression> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    do { args.add(expression()); } while (match(TokenType.COMMA));
                }
                consume(TokenType.RPAREN, "Expected ')' after arguments.");
                return new CallExpression(name.lexeme, args);
            }
            return new IdentifierExpression(name.lexeme);
        }
        if (match(TokenType.LPAREN)) {
            Expression expr = expression();
            consume(TokenType.RPAREN, "Expected ')' after expression.");
            return expr;
        }
        throw error("Expected expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) if (check(type)) { advance(); return true; }
        return false;
    }
    private Token consume(TokenType type, String message) { if (check(type)) return advance(); throw error(message); }
    private boolean check(TokenType type) { return peek().type == type; }
    private boolean checkNext(TokenType type) { return current + 1 < tokens.size() && tokens.get(current + 1).type == type; }
    private Token advance() { if (!isAtEnd()) current++; return previous(); }
    private boolean isAtEnd() { return peek().type == TokenType.EOF; }
    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }
    private LogicError error(String message) { return new LogicError("Parse error near '" + peek().lexeme + "': " + message); }
}

class Interpreter {
    private Environment globals = new Environment(null);
    private Environment environment = globals;

    void execute(Program program) {
        for (Statement statement : program.statements) execute(statement);
    }

    private void execute(Statement stmt) {
        if (stmt instanceof ModuleStatement) return;
        if (stmt instanceof ClassStatement c) { environment.define(c.name, "<class " + c.name + ">"); return; }
        if (stmt instanceof AssignmentStatement a) { environment.assignOrDefine(a.name, evaluate(a.expression)); return; }
        if (stmt instanceof PrintStatement p) { System.out.println(stringify(evaluate(p.expression))); return; }
        if (stmt instanceof BlockStatement b) { executeBlock(b.statements, new Environment(environment)); return; }
        if (stmt instanceof IfStatement i) { if (isTruthy(evaluate(i.condition))) execute(i.thenBranch); else if (i.elseBranch != null) execute(i.elseBranch); return; }
        if (stmt instanceof WhileStatement w) { while (isTruthy(evaluate(w.condition))) execute(w.body); return; }
        if (stmt instanceof FunctionStatement f) { environment.define(f.name, new LogicFunction(f)); return; }
        if (stmt instanceof ReturnStatement r) throw new ReturnSignal(evaluate(r.value));
        throw new LogicError("Unknown statement type.");
    }

    void executeBlock(List<Statement> statements, Environment newEnv) {
        Environment previous = environment;
        try {
            environment = newEnv;
            for (Statement statement : statements) execute(statement);
        } finally { environment = previous; }
    }

    private Object evaluate(Expression expr) {
        if (expr instanceof LiteralExpression l) return l.value;
        if (expr instanceof IdentifierExpression id) return environment.get(id.name);
        if (expr instanceof UnaryExpression u) {
            Object right = evaluate(u.right);
            return switch (u.operator) {
                case "-" -> -asInt(right, "Unary '-' requires an integer.");
                case "not", "!" -> !isTruthy(right);
                default -> throw new LogicError("Unknown unary operator: " + u.operator);
            };
        }
        if (expr instanceof BinaryExpression b) {
            Object left = evaluate(b.left);
            Object right = evaluate(b.right);
            return switch (b.operator) {
                case "+" -> plus(left, right);
                case "-" -> asInt(left, "Left side of '-' must be integer.") - asInt(right, "Right side of '-' must be integer.");
                case "*" -> asInt(left, "Left side of '*' must be integer.") * asInt(right, "Right side of '*' must be integer.");
                case "/" -> divide(left, right);
                case ">" -> asInt(left, "Comparison requires integers.") > asInt(right, "Comparison requires integers.");
                case ">=" -> asInt(left, "Comparison requires integers.") >= asInt(right, "Comparison requires integers.");
                case "<" -> asInt(left, "Comparison requires integers.") < asInt(right, "Comparison requires integers.");
                case "<=" -> asInt(left, "Comparison requires integers.") <= asInt(right, "Comparison requires integers.");
                case "==" -> Objects.equals(left, right);
                case "!=" -> !Objects.equals(left, right);
                case "and" -> isTruthy(left) && isTruthy(right);
                case "or" -> isTruthy(left) || isTruthy(right);
                default -> throw new LogicError("Unknown binary operator: " + b.operator);
            };
        }
        if (expr instanceof CallExpression c) {
            Object fn = environment.get(c.name);
            if (!(fn instanceof LogicFunction f)) throw new LogicError("'" + c.name + "' is not a function.");
            List<Object> args = new ArrayList<>();
            for (Expression e : c.arguments) args.add(evaluate(e));
            return f.call(this, args, environment);
        }
        throw new LogicError("Unknown expression type.");
    }

    private Object plus(Object left, Object right) {
        if (left instanceof Integer && right instanceof Integer) return (Integer) left + (Integer) right;
        if (left instanceof String || right instanceof String) return stringify(left) + stringify(right);
        throw new LogicError("'+' requires two integers or at least one string.");
    }
    private Object divide(Object left, Object right) {
        int r = asInt(right, "Right side of '/' must be integer.");
        if (r == 0) throw new LogicError("Math error: cannot divide by zero.");
        return asInt(left, "Left side of '/' must be integer.") / r;
    }
    private int asInt(Object value, String message) { if (value instanceof Integer i) return i; throw new LogicError(message); }
    private boolean isTruthy(Object value) { if (value instanceof Boolean b) return b; return value != null; }
    private String stringify(Object value) { return String.valueOf(value); }
}

class Environment {
    private final Environment parent;
    private final Map<String, Object> values = new HashMap<>();
    Environment(Environment parent) { this.parent = parent; }
    void define(String name, Object value) { values.put(name, value); }
    void assignOrDefine(String name, Object value) {
        if (values.containsKey(name)) values.put(name, value);
        else if (parent != null && parent.contains(name)) parent.assignOrDefine(name, value);
        else values.put(name, value);
    }
    boolean contains(String name) { return values.containsKey(name) || (parent != null && parent.contains(name)); }
    Object get(String name) {
        if (values.containsKey(name)) return values.get(name);
        if (parent != null) return parent.get(name);
        throw new LogicError("Undefined variable: " + name);
    }
}

class LogicFunction {
    private final FunctionStatement declaration;
    LogicFunction(FunctionStatement declaration) { this.declaration = declaration; }
    Object call(Interpreter interpreter, List<Object> args, Environment closure) {
        if (args.size() != declaration.params.size()) throw new LogicError("Function '" + declaration.name + "' expected " + declaration.params.size() + " arguments but got " + args.size() + ".");
        Environment local = new Environment(closure);
        for (int i = 0; i < args.size(); i++) local.define(declaration.params.get(i), args.get(i));
        try {
            interpreter.executeBlock(declaration.body.statements, local);
        } catch (ReturnSignal r) { return r.value; }
        return null;
    }
    public String toString() { return "<function " + declaration.name + ">"; }
}

class ReturnSignal extends RuntimeException {
    final Object value;
    ReturnSignal(Object value) { this.value = value; }
}

class LogicError extends RuntimeException {
    LogicError(String message) { super(message); }
}
