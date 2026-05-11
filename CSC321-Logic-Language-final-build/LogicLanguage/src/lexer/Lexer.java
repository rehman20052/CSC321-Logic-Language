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

            // Skip single-line comments
            if (c == '/' && pos + 1 < source.length() && source.charAt(pos + 1) == '/') {
                while (pos < source.length() && source.charAt(pos) != '\n') {
                    pos++;
                }
                continue;
            }

            // Integer literals
            if (Character.isDigit(c)) {
                tokens.add(readInteger());
                continue;
            }

            // Identifiers and keywords
            if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }

            // Two-character operators
            if (c == '=' && pos + 1 < source.length() && source.charAt(pos + 1) == '=') {
                tokens.add(new Token(TokenType.EQUAL_EQUAL, "==")); pos += 2; continue;
            }
            if (c == '!' && pos + 1 < source.length() && source.charAt(pos + 1) == '=') {
                tokens.add(new Token(TokenType.NOT_EQUAL, "!=")); pos += 2; continue;
            }
            if (c == '<' && pos + 1 < source.length() && source.charAt(pos + 1) == '=') {
                tokens.add(new Token(TokenType.LESS_EQUAL, "<=")); pos += 2; continue;
            }
            if (c == '>' && pos + 1 < source.length() && source.charAt(pos + 1) == '=') {
                tokens.add(new Token(TokenType.GREATER_EQUAL, ">=")); pos += 2; continue;
            }

            // Single-character operators and punctuation
            switch (c) {
                case '+': tokens.add(new Token(TokenType.PLUS,      "+")); pos++; break;
                case '-': tokens.add(new Token(TokenType.MINUS,     "-")); pos++; break;
                case '*': tokens.add(new Token(TokenType.STAR,      "*")); pos++; break;
                case '/': tokens.add(new Token(TokenType.SLASH,     "/")); pos++; break;
                case '=': tokens.add(new Token(TokenType.EQUAL,     "=")); pos++; break;
                case '<': tokens.add(new Token(TokenType.LESS,      "<")); pos++; break;
                case '>': tokens.add(new Token(TokenType.GREATER,   ">")); pos++; break;
                case ';': tokens.add(new Token(TokenType.SEMICOLON, ";")); pos++; break;
                case '(': tokens.add(new Token(TokenType.LPAREN,    "(")); pos++; break;
                case ')': tokens.add(new Token(TokenType.RPAREN,    ")")); pos++; break;
                case '{': tokens.add(new Token(TokenType.LBRACE,    "{")); pos++; break;
                case '}': tokens.add(new Token(TokenType.RBRACE,    "}")); pos++; break;
                case ',': tokens.add(new Token(TokenType.COMMA,     ",")); pos++; break;
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
        while (pos < source.length() && (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
            pos++;
        }
        String word = source.substring(start, pos);
        switch (word) {
            case "print":  return new Token(TokenType.PRINT,  word);
            case "if":     return new Token(TokenType.IF,     word);
            case "else":   return new Token(TokenType.ELSE,   word);
            case "while":  return new Token(TokenType.WHILE,  word);
            case "for":    return new Token(TokenType.FOR,    word);
            case "func":   return new Token(TokenType.FUNC,   word);
            case "return": return new Token(TokenType.RETURN, word);
            default:       return new Token(TokenType.IDENTIFIER, word);
        }
    }
}
