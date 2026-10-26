package tagalog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Gamit: java tagalog.Main <file.tgl>");
            return;
        }

        String source = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);

        try {
            List<Token> tokens = new Lexer(source).scanTokens();
            for (Token token : tokens) {
                System.out.println(token);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
