package jsonparser;

import java.util.*;

public class JsonParser {

    private final String input;
    private int pos;

    public JsonParser(String input) {
        this.input = input.trim();
        this.pos = 0;
    }

    public Object parse() {
        Object value = parseValue();
        skipWhitespace();
        if (pos != input.length()) {
            throw new JsonException("Unexpected characters after JSON at position " + pos);
        }
        return value;
    }

    private Object parseValue() {
        skipWhitespace();
        if (pos >= input.length()) {
            throw new JsonException("Unexpected end of input");
        }

        char ch = input.charAt(pos);

        if (ch == '"')
            return parseString();
        if (ch == '{')
            return parseObject();
        if (ch == '[')
            return parseArray();
        if (ch == 't' || ch == 'f')
            return parseBoolean();
        if (ch == 'n')
            return parseNull();
        if (ch == '-' || Character.isDigit(ch))
            return parseNumber();

        throw new JsonException("Unexpected character '" + ch + "' at position " + pos);
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char ch = input.charAt(pos++);
            if (ch == '"')
                return sb.toString();
            if (ch == '\\') {
                if (pos >= input.length())
                    break;
                char esc = input.charAt(pos++);
                switch (esc) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'u':
                        String hex = input.substring(pos, pos + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                        break;
                    default:
                        throw new JsonException("Invalid escape sequence: \\" + esc);
                }
            } else {
                sb.append(ch);
            }
        }
        throw new JsonException("Unterminated string");
    }

    private Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWhitespace();

        if (peek() == '}') {
            pos++;
            return map;
        }

        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object value = parseValue();
            map.put(key, value);

            skipWhitespace();
            char next = input.charAt(pos++);
            if (next == '}')
                break;
            if (next != ',')
                throw new JsonException("Expected ',' or '}' in object at position " + (pos - 1));
        }
        return map;
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> list = new ArrayList<>();
        skipWhitespace();

        if (peek() == ']') {
            pos++;
            return list;
        }

        while (true) {
            list.add(parseValue());
            skipWhitespace();
            char next = input.charAt(pos++);
            if (next == ']')
                break;
            if (next != ',')
                throw new JsonException("Expected ',' or ']' in array at position " + (pos - 1));
        }
        return list;
    }

    private Boolean parseBoolean() {
        if (input.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        }
        if (input.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        }
        throw new JsonException("Invalid boolean at position " + pos);
    }

    private Object parseNull() {
        if (input.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw new JsonException("Invalid null at position " + pos);
    }

    private Number parseNumber() {
        int start = pos;
        if (peek() == '-')
            pos++;
        while (pos < input.length() && Character.isDigit(input.charAt(pos)))
            pos++;

        boolean isDecimal = false;
        if (pos < input.length() && input.charAt(pos) == '.') {
            isDecimal = true;
            pos++;
            while (pos < input.length() && Character.isDigit(input.charAt(pos)))
                pos++;
        }
        if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
            isDecimal = true;
            pos++;
            if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-'))
                pos++;
            while (pos < input.length() && Character.isDigit(input.charAt(pos)))
                pos++;
        }

        String numStr = input.substring(start, pos);
        try {
            if (isDecimal)
                return Double.parseDouble(numStr);
            return Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            try {
                return Long.parseLong(numStr);
            } catch (NumberFormatException e2) {
                return Double.parseDouble(numStr);
            }
        }
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos)))
            pos++;
    }

    private char peek() {
        skipWhitespace();
        return input.charAt(pos);
    }

    private void expect(char ch) {
        skipWhitespace();
        if (pos >= input.length() || input.charAt(pos) != ch) {
            throw new JsonException("Expected '" + ch + "' at position " + pos
                    + " but got '" + (pos < input.length() ? input.charAt(pos) : "EOF") + "'");
        }
        pos++;
    }
}
