package tagalog;

import java.util.List;

public sealed interface Expr {
    record Literal(Object value) implements Expr {}

    record Variable(Token name) implements Expr {}

    record Unary(Token operator, Expr right) implements Expr {}

    record Binary(Expr left, Token operator, Expr right) implements Expr {}

    record Grouping(Expr expression) implements Expr {}

    // kuwadrado(5)
    record Call(Expr callee, Token paren, List<Expr> arguments) implements Expr {}
}
