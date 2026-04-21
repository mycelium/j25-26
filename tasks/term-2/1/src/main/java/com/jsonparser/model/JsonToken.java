package com.jsonparser.core;

/**
 * Представление токена JSON
 */
public class JsonToken {
    public enum TokenType {
        BEGIN_OBJECT,  // {
        END_OBJECT,    // }
        BEGIN_ARRAY,   // [
        END_ARRAY,     // ]
        COLON,         // :
        COMMA,         // ,
        STRING,        // "..."
        NUMBER,        // 123, 45.67
        TRUE,          // true
        FALSE,         // false
        NULL           // null
    }

    private final TokenType type;
    private final Object value;

    public JsonToken(TokenType type) {
        this(type, null);
    }

    public JsonToken(TokenType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String getStringValue() {
        return (String) value;
    }

    public String getNumberValue() {
        return (String) value;
    }
}