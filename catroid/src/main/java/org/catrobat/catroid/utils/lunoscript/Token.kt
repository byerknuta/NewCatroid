package org.catrobat.catroid.utils.lunoscript

enum class TokenType {
    VAR, IF, ELSE, FUN, CLASS, STATIC, RETURN, TRUE, FALSE, NULL, WHILE, FOR, IN, SWITCH, CASE, DEFAULT, BREAK, CONTINUE, THIS, SUPER, IMPORT, TRY, CATCH, FINALLY,

    IDENTIFIER,
    NUMBER_LITERAL,
    F_STRING,
    STRING_LITERAL,
    FLOAT_LITERAL,

    ASSIGN,         // =
    PLUS,           // +
    MINUS,          // -
    MULTIPLY,       // *
    DIVIDE,         // /
    MODULO,         // %
    EQ,             // ==
    NEQ,            // !=
    LT,             // <
    GT,             // >
    LTE,            // <=
    GTE,            // >=
    PLUS_ASSIGN,    // +=
    MINUS_ASSIGN,   // -=
    MULTIPLY_ASSIGN,// *=
    DIVIDE_ASSIGN,  // /=
    MODULO_ASSIGN,  // %=
    BANG,           // ! (logical NOT)
    AND,            // &&
    OR,             // ||

    // Punctuation
    LPAREN,         // (
    RPAREN,         // )
    LBRACE,         // {
    RBRACE,         // }
    LBRACKET,       // [
    RBRACKET,       // ]
    COMMA,          // ,
    DOT,            // .
    SEMICOLON,      // ;
    COLON,          // :
    ARROW,          // -> (lambda)


    COMMENT,

    // End of File
    EOF
}

data class Token(
    val type: TokenType,
    val lexeme: String,
    val literal: Any?,
    val line: Int,
    val position: Int
)
