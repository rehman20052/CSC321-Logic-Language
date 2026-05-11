package ast;

import java.util.List;

public class FunctionCall extends Expression {
    public String name;
    public List<Expression> args;

    public FunctionCall(String name, List<Expression> args) {
        this.name = name;
        this.args = args;
    }
}
