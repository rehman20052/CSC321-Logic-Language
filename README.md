# LogicLanguage — CSC321 Final Project

LogicLanguage is a custom interpreted programming language built in Java for CSC321.
The project demonstrates the complete programming language pipeline including:

* Lexical Analysis
* Parsing
* Abstract Syntax Tree (AST) construction
* Runtime interpretation
* Error handling
* GUI integration

The language evolved from the original Phase 1 lexer/parser implementation into a fully working language environment with execution support and an IDE-style GUI.

---

# How to Run

Open terminal inside the project folder and run:

```bash
java -cp out Main gui
```

This launches the LogicLanguage GUI.

---

# Features

## Compiler / Interpreter Pipeline

```text
Source Code
   ↓
Lexer
   ↓
Tokens
   ↓
Parser
   ↓
AST
   ↓
Interpreter
   ↓
Executed Output
```

---

# Supported Language Features

* Integer values
* String values
* Boolean values
* Variables and assignments
* Arithmetic expressions
* Print statements
* If / Else conditions
* While loops
* Functions
* Runtime interpretation
* Error handling
* GUI-based IDE environment

---

# GUI Features

* Dark IDE-style interface
* Source code editor
* Output console
* Run functionality
* Token visualization
* AST visualization
* Runtime execution

---

# Language Grammar (Core)

```ebnf
program     → statement* EOF
statement   → assignment
            | print_stmt
            | if_stmt
            | while_stmt
            | function_stmt

assignment  → IDENTIFIER '=' expression ';'
print_stmt  → 'print' expression ';'

expression  → term (('+' | '-') term)*
term        → factor (('*' | '/') factor)*

factor      → INTEGER
            | STRING
            | BOOLEAN
            | IDENTIFIER
            | '(' expression ')'
```

---

# Architecture

```text
LogicLanguage/
│
├── README.md
│
├── src/
│   ├── Main.java
│   │
│   ├── lexer/
│   │   ├── TokenType.java
│   │   ├── Token.java
│   │   └── Lexer.java
│   │
│   ├── parser/
│   │   └── Parser.java
│   │
│   ├── ast/
│   │   ├── Node.java
│   │   ├── Program.java
│   │   ├── Statement.java
│   │   ├── Expression.java
│   │   ├── AssignmentStatement.java
│   │   ├── PrintStatement.java
│   │   ├── BinaryExpression.java
│   │   ├── Identifier.java
│   │   └── IntegerLiteral.java
│   │
│   └── runtime/
│       ├── Interpreter.java
│       └── Environment.java
│
├── tests/
│
└── out/
```

---

# Example Program

```text
x = 5;
y = x + 7;

print y;

if (y > 10) {
    print "greater than 10";
}

counter = 0;

while (counter < 3) {
    print counter;
    counter = counter + 1;
}
```

---

# Example Output

```text
12
greater than 10
0
1
2
```

---

# Team Responsibilities

* Person 1 — Lexer Development
* Person 2 — Parser Development
* Person 3 — AST / GUI / Integration

---

# Technologies Used

* Java
* Java Swing
* Recursive Descent Parsing
* AST-based Interpretation
* Object-Oriented Design

---

# Notes

This project was developed for CSC321 to demonstrate the internal implementation of a programming language from tokenization to runtime execution.
