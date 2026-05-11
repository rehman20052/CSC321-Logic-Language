package ast;

import java.util.List;

public class WhileStatement extends Statement {
    public Expression condition;
    public List<Statement> body;

    public WhileStatement(Expression condition, List<Statement> body) {
        this.condition = condition;
        this.body = body;
    }
}
