package jsonparser;

class JsonToken {
    enum Type {
        LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET,
        COLON, COMMA, STRING, NUMBER, BOOLEAN, NULL
    }
    
    private final Type type;
    private final String value;
    
    public JsonToken(Type type, String value) {
        this.type = type;
        this.value = value;
    }
    
    public Type getType() { return type; }
    public String getValue() { return value; }
    
    @Override
    public String toString() {
        return type + ": " + value;
    }
}
