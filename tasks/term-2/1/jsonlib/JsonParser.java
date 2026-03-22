package jsonlib;

import java.util.*;

class JsonParser {
    private String input;
    private int pos;
    private int length;

    static Object parse(String json) {
        JsonParser parser = new JsonParser(json);
        return parser.parseValue();
    }

    private JsonParser(String input) {
        this.input = input;
        this.pos = 0;
        this.length = input.length();
    }

    private void skipWhitespace() {
        while (pos < length && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        if (pos >= length) return 0;
        return input.charAt(pos);
    }

    private char next() {
        if (pos >= length) throw new RuntimeException("Unexpected end of input");
        return input.charAt(pos++);
    }

    private void expect(char c) {
        if (next() != c) {
            throw new RuntimeException("Expected '" + c + "' at position: " + (this.pos + 1));
        }
    }

    private Object parseValue() {
        skipWhitespace();
        char c = peek();
        if (c == '{') {
            return parseObject();
        } else if (c == '[') {
            return parseArray();
        } else if (c == '"') {
            return parseString();
        } else if (c == 't' || c == 'f') {
            return parseBoolean();
        } else if (c == 'n') {
            return parseNull();
        } else if (c == '-' || (c >= '0' && c <= '9')) {
            return parseNumber();
        } else {
            throw new RuntimeException("Unexpected character: '" + c + "' at position: " + (this.pos));
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
                continue;
            } else {
                throw new RuntimeException("Expected ',' or '}'");
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
                continue;
            } else {
                throw new RuntimeException("Expected ',' or ']'");
            }
        }
        return list;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                char esc = next();
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: throw new RuntimeException("Invalid escape sequence: \\" + esc);
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
            return Boolean.TRUE;
        } else if (input.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        } else {
            throw new RuntimeException("Expected boolean");
        }
    }

    private Object parseNull() {
        if (input.startsWith("null", pos)) {
            pos += 4;
            return null;
        } else {
            throw new RuntimeException("Expected null");
        }
    }

    private Number parseNumber() {
        int start = pos;
        if (peek() == '-') {
            next();
        }
        while (pos < length && Character.isDigit(peek())) {
            next();
        }
        if (peek() == '.') {
            next();
            while (pos < length && Character.isDigit(peek())) {
                next();
            }
        }
        if (peek() == 'e' || peek() == 'E') {
            next();
            if (peek() == '+' || peek() == '-') {
                next();
            }
            while (pos < length && Character.isDigit(peek())) {
                next();
            }
        }
        String numStr = input.substring(start, pos);
        try {
            if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                return Double.parseDouble(numStr);
            } else {
                long l = Long.parseLong(numStr);
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                    return (int) l;
                } else {
                    return l;
                }
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number: " + numStr, e);
        }
    }
}