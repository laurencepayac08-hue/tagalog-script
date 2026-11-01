package tagalog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Gamit: java tagalog.Main <file.tgl> [--tokens | --tree]");
            return;
        }

        String source = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
        String option = args.length > 1 ? args[1] : "";

        try {
            List<Token> tokens = new Lexer(source).scanTokens();

            if (option.equals("--tokens")) {
                for (Token token : tokens) {
                    System.out.println(token);
                }
                return;
            }

            List<Stmt> statements = new Parser(tokens).parse();

            if (option.equals("--tree")) {
                System.out.print(new AstPrinter().print(statements));
                return;
            }

            new Interpreter().interpret(statements);
        } catch (IllegalArgumentException | ParseException | RuntimeError e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
