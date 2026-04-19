package jsonparser;

enum TokenType {
    BEGIN_OBJECT, END_OBJECT,
    BEGIN_ARRAY, END_ARRAY,
    COMMA, COLON,
    STRING, NUMBER, BOOLEAN, NULL,
    EOF
}
