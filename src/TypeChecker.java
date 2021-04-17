import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.HashMap;

enum Type {
    NUMBER, STRING, BOOLEAN
}

class Param {
    public String name;
    public Type type;

    public Param(String name, Type type) {
        this.name = name;
        this.type = type;
    }
}

public class TypeChecker {
    ArrayList<String> opcodes;
    HashMap<String, Type> symbolTable;
    HashMap<String, Function> functions;

    public TypeChecker() {
        this.opcodes = new ArrayList<>();
        this.symbolTable = new HashMap<>();
        this.functions = new HashMap<>();
    }

    public Program checkProgram(Value program) throws Exception {
        if (!program.hasArrayElements()) {
            throw new Exception("Program must be array");
        }
        var len = program.getArraySize();
        var stmtOpCodes = new ArrayList<ArrayList<String>>();

        // We add each stmt's opcodes as a separate ArrayList...
        for (var i = 0; i < len; i++) {
            stmtOpCodes.add(checkStmt(program.getArrayElement(i), true));
        }

        // ...so that we can reverse the stmt order for the interpreter
        var opcodes = new ArrayList<String>();
        for (int j = (int)len - 1; j >= 0; j--) {
            opcodes.addAll(stmtOpCodes.get(j));
        }

        return new Program(opcodes, this.functions);
    }

    public ArrayList<String> checkStmt(Value stmt, boolean isTopLevel) throws Exception {
        var oldOpcodes = this.opcodes;
        this.opcodes = new ArrayList<>();

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

                this.opcodes.add("let");
                this.opcodes.add(varName.asString());
                this.opcodes.add("ident");
                Type type = checkExpr(stmt.getArrayElement(2));
                this.symbolTable.put(varName.asString(), type);

                break;
            }
            case "print": {
                if (stmt.getArraySize() != 2) {
                    throw new Exception("Print statements must take exactly one argument");
                }
                this.opcodes.add("print");
                checkExpr(stmt.getArrayElement(1));
                break;
            }
            case "if": {
                if (stmt.getArraySize() < 3 || stmt.getArraySize() > 4) {
                    throw new Exception("If statements must take a condition, a then block and an optional else block");
                }

                this.opcodes.add("end");
                if (stmt.getArraySize() == 4) {
                    var elseOpcodes = checkStmt(stmt.getArrayElement(3), false);
                    opcodes.addAll(elseOpcodes);
                }
                this.opcodes.add("else");
                var thenOpcodes = checkStmt(stmt.getArrayElement(2), false);
                this.opcodes.addAll(thenOpcodes);
                this.opcodes.add("if");
                checkExpr(stmt.getArrayElement(1));

                break;
            }
            case "fun": {
                if (!isTopLevel) {
                    throw new Exception("Functions must be defined at top level");
                }
                checkFunction(stmt);
                return new ArrayList<>();
            }
            default:
				throw new Exception("Operator " + opString + " not defined");
        }
        var opcodes = this.opcodes;
        this.opcodes = oldOpcodes;
        return opcodes;
    }

    public void checkFunction(Value fun) throws Exception {
        if (fun.getArraySize() != 4) {
            throw new Exception("fun take three arguments: name, parameters and body");
        }

        var funName = fun.getArrayElement(1);
        var params = fun.getArrayElement(2);

        var paramsList = new ArrayList<Param>();

        if (!funName.isString()) {
            throw new Exception("Function name must be an identifier");
        }
        if (this.functions.containsKey(funName.asString())) {
            throw new Exception("Cannot have two functions named `" + funName.asString() + "`");
        }
        if (!params.hasArrayElements()) {
            throw new Exception("Params must be an array");
        }

        var paramsLen = params.getArraySize();

        var oldSymbolTable = this.symbolTable;
        this.symbolTable = (HashMap<String, Type>) this.symbolTable.clone();

        for (var i = 0; i < paramsLen; i++) {
            var param = params.getArrayElement(i);

            if ((!param.hasArrayElements()) || (param.getArraySize() != 2)) {
                throw new Exception("Params consist of a name/type pair");
            }

            var paramName = param.getArrayElement(0);
            var paramType = param.getArrayElement(1);
            if (paramName.isString() && paramType.isString()) {
                var parsedParamType = parseParamType(paramType.asString());
                this.symbolTable.put(paramName.asString(), parsedParamType);
                paramsList.add(new Param(paramName.asString(), parsedParamType));
            } else {
                throw new Exception("Params must all be variable names");
            }
        }

        this.opcodes.add("return");
        var returnType = checkExpr(fun.getArrayElement(3));
        this.symbolTable = oldSymbolTable;

        this.functions.put(funName.asString(), new Function(this.opcodes, paramsList, returnType));
    }

    public Type parseParamType(String paramType) throws Exception {
        switch (paramType) {
            case "bool":
                return Type.BOOLEAN;
            case "number":
                return Type.NUMBER;
            case "string":
                return Type.STRING;
            default:
                throw new Exception("Invalid type " + paramType);
        }
    }

    public Type checkExpr(Value expr) throws Exception {
		if (!expr.hasArrayElements()) {
		    if (expr.fitsInDouble()) {
		        this.opcodes.add(String.valueOf(expr.asDouble()));
                this.opcodes.add("number");
                return Type.NUMBER;
            }
            // If it's just a naked string, it's a variable
		    if (expr.isString()) {
                var varName = expr.asString();
                if (varName.equals("true") || varName.equals("false")) {
                    this.opcodes.add(varName);
                    this.opcodes.add("bool");
                    return Type.BOOLEAN;
                }
		        Type varType = this.symbolTable.get(varName);
		        if (varType == null) {
		            throw new Exception("Variable `" + varName + "' not defined");
                }
		        this.opcodes.add(varName);
		        this.opcodes.add("var");
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
            this.opcodes.add(str.asString());
            this.opcodes.add("string");
            return Type.STRING;
        }

        if (this.functions.containsKey(opString)) {
            var fun = this.functions.get(opString);
            return checkCall(opString, expr, fun.params, fun.returnType);
        }

        return checkBinaryExpr(opString, expr);
    }

    Type checkCall(String funName, Value callExpr, ArrayList<Param> params, Type returnType) throws Exception {
        this.opcodes.add("call");
        this.opcodes.add(funName);
        this.opcodes.add("ident");
        if (callExpr.getArraySize() != (params.size() + 1)) {
            throw new Exception("Function `" + funName + "` not called with right number of arguments");
        }

        for (int i = 0; i < params.size(); i++) {
            var arg = callExpr.getArrayElement(i + 1);
            var param = params.get(i);
            var argType = this.checkExpr(arg);
            if (argType != param.type) {
                throw new Exception("Param `" + param.name + "` expected type" + param.type + "received " + argType);
            }
        }

        return returnType;
    }

    Type checkBinaryExpr(String op, Value expr) throws Exception {
        if (expr.getArraySize() != 3) {
            throw new Exception("Only binary operations are supported");
        }

        this.opcodes.add(op);
        var lhsType = checkExpr(expr.getArrayElement(1));
        var rhsType = checkExpr(expr.getArrayElement(2));

        Type type;
        switch (op) {
            case "+":
            case "-":
            case "*":
            case "/":
                if (lhsType != Type.NUMBER || rhsType != Type.NUMBER) {
                    throw new Exception("Invalid operator " + op + " for types " + lhsType + " and " + rhsType);
                }
                type = Type.NUMBER;
                break;
            case "==":
            case "!=":
                if (!(lhsType == Type.NUMBER && rhsType == Type.NUMBER) &&
                        !(lhsType == Type.STRING && rhsType == Type.STRING)) {
                    throw new Exception("Invalid operator " + op + " for types " + lhsType + " and " + rhsType);
                }
                type = Type.BOOLEAN;
                break;
            case ">=":
            case "<=":
            case ">":
            case "<":
                if (lhsType != Type.NUMBER || rhsType != Type.NUMBER) {
                    throw new Exception("Invalid operator " + op + " for types " + lhsType + " and " + rhsType);
                }
                type = Type.BOOLEAN;
                break;
            default:
                throw new Exception("Invalid operator " + op + " for types " + lhsType + " and " + rhsType);
        }

        return type;
    }
}
