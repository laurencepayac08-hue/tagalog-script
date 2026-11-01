package tagalog;

import java.util.List;
import java.util.stream.Collectors;

public class AstPrinter {
    public String print(List<Stmt> statements) {
        return block(statements, 0);
    }

    private String block(List<Stmt> statements, int indent) {
        StringBuilder sb = new StringBuilder();
        for (Stmt statement : statements) {
            sb.append(stmt(statement, indent)).append("\n");
        }
        return sb.toString();
    }

    private String stmt(Stmt statement, int indent) {
        String pad = "  ".repeat(indent);

        if (statement instanceof Stmt.Assign a) {
            return pad + "Itakda " + a.name().lexeme + " = " + expr(a.value());
        }
        if (statement instanceof Stmt.Print p) {
            return pad + "Sabihin " + expr(p.expression());
        }
        if (statement instanceof Stmt.If i) {
            StringBuilder sb = new StringBuilder();
            sb.append(pad).append("Kung ").append(expr(i.condition())).append("\n");
            sb.append(block(i.thenBranch(), indent + 1));
            if (!i.elseBranch().isEmpty()) {
                sb.append(pad).append("Kundi\n");
                sb.append(block(i.elseBranch(), indent + 1));
            }
            sb.append(pad).append("Tapos");
            return sb.toString();
        }
        if (statement instanceof Stmt.While w) {
            StringBuilder sb = new StringBuilder();
            sb.append(pad).append("Habang ").append(expr(w.condition())).append("\n");
            sb.append(block(w.body(), indent + 1));
            sb.append(pad).append("Tapos");
            return sb.toString();
        }
        if (statement instanceof Stmt.Function f) {
            String params = f.params().stream().map(t -> t.lexeme).collect(Collectors.joining(", "));
            StringBuilder sb = new StringBuilder();
            sb.append(pad).append("Gawin ").append(f.name().lexeme).append("(").append(params).append(")\n");
            sb.append(block(f.body(), indent + 1));
            sb.append(pad).append("Tapos");
            return sb.toString();
        }
        if (statement instanceof Stmt.Return r) {
            if (r.value() == null) return pad + "Ibalik";
            return pad + "Ibalik " + expr(r.value());
        }
        if (statement instanceof Stmt.Expression e) {
            return pad + expr(e.expression());
        }
        throw new IllegalStateException("Hindi kilalang pahayag");
    }

    private String expr(Expr expression) {
        if (expression instanceof Expr.Literal l) {
            return literal(l.value());
        }
        if (expression instanceof Expr.Variable v) {
            return v.name().lexeme;
        }
        if (expression instanceof Expr.Unary u) {
            return "(" + u.operator().lexeme + " " + expr(u.right()) + ")";
        }
        if (expression instanceof Expr.Binary b) {
            return "(" + b.operator().lexeme + " " + expr(b.left()) + " " + expr(b.right()) + ")";
        }
        if (expression instanceof Expr.Grouping g) {
            return "(grupo " + expr(g.expression()) + ")";
        }
        if (expression instanceof Expr.Call c) {
            StringBuilder sb = new StringBuilder("(tawag ").append(expr(c.callee()));
            for (Expr argument : c.arguments()) {
                sb.append(" ").append(expr(argument));
            }
            return sb.append(")").toString();
        }
        throw new IllegalStateException("Hindi kilalang expression");
    }

    private String literal(Object value) {
        if (value instanceof Double d) {
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf(d.longValue());
            }
            return d.toString();
        }
        if (value instanceof Boolean b) {
            return b ? "totoo" : "mali";
        }
        return "\"" + value + "\"";
    }
}
