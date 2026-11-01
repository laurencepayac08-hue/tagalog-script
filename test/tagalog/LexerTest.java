package tagalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class LexerTest {
    private List<TokenType> types(String source) {
        return new Lexer(source).scanTokens().stream().map(token -> token.type).toList();
    }

    @Test
    void scansVariableDeclaration() {
        assertEquals(
                List.of(TokenType.ITAKDA, TokenType.IDENTIFIER, TokenType.EQUAL, TokenType.NUMBER, TokenType.EOF),
                types("itakda x = 1"));
    }

    @Test
    void scansTagalogKeywords() {
        assertEquals(
                List.of(TokenType.KUNG, TokenType.KUNDI, TokenType.HABANG, TokenType.TAPOS,
                        TokenType.GAWIN, TokenType.IBALIK, TokenType.SABIHIN, TokenType.EOF),
                types("kung kundi habang tapos gawin ibalik sabihin"));
    }

    @Test
    void scansLogicWords() {
        assertEquals(
                List.of(TokenType.TOTOO, TokenType.AT, TokenType.MALI, TokenType.O, TokenType.HINDI, TokenType.EOF),
                types("totoo at mali o hindi"));
    }

    @Test
    void scansComparisonOperators() {
        assertEquals(
                List.of(TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL, TokenType.EQUAL_EQUAL,
                        TokenType.BANG_EQUAL, TokenType.LESS, TokenType.GREATER, TokenType.EOF),
                types("<= >= == != < >"));
    }

    @Test
    void scansParenthesesAndComma() {
        assertEquals(
                List.of(TokenType.LEFT_PAREN, TokenType.NUMBER, TokenType.COMMA, TokenType.NUMBER,
                        TokenType.RIGHT_PAREN, TokenType.EOF),
                types("(1, 2)"));
    }

    @Test
    void stringTokenHoldsTextWithoutQuotes() {
        Token token = new Lexer("\"kumusta\"").scanTokens().get(0);
        assertEquals(TokenType.STRING, token.type);
        assertEquals("kumusta", token.lexeme);
    }

    @Test
    void scansDecimalNumber() {
        Token token = new Lexer("3.14").scanTokens().get(0);
        assertEquals(TokenType.NUMBER, token.type);
        assertEquals("3.14", token.lexeme);
    }

    @Test
    void skipsComments() {
        assertEquals(
                List.of(TokenType.NEWLINE, TokenType.SABIHIN, TokenType.NUMBER, TokenType.EOF),
                types("# paliwanag\nsabihin 1"));
    }

    @Test
    void tracksLineNumbers() {
        List<Token> tokens = new Lexer("a\nb\nc").scanTokens();
        assertEquals(1, tokens.get(0).line);
        assertEquals(2, tokens.get(2).line);
        assertEquals(3, tokens.get(4).line);
    }

    @Test
    void identifierMayContainUnderscoreAndDigits() {
        Token token = new Lexer("walang_hanggan2").scanTokens().get(0);
        assertEquals(TokenType.IDENTIFIER, token.type);
        assertEquals("walang_hanggan2", token.lexeme);
    }

    @Test
    void rejectsUnknownCharacter() {
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> new Lexer("@").scanTokens());
        assertTrue(error.getMessage().contains("Linya 1"));
    }

    @Test
    void rejectsUnterminatedString() {
        assertThrows(IllegalArgumentException.class, () -> new Lexer("\"hindi tapos").scanTokens());
    }
}
