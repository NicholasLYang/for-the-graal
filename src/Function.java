import java.util.ArrayList;

public class Function {
    ArrayList<String> body;
    ArrayList<Param> params;
    Type returnType;

    public Function(ArrayList<String> body, ArrayList<Param> params, Type returnType) {
        this.body = body;
        this.params = params;
        this.returnType = returnType;
    }
}