package tagalog;

import java.util.List;

public sealed interface Stmt {
    // itakda bilang = 1
    record Assign(Token name, Expr value) implements Stmt {}

    // sabihin bilang
    record Print(Expr expression) implements Stmt {}

    // kung ... kundi ... tapos
    record If(Expr condition, List<Stmt> thenBranch, List<Stmt> elseBranch) implements Stmt {}

    // habang ... tapos
    record While(Expr condition, List<Stmt> body) implements Stmt {}
}
