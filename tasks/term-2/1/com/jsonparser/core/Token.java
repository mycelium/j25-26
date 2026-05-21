package com.jsonparser.core;

public record Token(Type kind, Object data) {

    public enum Type {
        OBJECT_START,
        OBJECT_END,
        ARRAY_START,
        ARRAY_END,
        COLON,
        COMMA,
        TEXT,
        NUM,
        BOOL_TRUE,
        BOOL_FALSE,
        EMPTY
    }

    public Token(Type kind) {
        this(kind, null);
    }

    public String asText() {
        return (String) data;
    }

    public String asNumber() {
        return (String) data;
    }
}