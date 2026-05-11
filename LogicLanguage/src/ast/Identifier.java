package ast;

public class Identifier extends Expression {
    public String name;

    public Identifier(String name, SourceSpan span) {
        this.name = name;
        this.span = span;
    }
}