# LogicLanguage Final Build

LogicLanguage is a small interpreted teaching language built for CSC321. It demonstrates a complete working language pipeline:

Source Code → Lexer → Tokens → Parser → AST → Interpreter/Runtime → Output

It also includes a simple Swing GUI IDE.

## Features

- Integer, string, and boolean values
- Variables and assignment
- Arithmetic: `+`, `-`, `*`, `/`
- Comparisons: `>`, `>=`, `<`, `<=`, `==`, `!=`
- Boolean logic: `and`, `or`, `not`
- Print statements
- If/else statements
- While loops
- Functions with parameters and return values
- Basic class declarations
- Basic module/import declarations
- Runtime errors, such as undefined variables and divide by zero
- CLI mode and GUI mode

## Example Program

```logic
module demo;

function add(a, b) {
  return a + b;
}

x = 5;
y = add(x, 7);
print "Result:";
print y;

if (y > 10) {
  print "y is greater than 10";
}

counter = 0;
while (counter < 3) {
  print counter;
  counter = counter + 1;
}
```

## How to Compile

From the project folder:

```bash
javac -d out src/Main.java
```

## How to Run

### Lex tokens
```bash
java -cp out Main lex tests/demo.logic
```

### Show AST
```bash
java -cp out Main parse tests/demo.logic
```

### Execute program
```bash
java -cp out Main run tests/demo.logic
```

### Launch GUI IDE
```bash
java -cp out Main gui
```

## Expected Runtime Output

```text
Result:
12
y is greater than 10
0
1
2
```

## Architecture

- `Lexer` converts source code into tokens.
- `Parser` converts tokens into an AST.
- AST classes represent statements and expressions.
- `Interpreter` executes the AST.
- `Environment` stores variables and function scopes.
- `LogicIDE` provides a simple GUI editor and output console.

## Notes

This is intentionally a small language, not a full replacement for Java or Python. Its purpose is to demonstrate the major pieces of a real programming language in a working, presentable build.
