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
    GAWIN,     // define a function
    IBALIK,    // return a value

    // Operators
    PLUS, MINUS, STAR, SLASH,
    EQUAL, EQUAL_EQUAL, BANG_EQUAL,
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    LEFT_PAREN, RIGHT_PAREN, COMMA,

    // Structure
    NEWLINE, EOF
}
