package ast;

public class Identifier extends Expression {
    public String name;

    public Identifier(String name) {
        this.name = name;
    }
}