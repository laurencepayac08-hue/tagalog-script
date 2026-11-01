package tagalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("itakda", TokenType.ITAKDA);
        KEYWORDS.put("sabihin", TokenType.SABIHIN);
        KEYWORDS.put("kung", TokenType.KUNG);
        KEYWORDS.put("kundi", TokenType.KUNDI);
        KEYWORDS.put("habang", TokenType.HABANG);
        KEYWORDS.put("tapos", TokenType.TAPOS);
        KEYWORDS.put("totoo", TokenType.TOTOO);
        KEYWORDS.put("mali", TokenType.MALI);
        KEYWORDS.put("at", TokenType.AT);
        KEYWORDS.put("o", TokenType.O);
        KEYWORDS.put("hindi", TokenType.HINDI);
        KEYWORDS.put("gawin", TokenType.GAWIN);
        KEYWORDS.put("ibalik", TokenType.IBALIK);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': add(TokenType.LEFT_PAREN); break;
            case ')': add(TokenType.RIGHT_PAREN); break;
            case ',': add(TokenType.COMMA); break;
            case '+': add(TokenType.PLUS); break;
            case '-': add(TokenType.MINUS); break;
            case '*': add(TokenType.STAR); break;
            case '/': add(TokenType.SLASH); break;
            case '=': add(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': add(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': add(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case '!':
                if (match('=')) {
                    add(TokenType.BANG_EQUAL);
                } else {
                    error("hindi kilalang karakter '!'");
                }
                break;
            case '#': // comment until end of line
                while (peek() != '\n' && !isAtEnd()) advance();
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                tokens.add(new Token(TokenType.NEWLINE, "\\n", line));
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error("hindi kilalang karakter '" + c + "'");
                }
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            error("hindi natapos ang string");
        }
        advance(); // closing quote
        String value = source.substring(start + 1, current - 1);
        tokens.add(new Token(TokenType.STRING, value, line));
    }

    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        add(TokenType.NUMBER);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        add(type);
    }

    private void add(TokenType type) {
        tokens.add(new Token(type, source.substring(start, current), line));
    }

    private void error(String message) {
        throw new IllegalArgumentException("Linya " + line + ": " + message);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
