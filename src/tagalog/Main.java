package tagalog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Gamit: java tagalog.Main <file.tgl> [--tokens]");
            return;
        }

        String source = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
        boolean showTokens = args.length > 1 && args[1].equals("--tokens");

        try {
            List<Token> tokens = new Lexer(source).scanTokens();

            if (showTokens) {
                for (Token token : tokens) {
                    System.out.println(token);
                }
                return;
            }

            List<Stmt> statements = new Parser(tokens).parse();
            System.out.print(new AstPrinter().print(statements));
        } catch (IllegalArgumentException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
