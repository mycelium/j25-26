package json.internal;

import json.exceptions.JsonParseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class JsonParser {
    private final String source;
    private int index;

    public JsonParser(String source) {
        this.source = Objects.requireNonNull(source, "json must not be null");
    }

    public Object parse() {
        skipWhitespace();
        Object result = parseValue();
        skipWhitespace();
        if (!isEnd()) {
            throw error("Unexpected trailing data");
        }
        return result;
    }

    private Object parseValue() {
        if (isEnd()) {
            throw error("Unexpected end of input");
        }

        char ch = current();
        return switch (ch) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't' -> parseLiteral("true", Boolean.TRUE);
            case 'f' -> parseLiteral("false", Boolean.FALSE);
            case 'n' -> parseLiteral("null", null);
            default -> {
                if (ch == '-' || isDigit(ch)) {
                    yield parseNumber();
                }
                throw error("Unexpected character '" + ch + "'");
            }
        };
    }

    private Map<String, Object> parseObject() {
        expect('{');
        skipWhitespace();
        Map<String, Object> result = new LinkedHashMap<>();
        if (tryConsume('}')) {
            return result;
        }

        while (true) {
            skipWhitespace();
            if (isEnd()) {
                throw error("Unexpected end of input in object");
            }
            if (current() != '"') {
                throw error("Object key must be a string");
            }
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            Object value = parseValue();
            result.put(key, value);
            skipWhitespace();
            if (tryConsume(',')) {
                skipWhitespace();
                continue;
            }
            if (tryConsume('}')) {
                break;
            }
            throw error("Expected ',' or '}' in object");
        }

        return result;
    }

    private List<Object> parseArray() {
        expect('[');
        skipWhitespace();
        List<Object> result = new ArrayList<>();
        if (tryConsume(']')) {
            return result;
        }

        while (true) {
            skipWhitespace();
            result.add(parseValue());
            skipWhitespace();
            if (tryConsume(',')) {
                skipWhitespace();
                continue;
            }
            if (tryConsume(']')) {
                break;
            }
            throw error("Expected ',' or ']' in array");
        }

        return result;
    }

    private String parseString() {
        expect('"');
        StringBuilder builder = new StringBuilder();

        while (!isEnd()) {
            char ch = source.charAt(index++);
            if (ch == '"') {
                return builder.toString();
            }
            if (ch == '\\') {
                if (isEnd()) {
                    throw error("Unexpected end of input in escape sequence");
                }
                builder.append(parseEscape());
                continue;
            }
            if (ch < 0x20) {
                throw error("Control characters are not allowed in strings");
            }
            builder.append(ch);
        }

        throw error("Unterminated string literal");
    }

    private char parseEscape() {
        char escapeChar = source.charAt(index++);
        return switch (escapeChar) {
            case '"', '\\', '/' -> escapeChar;
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'u' -> parseUnicodeEscape();
            default -> throw error("Invalid escape sequence '\\" + escapeChar + "'");
        };
    }

    private char parseUnicodeEscape() {
        if (index + 4 > source.length()) {
            throw error("Invalid unicode escape sequence");
        }
        int codePoint = 0;
        for (int i = 0; i < 4; i++) {
            char ch = source.charAt(index++);
            int digit = Character.digit(ch, 16);
            if (digit < 0) {
                throw error("Invalid hex digit in unicode escape: '" + ch + "'");
            }
            codePoint = (codePoint << 4) + digit;
        }
        return (char) codePoint;
    }

    private Object parseNumber() {
        int start = index;

        if (tryConsume('-') && isEnd()) {
            throw error("Invalid number");
        }

        parseIntegerPart();
        parseFractionPart();
        parseExponentPart();

        String rawNumber = source.substring(start, index);
        try {
            return new BigDecimal(rawNumber);
        } catch (NumberFormatException ex) {
            throw new JsonParseException("Invalid number '" + rawNumber + "' at position " + start, ex);
        }
    }

    private void parseIntegerPart() {
        if (isEnd()) {
            throw error("Invalid number");
        }

        if (current() == '0') {
            index++;
            return;
        }

        if (!isDigitOneToNine(current())) {
            throw error("Invalid number");
        }

        while (!isEnd() && isDigit(current())) {
            index++;
        }
    }

    private void parseFractionPart() {
        if (isEnd() || current() != '.') {
            return;
        }
        index++;
        if (isEnd() || !isDigit(current())) {
            throw error("Invalid number fraction");
        }
        while (!isEnd() && isDigit(current())) {
            index++;
        }
    }

    private void parseExponentPart() {
        if (isEnd()) {
            return;
        }
        char ch = current();
        if (ch != 'e' && ch != 'E') {
            return;
        }

        index++;
        if (!isEnd() && (current() == '+' || current() == '-')) {
            index++;
        }
        if (isEnd() || !isDigit(current())) {
            throw error("Invalid number exponent");
        }
        while (!isEnd() && isDigit(current())) {
            index++;
        }
    }

    private Object parseLiteral(String literal, Object value) {
        for (int i = 0; i < literal.length(); i++) {
            if (isEnd() || source.charAt(index) != literal.charAt(i)) {
                throw error("Expected '" + literal + "'");
            }
            index++;
        }
        return value;
    }

    private void expect(char expected) {
        if (isEnd() || current() != expected) {
            throw error("Expected '" + expected + "'");
        }
        index++;
    }

    private boolean tryConsume(char ch) {
        if (!isEnd() && current() == ch) {
            index++;
            return true;
        }
        return false;
    }

    private void skipWhitespace() {
        while (!isEnd()) {
            char ch = current();
            if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') {
                index++;
            } else {
                return;
            }
        }
    }

    private char current() {
        return source.charAt(index);
    }

    private boolean isEnd() {
        return index >= source.length();
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isDigitOneToNine(char ch) {
        return ch >= '1' && ch <= '9';
    }

    private JsonParseException error(String message) {
        return new JsonParseException(message + " at position " + index);
    }
}
