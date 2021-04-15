import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;

public class Main {
    public static void main(String[] args) throws IOException {
	Path parserPath = Path.of("parser.rb");
	String rubySourceCode = Files.readString(parserPath);

	Context context = Context.newBuilder().allowAllAccess(true).build();
	context.eval("ruby", rubySourceCode);
    }
}
