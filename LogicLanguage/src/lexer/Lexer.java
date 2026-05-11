package lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String source;
    private int pos;
    private int line;

    public Lexer(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < source.length()) {
            char c = source.charAt(pos);

            if (c == '\n') {
                line++;
                pos++;
                continue;
            }

            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            if (Character.isDigit(c)) {
                tokens.add(readInteger());
                continue;
            }

            if (Character.isLetter(c)) {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }

            int start = pos;
            int tokenLine = line; // IMPORTANT: snapshot line here

            switch (c) {

                case '+':
                    tokens.add(new Token(TokenType.PLUS, "+", start, start + 1, tokenLine));
                    pos++;
                    break;

                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-", start, start + 1, tokenLine));
                    pos++;
                    break;

                case '*':
                    tokens.add(new Token(TokenType.STAR, "*", start, start + 1, tokenLine));
                    pos++;
                    break;

                case '/':
                    tokens.add(new Token(TokenType.SLASH, "/", start, start + 1, tokenLine));
                    pos++;
                    break;

                case '=':
                    tokens.add(new Token(TokenType.EQUAL, "=", start, start + 1, tokenLine));
                    pos++;
                    break;

                case ';':
                    tokens.add(new Token(TokenType.SEMICOLON, ";", start, start + 1, tokenLine));
                    pos++;
                    break;

                case '(':
                    tokens.add(new Token(TokenType.LPAREN, "(", start, start + 1, tokenLine));
                    pos++;
                    break;

                case ')':
                    tokens.add(new Token(TokenType.RPAREN, ")", start, start + 1, tokenLine));
                    pos++;
                    break;

                default:
                    throw new RuntimeException(
                            "Lexer error at line " + tokenLine +
                            ": Unknown character '" + c + "'"
                    );
            }
        }

        tokens.add(new Token(TokenType.EOF, "EOF", pos, pos, line));
        return tokens;
    }

    private Token readInteger() {
        int start = pos;
        int tokenLine = line;

        while (pos < source.length() && Character.isDigit(source.charAt(pos))) {
            pos++;
        }

        return new Token(
                TokenType.INTEGER,
                source.substring(start, pos),
                start,
                pos,
                tokenLine
        );
    }

    private Token readIdentifierOrKeyword() {
        int start = pos;
        int tokenLine = line;

        while (pos < source.length() &&
                Character.isLetterOrDigit(source.charAt(pos))) {
            pos++;
        }

        String word = source.substring(start, pos);

        if (word.equals("print")) {
            return new Token(TokenType.PRINT, word, start, pos, tokenLine);
        }

        return new Token(
                TokenType.IDENTIFIER,
                word,
                start,
                pos,
                tokenLine
        );
    }
}