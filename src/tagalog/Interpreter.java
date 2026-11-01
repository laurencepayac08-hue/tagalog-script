package tagalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Interpreter {
    private final Map<String, Object> variables = new HashMap<>();

    public void interpret(List<Stmt> statements) {
        execute(statements);
    }

    // Statements

    private void execute(List<Stmt> statements) {
        for (Stmt statement : statements) {
            execute(statement);
        }
    }

    private void execute(Stmt statement) {
        if (statement instanceof Stmt.Assign a) {
            variables.put(a.name().lexeme, evaluate(a.value()));
        } else if (statement instanceof Stmt.Print p) {
            System.out.println(stringify(evaluate(p.expression())));
        } else if (statement instanceof Stmt.If i) {
            if (isTrue(evaluate(i.condition()), lineOf(i.condition()))) {
                execute(i.thenBranch());
            } else {
                execute(i.elseBranch());
            }
        } else if (statement instanceof Stmt.While w) {
            while (isTrue(evaluate(w.condition()), lineOf(w.condition()))) {
                execute(w.body());
            }
        } else {
            throw new IllegalStateException("Hindi kilalang pahayag");
        }
    }

    // Expressions

    private Object evaluate(Expr expression) {
        if (expression instanceof Expr.Literal l) {
            return l.value();
        }
        if (expression instanceof Expr.Grouping g) {
            return evaluate(g.expression());
        }
        if (expression instanceof Expr.Variable v) {
            String name = v.name().lexeme;
            if (!variables.containsKey(name)) {
                throw error(v.name().line, "walang variable na '" + name + "'");
            }
            return variables.get(name);
        }
        if (expression instanceof Expr.Unary u) {
            return unary(u);
        }
        if (expression instanceof Expr.Binary b) {
            return binary(b);
        }
        throw new IllegalStateException("Hindi kilalang expression");
    }

    private Object unary(Expr.Unary expr) {
        Object right = evaluate(expr.right());
        Token operator = expr.operator();

        switch (operator.type) {
            case MINUS:
                if (!(right instanceof Double)) {
                    throw error(operator.line, "ang '-' ay para sa numero lamang");
                }
                return -(Double) right;
            case HINDI:
                if (!(right instanceof Boolean)) {
                    throw error(operator.line, "ang 'hindi' ay para sa totoo o mali lamang");
                }
                return !(Boolean) right;
            default:
                throw new IllegalStateException("Hindi kilalang operator");
        }
    }

    private Object binary(Expr.Binary expr) {
        Token operator = expr.operator();

        if (operator.type == TokenType.AT || operator.type == TokenType.O) {
            return logical(expr);
        }

        Object left = evaluate(expr.left());
        Object right = evaluate(expr.right());

        switch (operator.type) {
            case PLUS:
                if (left instanceof Double l && right instanceof Double r) {
                    return l + r;
                }
                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }
                throw error(operator.line, "ang '+' ay para sa numero o text lamang");
            case MINUS:
                checkNumbers(operator, left, right);
                return (Double) left - (Double) right;
            case STAR:
                checkNumbers(operator, left, right);
                return (Double) left * (Double) right;
            case SLASH:
                checkNumbers(operator, left, right);
                if ((Double) right == 0.0) {
                    throw error(operator.line, "hindi maaaring hatiin sa zero");
                }
                return (Double) left / (Double) right;
            case LESS:
                checkNumbers(operator, left, right);
                return (Double) left < (Double) right;
            case LESS_EQUAL:
                checkNumbers(operator, left, right);
                return (Double) left <= (Double) right;
            case GREATER:
                checkNumbers(operator, left, right);
                return (Double) left > (Double) right;
            case GREATER_EQUAL:
                checkNumbers(operator, left, right);
                return (Double) left >= (Double) right;
            case EQUAL_EQUAL:
                return Objects.equals(left, right);
            case BANG_EQUAL:
                return !Objects.equals(left, right);
            default:
                throw new IllegalStateException("Hindi kilalang operator");
        }
    }

    // "at" and "o" stop early, so the right side runs only when it is needed.
    private Object logical(Expr.Binary expr) {
        Token operator = expr.operator();
        Object left = evaluate(expr.left());
        checkBoolean(operator, left);

        if (operator.type == TokenType.O && (Boolean) left) return true;
        if (operator.type == TokenType.AT && !(Boolean) left) return false;

        Object right = evaluate(expr.right());
        checkBoolean(operator, right);
        return right;
    }

    // Helpers

    private boolean isTrue(Object value, int line) {
        if (value instanceof Boolean b) return b;
        throw error(line, "ang kondisyon ay dapat totoo o mali");
    }

    private void checkNumbers(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw error(operator.line, "ang '" + operator.lexeme + "' ay para sa numero lamang");
    }

    private void checkBoolean(Token operator, Object value) {
        if (value instanceof Boolean) return;
        throw error(operator.line, "ang '" + operator.lexeme + "' ay para sa totoo o mali lamang");
    }

    // Finds the first line number inside an expression. A plain literal has none.
    private int lineOf(Expr expression) {
        if (expression instanceof Expr.Variable v) return v.name().line;
        if (expression instanceof Expr.Unary u) return u.operator().line;
        if (expression instanceof Expr.Binary b) return b.operator().line;
        if (expression instanceof Expr.Grouping g) return lineOf(g.expression());
        return -1;
    }

    private RuntimeError error(int line, String message) {
        if (line > 0) {
            return new RuntimeError("Linya " + line + ": " + message);
        }
        return new RuntimeError(message);
    }

    private String stringify(Object value) {
        if (value instanceof Double d) {
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf(d.longValue());
            }
            return d.toString();
        }
        if (value instanceof Boolean b) {
            return b ? "totoo" : "mali";
        }
        return String.valueOf(value);
    }
}
