public class LetStmt {
    String varName;
    Expr rhs;
    Type type;

    public LetStmt(String varName, Expr rhs, Type type) {
        this.varName = varName;
        this.rhs = rhs;
        this.type = type;
    }
}