package ast;

public class IntegerLiteral extends Expression {
    public int value;

    public IntegerLiteral(int value) {
        this.value = value;
    }
}