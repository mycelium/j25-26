package jsonengine;

import java.util.*;

class ValueParser {
    private final String src;
    private int index = 0;

    private ValueParser(String src) {
        this.src = src;
    }

    static Object parse(String content) {
        return new ValueParser(content).extractValue();
    }

    private Object extractValue() {
        trim();
        char current = current();
        return switch (current) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't', 'f' -> parseLiteral();
            case 'n' -> parseNull();
            default -> {
                if (Character.isDigit(current) || current == '-') yield parseNumber();
                throw new JsonException("Unexpected symbol '" + current + "' at " + index);
            }
        };
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> container = new LinkedHashMap<>();
        match('{');
        trim();
        if (current() == '}') {
            match('}');
            return container;
        }
        while (true) {
            trim();
            String key = parseString();
            trim();
            match(':');
            container.put(key, extractValue());
            trim();
            char next = current();
            if (next == '}') {
                match('}');
                break;
            }
            match(',');
        }
        return container;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        match('[');
        trim();
        if (current() == ']') {
            match(']');
            return list;
        }
        while (true) {
            list.add(extractValue());
            trim();
            char next = current();
            if (next == ']') {
                match(']');
                break;
            }
            match(',');
        }
        return list;
    }

    private String parseString() {
        match('"');
        StringBuilder res = new StringBuilder();
        while (true) {
            char c = move();
            if (c == '"') break;
            if (c == '\\') {
                char esc = move();
                switch (esc) {
                    case '"' -> res.append('"');
                    case '\\' -> res.append('\\');
                    case '/' -> res.append('/');
                    case 'b' -> res.append('\b');
                    case 'f' -> res.append('\f');
                    case 'n' -> res.append('\n');
                    case 'r' -> res.append('\r');
                    case 't' -> res.append('\t');
                    default -> throw new JsonException("Unknown escape: \\" + esc);
                }
            } else res.append(c);
        }
        return res.toString();
    }

    private Object parseLiteral() {
        if (src.startsWith("true", index)) { index += 4; return Boolean.TRUE; }
        if (src.startsWith("false", index)) { index += 5; return Boolean.FALSE; }
        throw new JsonException("Invalid boolean at " + index);
    }

    private Object parseNull() {
        if (src.startsWith("null", index)) { index += 4; return null; }
        throw new JsonException("Invalid null at " + index);
    }

    private Number parseNumber() {
        int start = index;
        while (index < src.length() && "0123456789.eE+-".indexOf(src.charAt(index)) != -1) {
            index++;
        }
        String val = src.substring(start, index);
        try {
            if (val.contains(".") || val.toLowerCase().contains("e")) return Double.parseDouble(val);
            long l = Long.parseLong(val);
            return (l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) ? (int) l : l;
        } catch (NumberFormatException e) {
            throw new JsonException("Bad number format: " + val);
        }
    }

    private void trim() {
        while (index < src.length() && Character.isWhitespace(src.charAt(index))) index++;
    }

    private char current() { return index < src.length() ? src.charAt(index) : 0; }

    private char move() {
        if (index >= src.length()) throw new JsonException("Unexpected EOF");
        return src.charAt(index++);
    }

    private void match(char expected) {
        if (move() != expected) throw new JsonException("Expected '" + expected + "' at " + (index - 1));
    }
}