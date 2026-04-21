package com.jsonparser.exception;

/**
 * Исключение для ошибок парсинга JSON
 */
public class JsonParseException extends RuntimeException {

    public JsonParseException(String message) {
        super(message);
    }

    public JsonParseException(String message, Throwable cause) {
        super(message, cause);
    }
}