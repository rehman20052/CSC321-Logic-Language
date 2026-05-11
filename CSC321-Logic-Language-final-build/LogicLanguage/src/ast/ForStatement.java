package ast;

import java.util.List;

public class ForStatement extends Statement {
    public Statement init;       // e.g. i = 0;
    public Expression condition; // e.g. i < 10
    public Statement update;     // e.g. i = i + 1;  (no trailing semicolon in source)
    public List<Statement> body;

    public ForStatement(Statement init, Expression condition, Statement update, List<Statement> body) {
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }
}
