import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException, Exception {
        if (args.length == 1) {
            Path codePath = Path.of(args[0]);
            runCode(Files.readString(codePath));
        } else {
            System.err.println("Usage: ftg <code_path>");
        }
    }

    public static void runCode(String code) throws IOException, Exception {
        var context = setupContext();

        Value program = context.eval("ruby", "parse_program '" + code + "'");

        var typeChecker = new TypeChecker();
        var compiledProgram = typeChecker.checkProgram(program);
        var jsCode = compiledProgram.createJavaScriptCode();

        context.eval("js", jsCode);
    }

    public static Context setupContext() throws IOException {
        Context context = Context.newBuilder().allowAllAccess(true).build();

        Path parserPath = Path.of("src/parser.rb");
        String rubySourceCode = Files.readString(parserPath);
        context.eval("ruby", rubySourceCode);

        Path interpreterPath = Path.of("src/interpreter.js");
        String jsInterpreterCode = Files.readString(interpreterPath);
        context.eval("js", jsInterpreterCode);

        return context;
    }

    public static void printProgram(Value program) {
        if (program.hasArrayElements()) {
            var len = program.getArraySize();
            System.out.print("[");
            for (var i = 0; i < len; i++) {
                printProgram(program.getArrayElement(i));
            }
            System.out.print("]");
        } else {
            if (program.isNumber()) {
                System.out.print(program.asDouble());
            }
            if (program.isString()) {
                System.out.print(program.asString());
            }
            System.out.print(" ");
        }
    }
}
