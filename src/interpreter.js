function evalProgram(opcodes) {
    const stack = []
    const bindings = new Map();

    while (opcodes.length > 0) {
        const opcode = opcodes.pop();
        switch (opcode) {
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
