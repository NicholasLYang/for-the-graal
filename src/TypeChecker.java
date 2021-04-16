import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.HashMap;

enum Type {
    NUMBER, STRING, BOOL
}

public class TypeChecker {
    ArrayList<String> opcodes;
    HashMap<String, Type> symbolTable;

    public TypeChecker() {
        opcodes = new ArrayList<>();
        symbolTable = new HashMap<>();
    }

    public ArrayList<String> checkProgram(Value program) throws Exception {
        if (!program.hasArrayElements()) {
            throw new Exception("Program must be array");
        }
        var len = program.getArraySize();
        var stmtOpCodes = new ArrayList<ArrayList<String>>();

        // We add each stmt's opcodes as a separate ArrayList...
        for (var i = 0; i < len; i++) {
            stmtOpCodes.add(checkStmt(program.getArrayElement(i)));
        }

        // ...so that we can reverse the stmt order for the interpreter
        var opcodes = new ArrayList<String>();
        for (int j = (int)len - 1; j >= 0; j--) {
            opcodes.addAll(stmtOpCodes.get(j));
        }

        return opcodes;
    }

    public ArrayList<String> checkStmt(Value stmt) throws Exception {
        if (!stmt.hasArrayElements()) {
            throw new Exception("Program cannot have top level values");
        }
        if (stmt.getArraySize() < 2) {
            throw new Exception("Statements must have at least an operator and an argument");
        }

        var operator = stmt.getArrayElement(0);
        if (!operator.isString()) {
            throw new Exception("Operator must be a symbol");
        }
        var opString = operator.asString();

        switch (opString) {
            case "let": {
                if (stmt.getArraySize() != 3) {
                    throw new Exception("Let bindings must take two arguments: name and value");
                }
                var varName = stmt.getArrayElement(1);
                if (!varName.isString()) {
                    throw new Exception("Variable names must be strings");
                }

                opcodes.add("let");
                opcodes.add(varName.asString());
                opcodes.add("ident");
                Type type = checkExpr(stmt.getArrayElement(2));
                symbolTable.put(varName.asString(), type);

                break;
            }
            case "print": {
                if (stmt.getArraySize() != 2) {
                    throw new Exception("Print statements must take exactly one argument");
                }
                opcodes.add("print");
                checkExpr(stmt.getArrayElement(1));
                break;
            }
            default:
				throw new Exception("Operator " + opString + " not defined");
        }
        var opcodes = this.opcodes;
        this.opcodes = new ArrayList<>();
        return opcodes;
    }

    public Type checkExpr(Value expr) throws Exception {
		if (!expr.hasArrayElements()) {
		    if (expr.fitsInDouble()) {
		        opcodes.add(String.valueOf(expr.asDouble()));
                opcodes.add("number");
                return Type.NUMBER;
            }

		    if (expr.isString()) {
                var varName = expr.asString();
		        Type varType = symbolTable.get(varName);
		        if (varType == null) {
		            throw new Exception("Variable `" + varName + "' not defined");
                }
		        opcodes.add(varName);
		        opcodes.add("var");
		        return varType;
            }
        }
        var operator = expr.getArrayElement(0);

        if (!operator.isString()) {
            throw new Exception("Operator must be string");
        }

        String opString = operator.asString();

        if (opString.equals("quote")) {
            var str = expr.getArrayElement(1);
            opcodes.add(str.asString());
            opcodes.add("string");
            return Type.STRING;
        }

        if (expr.getArraySize() != 3) {
            throw new Exception("Only binary operations are supported");
        }
        if (!(opString.equals("+") || opString.equals("-") || opString.equals("*") || opString.equals("/"))) {
                throw new Exception("Invalid operator: " + opString);
        }

        opcodes.add(opString);
        var lhsType = checkExpr(expr.getArrayElement(1));
        var rhsType = checkExpr(expr.getArrayElement(2));

        if (!(lhsType == Type.NUMBER && rhsType == Type.NUMBER)) {
            throw new Exception("Invalid operator " + opString + " for types " + lhsType + " and " + rhsType);
        }


        return Type.NUMBER;
    }
}
