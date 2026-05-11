package ast;

public class ReturnStatement extends Statement {
    public Expression expression; // may be null

    public ReturnStatement(Expression expression) {
        this.expression = expression;
    }
}
