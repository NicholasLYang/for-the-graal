public class PrimaryExpr extends Expr {
    Object value;

    public PrimaryExpr(Object value, Type type) {
        super(type);

        this.value = value;
    }
}