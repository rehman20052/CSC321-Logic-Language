package lexer;

public enum TokenType {
    INTEGER,
    IDENTIFIER,
    // Keywords
    PRINT,
    IF,
    ELSE,
    WHILE,
    FOR,
    FUNC,
    RETURN,
    // Arithmetic operators
    PLUS,
    MINUS,
    STAR,
    SLASH,
    // Comparison operators
    EQUAL,
    EQUAL_EQUAL,
    NOT_EQUAL,
    LESS,
    LESS_EQUAL,
    GREATER,
    GREATER_EQUAL,
    // Punctuation
    SEMICOLON,
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    COMMA,
    EOF
}
