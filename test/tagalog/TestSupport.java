package tagalog;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Helper methods shared by the test classes. */
final class TestSupport {
    private TestSupport() {}

    /** Runs a Tagalog program and returns everything it printed. */
    static String run(String source) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            List<Stmt> statements = new Parser(new Lexer(source).scanTokens()).parse();
            new Interpreter().interpret(statements);
        } finally {
            System.setOut(original);
        }
        return normalize(buffer.toString(StandardCharsets.UTF_8));
    }

    /** Parses a Tagalog program and returns the syntax tree as text. */
    static String tree(String source) {
        List<Stmt> statements = new Parser(new Lexer(source).scanTokens()).parse();
        return normalize(new AstPrinter().print(statements));
    }

    private static String normalize(String text) {
        return text.replace("\r\n", "\n");
    }
}
