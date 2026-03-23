LogicLanguage — CSC321 Phase 1

A minimal language implementation featuring a lexer and parser built in Java.
This project covers Phase 1: tokenization and AST construction.


How to Run:
### Compile
```bash
javac -d out src/lexer/*.java src/ast/*.java src/parser/*.java src/Main.java
```

### Lex a file
```bash
java -cp out Main lex <file>
```
Example:
```bash
java -cp out Main lex tests/valid/test1.ml
```
Output:
```
IDENTIFIER(x)
EQUAL(=)
INTEGER(5)
SEMICOLON(;)
PRINT(print)
IDENTIFIER(x)
SEMICOLON(;)
EOF
```

### Parse a file
```bash
java -cp out Main parse <file>
```
Example:
```bash
java -cp out Main parse tests/valid/test1.ml
```
Output:
```
Program
  AssignmentStatement
    Identifier(x)
    IntegerLiteral(5)
  PrintStatement
    Identifier(x)
```

---

## Language Grammar (EBNF)

```
program     → statement* EOF
statement   → assignment | print_stmt
assignment  → IDENTIFIER '=' expression ';'
print_stmt  → 'print' expression ';'
expression  → term (('+' | '-') term)*
term        → factor (('*' | '/') factor)*
factor      → INTEGER | IDENTIFIER | '(' expression ')'
```

---

## Architecture

```
src/
├── Main.java                  — CLI entry point (lex / parse commands)
├── lexer/
│   ├── TokenType.java         — Enum of all token types
│   ├── Token.java             — Token class (type + lexeme)
│   └── Lexer.java             — Converts source code into token stream
├── ast/
│   ├── Node.java              — Base class for all AST nodes
│   ├── Statement.java         — Abstract statement node
│   ├── Expression.java        — Abstract expression node
│   ├── Program.java           — Root node (list of statements)
│   ├── AssignmentStatement.java
│   ├── PrintStatement.java
│   ├── BinaryExpression.java
│   ├── Identifier.java
│   └── IntegerLiteral.java
└── parser/
    └── Parser.java            — Builds AST from token stream
```

**Pipeline:**
```
Source Code → Lexer → Tokens → Parser → AST → Printed Output
```

---

## Supported Language Features (Phase 1)

- Integer literals (e.g. `5`, `42`, `1000`)
- Identifiers (e.g. `x`, `total1`, `value`)
- Arithmetic expressions: `+`, `-`, `*`, `/`
- Correct operator precedence (`*` and `/` before `+` and `-`)
- Parentheses to override precedence
- Assignment statements: `x = 5;`
- Print statements: `print x;`
- A program is a sequence of statements

**Not included in Phase 1:** evaluation, variable storage, type checking, functions, loops, error recovery.

---

## Test Cases

### Valid Programs

**test1.ml** — Basic assignment and print
```
x = 5;
print x;
```

**test2.ml** — Arithmetic with precedence
```
x = 3 + 4 * 5;
print x;
```

**test3.ml** — Parentheses override precedence
```
x = (3 + 4) * 5;
print x;
```

**test4.ml** — Multiple assignments
```
x = 1;
y = x + 2;
print y;
```

**test5.ml** — Print an expression directly
```
print 3 + 4;
```

**test6.ml** — Division
```
x = 10 / 2;
print x;
```

**test7.ml** — Subtraction
```
x = 10 - 3;
print x;
```

**test8.ml** — Nested parentheses
```
x = (2 + (3 * 4));
print x;
```

**test9.ml** — Multiple prints
```
x = 5;
y = 10;
print x;
print y;
```

**test10.ml** — Complex expression
```
x = 2 + 3 * 4 - 1;
print x;
```

### Invalid Programs

**invalid1.ml** — Missing semicolon
```
x = 5
print x;
```

**invalid2.ml** — Missing value in assignment
```
x = ;
```

**invalid3.ml** — Unknown character
```
x = 5 @ 2;
```

**invalid4.ml** — Missing closing parenthesis
```
x = (3 + 4;
print x;
```

**invalid5.ml** — Incomplete expression
```
print ;
```

---

## Team
- Person 1 — Lexer (`TokenType`, `Token`, `Lexer`)
- Person 2 — Parser (`Parser`)
- Person 3 — AST classes, `Main.java`, tests, README
