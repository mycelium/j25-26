package jsonlib;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class JsonReader {
    private final String text;
    private int cursor;

    JsonReader(String text) {
        if (text == null) {
            throw new JsonException("JSON text must not be null");
        }
        this.text = text;
    }

    Object read() {
        skipWhitespace();
        Object value = readValue();
        skipWhitespace();
        if (!isAtEnd()) {
            throw error("Unexpected data after JSON value");
        }
        return value;
    }

    private Object readValue() {
        skipWhitespace();
        if (isAtEnd()) {
            throw error("Unexpected end of input");
        }

        char first = peek();
        return switch (first) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default -> {
                if (first == '-' || Character.isDigit(first)) {
                    yield readNumber();
                }
                throw error("Unexpected character '" + first + "'");
            }
        };
    }

    private Map<String, Object> readObject() {
        expect('{');
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        skipWhitespace();
        if (tryConsume('}')) {
            return values;
        }

        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw error("Object key must be a string");
            }
            String key = readString();
            skipWhitespace();
            expect(':');
            values.put(key, readValue());
            skipWhitespace();

            if (tryConsume('}')) {
                return values;
            }
            expect(',');
        }
    }

    private List<Object> readArray() {
        expect('[');
        ArrayList<Object> values = new ArrayList<>();
        skipWhitespace();
        if (tryConsume(']')) {
            return values;
        }

        while (true) {
            values.add(readValue());
            skipWhitespace();
            if (tryConsume(']')) {
                return values;
            }
            expect(',');
        }
    }

    private String readString() {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (!isAtEnd()) {
            char current = next();
            if (current == '"') {
                return builder.toString();
            }
            if (current < 0x20) {
                throw error("Control character is not allowed in JSON string");
            }
            if (current == '\\') {
                builder.append(readEscapedCharacter());
            } else {
                builder.append(current);
            }
        }
        throw error("Unclosed string");
    }

    private char readEscapedCharacter() {
        if (isAtEnd()) {
            throw error("Unclosed escape sequence");
        }

        char escaped = next();
        return switch (escaped) {
            case '"' -> '"';
            case '\\' -> '\\';
            case '/' -> '/';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'u' -> readUnicodeEscape();
            default -> throw error("Unknown escape sequence \\" + escaped);
        };
    }

    private char readUnicodeEscape() {
        if (cursor + 4 > text.length()) {
            throw error("Incomplete unicode escape");
        }

        int code = 0;
        for (int i = 0; i < 4; i++) {
            char hex = next();
            int digit = Character.digit(hex, 16);
            if (digit < 0) {
                throw error("Invalid unicode escape");
            }
            code = code * 16 + digit;
        }
        return (char) code;
    }

    private Object readLiteral(String literal, Object value) {
        if (!text.startsWith(literal, cursor)) {
            throw error("Expected " + literal);
        }
        cursor += literal.length();
        return value;
    }

    private Number readNumber() {
        int start = cursor;
        if (tryConsume('-') && isAtEnd()) {
            throw error("Invalid number");
        }

        if (tryConsume('0')) {
            if (!isAtEnd() && Character.isDigit(peek())) {
                throw error("Leading zero is not allowed");
            }
        } else {
            readDigits();
        }

        boolean floatingPoint = false;
        if (tryConsume('.')) {
            floatingPoint = true;
            readDigits();
        }

        if (!isAtEnd() && (peek() == 'e' || peek() == 'E')) {
            floatingPoint = true;
            next();
            if (!isAtEnd() && (peek() == '+' || peek() == '-')) {
                next();
            }
            readDigits();
        }

        String number = text.substring(start, cursor);
        try {
            if (floatingPoint) {
                double parsed = Double.parseDouble(number);
                if (!Double.isFinite(parsed)) {
                    throw error("Number is too large");
                }
                return parsed;
            }

            long parsed = Long.parseLong(number);
            if (parsed >= Integer.MIN_VALUE && parsed <= Integer.MAX_VALUE) {
                return (int) parsed;
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new JsonException("Invalid number at position " + start + ": " + number, exception);
        }
    }

    private void readDigits() {
        int start = cursor;
        while (!isAtEnd() && Character.isDigit(peek())) {
            next();
        }
        if (start == cursor) {
            throw error("Expected digit");
        }
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char current = peek();
            if (current == ' ' || current == '\n' || current == '\r' || current == '\t') {
                cursor++;
            } else {
                return;
            }
        }
    }

    private boolean tryConsume(char expected) {
        if (!isAtEnd() && peek() == expected) {
            cursor++;
            return true;
        }
        return false;
    }

    private void expect(char expected) {
        if (isAtEnd() || next() != expected) {
            throw error("Expected '" + expected + "'");
        }
    }

    private char peek() {
        return text.charAt(cursor);
    }

    private char next() {
        return text.charAt(cursor++);
    }

    private boolean isAtEnd() {
        return cursor >= text.length();
    }

    private JsonException error(String message) {
        return new JsonException(message + " at position " + cursor);
    }
}
