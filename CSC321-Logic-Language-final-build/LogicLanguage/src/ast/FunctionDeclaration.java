package ast;

import java.util.List;

public class FunctionDeclaration extends Statement {
    public String name;
    public List<String> params;
    public List<Statement> body;

    public FunctionDeclaration(String name, List<String> params, List<Statement> body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }
}
