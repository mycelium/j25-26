package org.example.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {

    private final JsonConfig config;

    private String json;
    private int index;

    public JsonParser() {
        this(new JsonConfig());
    }

    public JsonParser(JsonConfig config) {
        this.config = config;
    }

    public Object parse(String json) {
        if (json == null) {
            throw new IllegalArgumentException("JSON string must not be null");
        }

        this.json = json;
        this.index = 0;

        skipWhitespace();
        Object result = parseValue();
        skipWhitespace();

        if (index != this.json.length()) {
            throw new IllegalArgumentException("Unexpected trailing characters at position " + index);
        }

        return result;
    }

    private Object parseValue() {
        skipWhitespace();

        if (index >= json.length()) {
            throw new IllegalArgumentException("Unexpected end of JSON input");
        }

        char ch = json.charAt(index);

        if (ch == '{') return parseObject();
        if (ch == '[') return parseArray();
        if (ch == '"') return parseString();
        if (ch == 't') return parseTrue();
        if (ch == 'f') return parseFalse();
        if (ch == 'n') return parseNull();
        if (ch == '-' || Character.isDigit(ch)) return parseNumber();

        throw new IllegalArgumentException("Unexpected character '" + ch + "' at position " + index);
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> object = new LinkedHashMap<>();

        expect('{');
        skipWhitespace();

        if (peek() == '}') {
            index++;
            return object;
        }

        while (true) {
            skipWhitespace();

            if (peek() != '"') {
                throw new IllegalArgumentException("Expected string key at position " + index);
            }

            String key = parseString();

            skipWhitespace();
            expect(':');
            skipWhitespace();

            Object value = parseValue();
            object.put(key, value);

            skipWhitespace();

            char ch = peek();
            if (ch == ',') {
                index++;
                continue;
            } else if (ch == '}') {
                index++;
                break;
            } else {
                throw new IllegalArgumentException("Expected ',' or '}' at position " + index);
            }
        }

        return object;
    }

    private List<Object> parseArray() {
        List<Object> array = new ArrayList<>();

        expect('[');
        skipWhitespace();

        if (peek() == ']') {
            index++;
            return array;
        }

        while (true) {
            skipWhitespace();

            Object value = parseValue();
            array.add(value);

            skipWhitespace();

            char ch = peek();
            if (ch == ',') {
                index++;
                continue;
            } else if (ch == ']') {
                index++;
                break;
            } else {
                throw new IllegalArgumentException("Expected ',' or ']' at position " + index);
            }
        }

        return array;
    }

    private String parseString() {
        expect('"');

        StringBuilder sb = new StringBuilder();

        while (index < json.length()) {
            char ch = json.charAt(index++);

            if (ch == '"') {
                return sb.toString();
            }

            if (ch == '\\') {
                if (index >= json.length()) {
                    throw new IllegalArgumentException("Invalid escape sequence at end of input");
                }

                char escaped = json.charAt(index++);

                switch (escaped) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (index + 4 > json.length()) {
                            throw new IllegalArgumentException("Invalid unicode escape at position " + index);
                        }
                        String hex = json.substring(index, index + 4);
                        try {
                            char unicodeChar = (char) Integer.parseInt(hex, 16);
                            sb.append(unicodeChar);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid unicode escape \\u" + hex + " at position " + index);
                        }
                        index += 4;
                    }
                    default -> throw new IllegalArgumentException(
                            "Invalid escape character '\\" + escaped + "' at position " + (index - 1)
                    );
                }
            } else {
                sb.append(ch);
            }
        }

        throw new IllegalArgumentException("Unterminated string starting before position " + index);
    }

    private Object parseNumber() {
        int start = index;

        if (peek() == '-') {
            index++;
        }

        if (index >= json.length()) {
            throw new IllegalArgumentException("Invalid number at position " + start);
        }

        if (peek() == '0') {
            index++;
        } else if (Character.isDigit(peek())) {
            while (index < json.length() && Character.isDigit(peek())) {
                index++;
            }
        } else {
            throw new IllegalArgumentException("Invalid number at position " + start);
        }

        boolean isFractional = false;

        if (index < json.length() && peek() == '.') {
            isFractional = true;
            index++;

            if (index >= json.length() || !Character.isDigit(peek())) {
                throw new IllegalArgumentException("Invalid number format at position " + start);
            }

            while (index < json.length() && Character.isDigit(peek())) {
                index++;
            }
        }

        if (index < json.length() && (peek() == 'e' || peek() == 'E')) {
            isFractional = true;
            index++;

            if (index < json.length() && (peek() == '+' || peek() == '-')) {
                index++;
            }

            if (index >= json.length() || !Character.isDigit(peek())) {
                throw new IllegalArgumentException("Invalid exponent in number at position " + start);
            }

            while (index < json.length() && Character.isDigit(peek())) {
                index++;
            }
        }

        String numberText = json.substring(start, index);

        try {
            if (isFractional) {
                return Double.parseDouble(numberText);
            }

            long longValue = Long.parseLong(numberText);
            if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                return (int) longValue;
            }
            return longValue;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number '" + numberText + "' at position " + start);
        }
    }

    private Boolean parseTrue() {
        expectLiteral("true");
        return Boolean.TRUE;
    }

    private Boolean parseFalse() {
        expectLiteral("false");
        return Boolean.FALSE;
    }

    private Object parseNull() {
        expectLiteral("null");
        return null;
    }

    private void expectLiteral(String literal) {
        if (index + literal.length() > json.length()) {
            throw new IllegalArgumentException("Expected '" + literal + "' at position " + index);
        }

        String actual = json.substring(index, index + literal.length());
        if (!actual.equals(literal)) {
            throw new IllegalArgumentException("Expected '" + literal + "' at position " + index);
        }

        index += literal.length();
    }

    private void expect(char expected) {
        if (index >= json.length() || json.charAt(index) != expected) {
            throw new IllegalArgumentException("Expected '" + expected + "' at position " + index);
        }
        index++;
    }

    private char peek() {
        if (index >= json.length()) {
            return '\0';
        }
        return json.charAt(index);
    }

    private void skipWhitespace() {
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }
    }
}