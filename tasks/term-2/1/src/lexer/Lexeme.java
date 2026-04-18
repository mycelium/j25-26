package lexer;

public class Lexeme {
    public enum LexemeType {
        STRING,
        NUMBER,
        BOOL,
        NULL,
        LeftBRACE,
        RightBRACE,
        LeftBRACKET,
        RightBRACKET,
        COMMA,
        COLON
    }
    private final LexemeType type;
    private final String value;

    Lexeme(LexemeType type, String value) {
        this.type = type;
        this.value = value;
    }

    public LexemeType getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }
}


