package ast;

public class PrintStatement extends Statement {
    public Expression expression;

    public PrintStatement(Expression expression) {
        this.expression = expression;
    }
}