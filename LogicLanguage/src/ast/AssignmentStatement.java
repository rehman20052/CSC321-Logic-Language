package ast;

public class AssignmentStatement extends Statement {
    public Identifier identifier;
    public Expression expression;

    public AssignmentStatement(Identifier identifier, Expression expression) {
        this.identifier = identifier;
        this.expression = expression;
    }
}