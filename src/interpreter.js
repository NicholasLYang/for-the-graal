function evalProgram(opcodes, stack = [], bindings = new Map(), depth = 0) {
    if (depth === 0) {
        console.log(opcodes);
    }
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
            case "if":
                const cond = stack.pop();
                if (cond) {
                    try {
                        evalProgram(opcodes, stack, bindings, depth + 1);
                    } catch (e) {
                        if (e === "else") {
                            while (opcodes.pop() !== "end") {}
                        }
                    }
                } else {
                    while (opcodes.pop() !== "else") {}
                    try {
                        evalProgram(opcodes, stack, bindings, depth + 1);
                    } catch (e) {
                        // We threw because end
                    }
                }

                break;
            case "else":
                throw "else";
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
        }
    }
}