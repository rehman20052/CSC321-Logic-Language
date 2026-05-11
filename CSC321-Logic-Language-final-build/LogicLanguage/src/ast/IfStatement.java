package ast;

import java.util.List;

public class IfStatement extends Statement {
    public Expression condition;
    public List<Statement> thenBranch;
    public List<Statement> elseBranch; // may be null

    public IfStatement(Expression condition, List<Statement> thenBranch, List<Statement> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
}
