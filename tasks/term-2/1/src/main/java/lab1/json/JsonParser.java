package lab1.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class JsonParser {
    private String json;
    private int index;

    public Object parse(String json) {
        if (json == null) {
            throw new JsonException("JSON string must not be null");
        }

        this.json = json;
        this.index = 0;

        skipWhitespace();
        Object result = parseValue();
        skipWhitespace();

        if (index != this.json.length()) {
            throw new JsonException("Unexpected trailing characters at position " + index);
        }

        return result;
    }

    private Object parseValue() {
        skipWhitespace();

        if (index >= json.length()) {
            throw new JsonException("Unexpected end of JSON");
        }

        char c = json.charAt(index);

        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') return parseNull();
        if (c == '-' || Character.isDigit(c)) return parseNumber();

        throw new JsonException("Unexpected character '" + c + "' at position " + index);
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();

        if (peek('}')) {
            index++;
            return map;
        }

        while (true) {
            skipWhitespace();

            if (!peek('"')) {
                throw new JsonException("Expected object key at position " + index);
            }

            String key = parseString();
            expect(':');
            Object value = parseValue();
            map.put(key, value);

            skipWhitespace();

            if (peek(',')) {
                index++;
                continue;
            }

            if (peek('}')) {
                index++;
                break;
            }

            throw new JsonException("Expected ',' or '}' at position " + index);
        }

        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        expect('[');
        skipWhitespace();

        if (peek(']')) {
            index++;
            return list;
        }

        while (true) {
            list.add(parseValue());
            skipWhitespace();

            if (peek(',')) {
                index++;
                continue;
            }

            if (peek(']')) {
                index++;
                break;
            }

            throw new JsonException("Expected ',' or ']' at position " + index);
        }

        return list;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();

        while (index < json.length()) {
            char c = json.charAt(index);

            if (c == '"') {
                index++;
                return sb.toString();
            }

            if (c == '\\') {
                index++;
                if (index >= json.length()) {
                    throw new JsonException("Unexpected end of JSON string");
                }

                char escaped = json.charAt(index);
                switch (escaped) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> sb.append(parseUnicodeEscape());
                    default -> throw new JsonException("Invalid escape sequence: \\" + escaped);
                }
            } else {
                if (c < 0x20) {
                    throw new JsonException("Unescaped control character in string at position " + index);
                }
                sb.append(c);
            }

            index++;
        }

        throw new JsonException("Unterminated string");
    }

    private char parseUnicodeEscape() {
        if (index + 4 >= json.length()) {
            throw new JsonException("Invalid unicode escape at position " + index);
        }

        String hex = json.substring(index + 1, index + 5);
        for (int i = 0; i < hex.length(); i++) {
            if (Character.digit(hex.charAt(i), 16) == -1) {
                throw new JsonException("Invalid unicode escape: \\u" + hex);
            }
        }

        index += 4;
        return (char) Integer.parseInt(hex, 16);
    }

    private Object parseNumber() {
        int start = index;

        if (peek('-')) {
            index++;
        }

        if (index >= json.length()) {
            throw new JsonException("Invalid number at position " + start);
        }

        if (peek('0')) {
            index++;
            if (index < json.length() && Character.isDigit(json.charAt(index))) {
                throw new JsonException("Leading zero is not allowed at position " + start);
            }
        } else if (isDigitOneToNine(currentChar())) {
            while (index < json.length() && Character.isDigit(json.charAt(index))) {
                index++;
            }
        } else {
            throw new JsonException("Invalid number at position " + start);
        }

        boolean floatingPoint = false;

        if (peek('.')) {
            floatingPoint = true;
            index++;

            if (index >= json.length() || !Character.isDigit(json.charAt(index))) {
                throw new JsonException("Expected digit after decimal point at position " + index);
            }

            while (index < json.length() && Character.isDigit(json.charAt(index))) {
                index++;
            }
        }

        if (peek('e') || peek('E')) {
            floatingPoint = true;
            index++;

            if (peek('+') || peek('-')) {
                index++;
            }

            if (index >= json.length() || !Character.isDigit(json.charAt(index))) {
                throw new JsonException("Expected digit in exponent at position " + index);
            }

            while (index < json.length() && Character.isDigit(json.charAt(index))) {
                index++;
            }
        }

        String number = json.substring(start, index);

        try {
            if (floatingPoint) {
                double parsed = Double.parseDouble(number);
                if (Double.isInfinite(parsed) || Double.isNaN(parsed)) {
                    throw new JsonException("Invalid floating point number: " + number);
                }
                return parsed;
            }

            try {
                return Integer.parseInt(number);
            } catch (NumberFormatException ignored) {
                return Long.parseLong(number);
            }
        } catch (NumberFormatException e) {
            throw new JsonException("Invalid number: " + number, e);
        }
    }

    private Boolean parseBoolean() {
        if (json.startsWith("true", index)) {
            index += 4;
            return true;
        }

        if (json.startsWith("false", index)) {
            index += 5;
            return false;
        }

        throw new JsonException("Invalid boolean value at position " + index);
    }

    private Object parseNull() {
        if (!json.startsWith("null", index)) {
            throw new JsonException("Invalid null value at position " + index);
        }

        index += 4;
        return null;
    }

    private void expect(char expected) {
        skipWhitespace();

        if (index >= json.length() || json.charAt(index) != expected) {
            throw new JsonException("Expected '" + expected + "' at position " + index);
        }

        index++;
    }

    private boolean peek(char expected) {
        return index < json.length() && json.charAt(index) == expected;
    }

    private char currentChar() {
        return index < json.length() ? json.charAt(index) : '\0';
    }

    private boolean isDigitOneToNine(char c) {
        return c >= '1' && c <= '9';
    }

    private void skipWhitespace() {
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }
    }
}
