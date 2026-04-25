package jsonlib;

import java.util.*;

public class JsonParser {
    private String input;
    private int pos;
    private int length;

    public static Object parse(String json) {
        JsonParser parser = new JsonParser(json);
        Object result = parser.parseValue();
        parser.skipWhitespace();

        if (parser.pos != parser.length) {
            throw new RuntimeException("Extra data after JSON at position " + parser.pos);
        }

        return result;
    }

    private JsonParser(String input) {
        this.input = input;
        this.pos = 0;
        this.length = input.length();
    }

    private void skipWhitespace() {
        while (pos < length && Character.isWhitespace(input.charAt(pos))) pos++;
    }

    private char peek() {
        return pos < length ? input.charAt(pos) : '\0';
    }

    private char next() {
        if (pos >= length) throw new RuntimeException("Unexpected end");
        return input.charAt(pos++);
    }

    private void expect(char c) {
        if (next() != c) throw new RuntimeException("Expected '" + c + "'");
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
                throw new RuntimeException("Invalid char: " + c);
        }
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();

        if (peek() == '}') {
            next();
            return map;
        }

        while (true) {
        	skipWhitespace(); 

            if (peek() != '"') {
                throw new RuntimeException(
                    "Expected string key but found: " + peek()
                );
            }

            String key = parseString();
            skipWhitespace();
            expect(':');

            Object value = parseValue();
            map.put(key, value);

            skipWhitespace();
            char c = peek();

            if (c == '}') {
                next();
                break;
            } else if (c == ',') {
                next();
            } else {
                throw new RuntimeException("Expected , or }");
            }
        }

        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        expect('[');
        skipWhitespace();

        if (peek() == ']') {
            next();
            return list;
        }

        while (true) {
            list.add(parseValue());
            skipWhitespace();

            char c = peek();
            if (c == ']') {
                next();
                break;
            } else if (c == ',') {
                next();
            } else {
                throw new RuntimeException("Expected , or ]");
            }
        }

        return list;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();

        while (true) {
            char c = next();
            if (c == '"') break;

            if (c == '\\') {
                char esc = next();
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    default: throw new RuntimeException("Bad escape");
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Boolean parseBoolean() {
        if (input.startsWith("true", pos)) {
            pos += 4;
            return true;
        } else if (input.startsWith("false", pos)) {
            pos += 5;
            return false;
        }
        throw new RuntimeException("Invalid boolean");
    }

    private Object parseNull() {
        if (input.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw new RuntimeException("Invalid null");
    }

    private Number parseNumber() {
        int start = pos;

        if (peek() == '-') next();
        while (Character.isDigit(peek())) next();

        if (peek() == '.') {
            next();
            while (Character.isDigit(peek())) next();
        }

        String num = input.substring(start, pos);

        return num.contains(".") ? Double.parseDouble(num) : Integer.parseInt(num);
    }
}