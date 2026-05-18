package jsonlib;

import java.util.*;


class JsonParser {

    private final String input;
    private int pos;
    private final int length;

    
    public static Object parse(String json) {
        if (json == null) throw new IllegalArgumentException("JSON input must not be null");
        JsonParser parser = new JsonParser(json.trim());
        Object result = parser.parseValue();
        parser.skipWhitespace();
        if (parser.pos != parser.length) {
            throw new JsonParseException(
                "Extra data after JSON value at position " + parser.pos
            );
        }
        return result;
    }

    private JsonParser(String input) {
        this.input  = input;
        this.pos    = 0;
        this.length = input.length();
    }

    
    private void skipWhitespace() {
        while (pos < length && Character.isWhitespace(input.charAt(pos))) pos++;
    }

    private char peek() {
        return pos < length ? input.charAt(pos) : '\0';
    }

    private char next() {
        if (pos >= length) throw new JsonParseException("Unexpected end of input");
        return input.charAt(pos++);
    }

    private void expect(char c) {
        char got = next();
        if (got != c) throw new JsonParseException(
            "Expected '" + c + "' but got '" + got + "' at position " + (pos - 1)
        );
    }

     private Object parseValue() {
        skipWhitespace();
        char c = peek();
        switch (c) {
            case '{': return parseObject();
            case '[': return parseArray();
            case '"': return parseString();
            case 't': case 'f': return parseBoolean();
            case 'n': return parseNull();
            default:
                if (c == '-' || Character.isDigit(c)) return parseNumber();
                throw new JsonParseException("Unexpected character '" + c + "' at position " + pos);
        }
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();

        if (peek() == '}') { next(); return map; }

        while (true) {
            skipWhitespace();
            if (peek() != '"') throw new JsonParseException(
                "Expected string key but found '" + peek() + "' at position " + pos
            );
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object value = parseValue();
            map.put(key, value);
            skipWhitespace();

            char c = peek();
            if      (c == '}') { next(); break; }
            else if (c == ',') { next(); }
            else throw new JsonParseException("Expected ',' or '}' at position " + pos);
        }
        return map;
    }

    // --------------------------------------------------------- Array

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        expect('[');
        skipWhitespace();

        if (peek() == ']') { next(); return list; }

        while (true) {
            list.add(parseValue());
            skipWhitespace();

            char c = peek();
            if      (c == ']') { next(); break; }
            else if (c == ',') { next(); }
            else throw new JsonParseException("Expected ',' or ']' at position " + pos);
        }
        return list;
    }

    
    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();

        while (true) {
            if (pos >= length) throw new JsonParseException("Unterminated string");
            char c = next();
            if (c == '"') break;

            if (c == '\\') {
                char esc = next();
                switch (esc) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/');  break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'u':
                        if (pos + 4 > length) throw new JsonParseException("Incomplete \\uXXXX escape");
                        String hex = input.substring(pos, pos + 4);
                        pos += 4;
                        try {
                            sb.append((char) Integer.parseInt(hex, 16));
                        } catch (NumberFormatException e) {
                            throw new JsonParseException("Invalid \\u escape: \\u" + hex);
                        }
                        break;
                    default:
                        throw new JsonParseException("Unknown escape sequence: \\" + esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

   private Boolean parseBoolean() {
        if (input.startsWith("true", pos))  { pos += 4; return Boolean.TRUE;  }
        if (input.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
        throw new JsonParseException("Invalid literal at position " + pos);
    }

    
    private Object parseNull() {
        if (input.startsWith("null", pos)) { pos += 4; return null; }
        throw new JsonParseException("Invalid literal at position " + pos);
    }

    


    private Number parseNumber() {
        int start = pos;
        boolean isFloating = false;

        if (peek() == '-') next();

        if (!Character.isDigit(peek()))
            throw new JsonParseException("Expected digit at position " + pos);

        while (Character.isDigit(peek())) next();

        if (peek() == '.') {
            isFloating = true;
            next();
            if (!Character.isDigit(peek()))
                throw new JsonParseException("Expected digit after '.' at position " + pos);
            while (Character.isDigit(peek())) next();
        }

        if (peek() == 'e' || peek() == 'E') {
            isFloating = true;
            next();
            if (peek() == '+' || peek() == '-') next();
            if (!Character.isDigit(peek()))
                throw new JsonParseException("Expected digit in exponent at position " + pos);
            while (Character.isDigit(peek())) next();
        }

        String raw = input.substring(start, pos);

        if (isFloating) {
            return Double.parseDouble(raw);
        }

        try {
            long val = Long.parseLong(raw);
            if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                return (int) val;
            }
            return val;
        } catch (NumberFormatException e) {
            // Overflow even for Long — fall back to Double
            return Double.parseDouble(raw);
        }
    }
}
