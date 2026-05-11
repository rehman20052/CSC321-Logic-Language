package lexer;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int start;
    private final int end;
    private final int line;

    public Token(TokenType type, String lexeme, int start, int end, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.start = start;
        this.end = end;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return type + "(" + lexeme + ") at line " + line;
    }
}