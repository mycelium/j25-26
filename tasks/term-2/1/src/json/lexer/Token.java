package json.lexer;

public class Token {

    public enum Type {
        LBRACE, RBRACE, LBRACKET, RBRACKET, COMMA, COLON,
        STRING, NUMBER, BOOLEAN, NULL,
        EOF
    }

    private final Type type;
    private final String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
