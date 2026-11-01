package tagalog;

import java.util.ArrayList;
import java.util.List;

/*
 * Grammar
 *
 * program     -> statement* EOF
 * statement   -> assign | print | if | while
 * assign      -> "itakda" IDENTIFIER "=" expression NEWLINE
 * print       -> "sabihin" expression NEWLINE
 * if          -> "kung" expression NEWLINE block ( "kundi" NEWLINE block )? "tapos" NEWLINE
 * while       -> "habang" expression NEWLINE block "tapos" NEWLINE
 *
 * expression  -> or
 * or          -> and ( "o" and )*
 * and         -> equality ( "at" equality )*
 * equality    -> comparison ( ( "==" | "!=" ) comparison )*
 * comparison  -> term ( ( "<" | "<=" | ">" | ">=" ) term )*
 * term        -> factor ( ( "+" | "-" ) factor )*
 * factor      -> unary ( ( "*" | "/" ) unary )*
 * unary       -> ( "hindi" | "-" ) unary | primary
 * primary     -> NUMBER | STRING | "totoo" | "mali" | IDENTIFIER | "(" expression ")"
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        skipNewlines();
        while (!isAtEnd()) {
            statements.add(statement());
            skipNewlines();
        }
        return statements;
    }

    // Statements

    private Stmt statement() {
        if (match(TokenType.ITAKDA)) return assignStatement();
        if (match(TokenType.SABIHIN)) return printStatement();
        if (match(TokenType.KUNG)) return ifStatement();
        if (match(TokenType.HABANG)) return whileStatement();
        throw error(peek(), "inaasahan ang 'itakda', 'sabihin', 'kung', o 'habang'");
    }

    private Stmt assignStatement() {
        Token name = consume(TokenType.IDENTIFIER, "inaasahan ang pangalan ng variable pagkatapos ng 'itakda'");
        consume(TokenType.EQUAL, "inaasahan ang '=' pagkatapos ng pangalan ng variable");
        Expr value = expression();
        endStatement();
        return new Stmt.Assign(name, value);
    }

    private Stmt printStatement() {
        Expr value = expression();
        endStatement();
        return new Stmt.Print(value);
    }

    private Stmt ifStatement() {
        Expr condition = expression();
        consume(TokenType.NEWLINE, "inaasahan ang bagong linya pagkatapos ng kondisyon");
        List<Stmt> thenBranch = block();
        List<Stmt> elseBranch = new ArrayList<>();
        if (match(TokenType.KUNDI)) {
            consume(TokenType.NEWLINE, "inaasahan ang bagong linya pagkatapos ng 'kundi'");
            elseBranch = block();
        }
        consume(TokenType.TAPOS, "inaasahan ang 'tapos' sa dulo ng 'kung'");
        endStatement();
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        Expr condition = expression();
        consume(TokenType.NEWLINE, "inaasahan ang bagong linya pagkatapos ng kondisyon");
        List<Stmt> body = block();
        consume(TokenType.TAPOS, "inaasahan ang 'tapos' sa dulo ng 'habang'");
        endStatement();
        return new Stmt.While(condition, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        skipNewlines();
        while (!check(TokenType.TAPOS) && !check(TokenType.KUNDI) && !isAtEnd()) {
            statements.add(statement());
            skipNewlines();
        }
        return statements;
    }

    private void endStatement() {
        if (match(TokenType.NEWLINE)) return;
        if (isAtEnd()) return;
        throw error(peek(), "inaasahan ang bagong linya pagkatapos ng pahayag");
    }

    // Expressions

    private Expr expression() {
        return or();
    }

    private Expr or() {
        Expr expr = and();
        while (match(TokenType.O)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(TokenType.AT)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.HINDI, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.NUMBER)) return new Expr.Literal(Double.parseDouble(previous().lexeme));
        if (match(TokenType.STRING)) return new Expr.Literal(previous().lexeme);
        if (match(TokenType.TOTOO)) return new Expr.Literal(Boolean.TRUE);
        if (match(TokenType.MALI)) return new Expr.Literal(Boolean.FALSE);
        if (match(TokenType.IDENTIFIER)) return new Expr.Variable(previous());
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "inaasahan ang ')' pagkatapos ng expression");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "inaasahan ang expression");
    }

    // Helpers

    private void skipNewlines() {
        while (match(TokenType.NEWLINE)) {
            // skip blank lines
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseException error(Token token, String message) {
        return new ParseException("Linya " + token.line + ": " + message + " (nakita: " + describe(token) + ")");
    }

    private String describe(Token token) {
        if (token.type == TokenType.EOF) return "dulo ng file";
        if (token.type == TokenType.NEWLINE) return "dulo ng linya";
        return "'" + token.lexeme + "'";
    }
}
