package ast;

public class BinaryExpression extends Expression {
    public Expression left;
    public String operator;
    public Expression right;

    public BinaryExpression(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}