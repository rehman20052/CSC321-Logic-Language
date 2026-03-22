package lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String source;
    private int pos;

    public Lexer(String source) {
        this.source = source;
        this.pos = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < source.length()) {
            char c = source.charAt(pos);

            // Skip whitespace
            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            // Integer literals
            if (Character.isDigit(c)) {
                tokens.add(readInteger());
                continue;
            }

            // Identifiers and 'print' keyword
            if (Character.isLetter(c)) {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }

            // Operators and punctuation
            switch (c) {
                case '+': tokens.add(new Token(TokenType.PLUS,      "+")); pos++; break;
                case '-': tokens.add(new Token(TokenType.MINUS,     "-")); pos++; break;
                case '*': tokens.add(new Token(TokenType.STAR,      "*")); pos++; break;
                case '/': tokens.add(new Token(TokenType.SLASH,     "/")); pos++; break;
                case '=': tokens.add(new Token(TokenType.EQUAL,     "=")); pos++; break;
                case ';': tokens.add(new Token(TokenType.SEMICOLON, ";")); pos++; break;
                case '(': tokens.add(new Token(TokenType.LPAREN,    "(")); pos++; break;
                case ')': tokens.add(new Token(TokenType.RPAREN,    ")")); pos++; break;
                default:
                    System.err.println("Unknown character '" + c + "' at position " + pos);
                    System.exit(1);
            }
        }

        tokens.add(new Token(TokenType.EOF, "EOF"));
        return tokens;
    }

    private Token readInteger() {
        int start = pos;
        while (pos < source.length() && Character.isDigit(source.charAt(pos))) {
            pos++;
        }
        return new Token(TokenType.INTEGER, source.substring(start, pos));
    }

    private Token readIdentifierOrKeyword() {
        int start = pos;
        while (pos < source.length() && Character.isLetterOrDigit(source.charAt(pos))) {
            pos++;
        }
        String word = source.substring(start, pos);
        if (word.equals("print")) {
            return new Token(TokenType.PRINT, word);
        }
        return new Token(TokenType.IDENTIFIER, word);
    }
}