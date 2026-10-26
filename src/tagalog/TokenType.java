package tagalog;

public enum TokenType {
    // Literals
    NUMBER, STRING, IDENTIFIER,

    // Keywords
    ITAKDA,    // set a variable
    SABIHIN,   // print
    KUNG,      // if
    KUNDI,     // else
    HABANG,    // while
    TAPOS,     // end of block
    TOTOO,     // true
    MALI,      // false
    AT,        // and
    O,         // or
    HINDI,     // not

    // Operators
    PLUS, MINUS, STAR, SLASH,
    EQUAL, EQUAL_EQUAL, BANG_EQUAL,
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    LEFT_PAREN, RIGHT_PAREN,

    // Structure
    NEWLINE, EOF
}
