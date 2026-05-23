package jsonparser;

enum TokenType {
    LBRACE,    // {
    RBRACE,    // }
    LBRACKET,  // [
    RBRACKET,  // ]
    COMMA,     // ,
    COLON,     // :
    STRING,
    NUMBER,
    BOOLEAN,
    NULL,
    EOF
}
