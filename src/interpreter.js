function evalProgram(opcodes, functions, stack = [], bindings = new Map(), depth = 0) {
    while (opcodes.length > 0) {
        const opcode = opcodes.pop();
        switch (opcode) {
            case "bool":
                stack.push(opcodes.pop() === "true");
                break;
            case "number":
                stack.push(Number.parseFloat(opcodes.pop()));
                break;
            case "string":
                stack.push(opcodes.pop());
                break;
            case "ident":
                stack.push(opcodes.pop());
                break;
            case "var":
                stack.push(bindings.get(opcodes.pop()));
                break;
            case "call": {
                const name = stack.pop();
                const fun = functions.get(name);
                const newBindings = new Map(bindings);
                for (const param of fun.params) {
                    newBindings.set(param, stack.pop());
                }
                try {
                    evalProgram([...fun.body], functions, stack, newBindings, depth + 1);
                } catch (e) {
                    stack.push(e);
                }
                break;
            }
            case "return":
                throw stack.pop()
            case "if":
                const cond = stack.pop();
                if (cond) {
                    evalProgram(opcodes, functions, stack, bindings, depth + 1);
                } else {
                    while (opcodes.pop() !== "else") {}
                    evalProgram(opcodes, functions, stack, bindings, depth + 1);
                }

                break;
            case "else":
            case "end":
                return;
            case "==": {
                const lhs = stack.pop();
                const rhs = stack.pop();
                stack.push(lhs === rhs);
                break;
            }
            case "!=": {
                const lhs = stack.pop();
                const rhs = stack.pop();
                stack.push(lhs !== rhs);
                break;
            }
            case ">": {
                const lhs = stack.pop();
                const rhs = stack.pop();
                stack.push(lhs > rhs);
                break;
            }
            case "<": {
                const lhs = stack.pop();
                const rhs = stack.pop();
                stack.push(lhs < rhs);
                break;
            }
            case "<=": {
                const lhs = stack.pop();
                const rhs = stack.pop();
                stack.push(lhs <= rhs);
                break;
            }
            case ">=": {
                const lhs = stack.pop();
                const rhs = stack.pop();
                stack.push(lhs >= rhs);
                break;
            }
            case "+": {
                const lhs = stack.pop();
                const rhs = stack.pop();
                stack.push(lhs + rhs);
                break;
            }
            case "-": {
                const lhs = stack.pop();
                const rhs = stack.pop();
                stack.push(lhs - rhs);
                break;
            }
            case "*": {
                const lhs = stack.pop();
                const rhs = stack.pop();
                stack.push(lhs * rhs);
                break;
            }
            case "/": {
                const lhs = stack.pop();
                const rhs = stack.pop();
                stack.push(lhs / rhs);
                break;
            }
            case "print": {
                console.log(stack.pop());
                break;
            }
            case "let": {
                const ident = stack.pop();
                const value = stack.pop();
                bindings.set(ident, value);
                break;
            }
            default: {
                throw new Error("Unexpected opcode: " + opcode)
            }
        }
    }
    if (depth > 0) {
        throw new Error("Depth should be 0 at end of opcodes, instead is " + depth);
    }
}