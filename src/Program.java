import java.util.ArrayList;
import java.util.HashMap;

public class Program {
    ArrayList<String> opcodes;
    HashMap<String, Function> functions;

    public Program(ArrayList<String> opcodes, HashMap<String, Function> functions) {
        this.opcodes = opcodes;
        this.functions = functions;
    }

    public String createOpCodesList(ArrayList<String> opcodes) {
        var codeBuilder = new StringBuilder();
        codeBuilder.append("[");
        for (String opcode : opcodes) {
            codeBuilder.append("\"");
            codeBuilder.append(opcode);
            codeBuilder.append("\",");
        }
        codeBuilder.append("]");

        return codeBuilder.toString();
    }

    public String createJavaScriptCode() {
        var codeBuilder = new StringBuilder();
        codeBuilder.append("const functions = new Map();\n");
        functions.forEach((name, fun) -> {
            codeBuilder.append("functions.set(\"");
            codeBuilder.append(name);
            codeBuilder.append("\", { body: ");
            codeBuilder.append(this.createOpCodesList(fun.body));
            codeBuilder.append(", params: [");
            for (Param p : fun.params) {
                codeBuilder.append("\"");
                codeBuilder.append(p.name);
                codeBuilder.append("\",");
            }
            codeBuilder.append("]});\n");
        });
        codeBuilder.append("const opcodes = ");
        codeBuilder.append(this.createOpCodesList(this.opcodes));
        codeBuilder.append(";\n");
        codeBuilder.append("evalProgram(opcodes, functions)");
        return codeBuilder.toString();
    }
}