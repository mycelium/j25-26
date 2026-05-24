package json;

import java.util.*;

class JsonReader {
    private final char[] chars;
    private int cursor;

    private JsonReader(String input) {
        this.chars = input.toCharArray();
        this.cursor = 0;
    }

    static Object read(String json) {
        if (json == null) throw new JsonLib.JsonException("Input is null");
        return new JsonReader(json).parse();
    }

    private Object parse() {
        skipSpaces();
        Object value = readValue();
        skipSpaces();
        if (cursor < chars.length) {
            throw new JsonLib.JsonException("Unexpected trailing content at pos " + cursor);
        }
        return value;
    }

    private Object readValue() {
        skipSpaces();
        char c = current();
        return switch (c) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't', 'f' -> readBool();
            case 'n' -> readNull();
            case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> readNum();
            default -> throw new JsonLib.JsonException("Unknown token at " + cursor + ": " + c);
        };
    }

    private Map<String, Object> readObject() {
        expect('{');
        Map<String, Object> obj = new LinkedHashMap<>();
        skipSpaces();
        if (current() == '}') { cursor++; return obj; }

        while (true) {
            skipSpaces();
            String key = readString();
            skipSpaces();
            expect(':');
            Object val = readValue();
            obj.put(key, val);
            skipSpaces();
            char sep = current();
            if (sep == '}') { cursor++; break; }
            if (sep == ',') { cursor++; continue; }
            throw new JsonLib.JsonException("Expected ',' or '}' at " + cursor);
        }
        return obj;
    }

    private List<Object> readArray() {
        expect('[');
        List<Object> arr = new ArrayList<>();
        skipSpaces();
        if (current() == ']') { cursor++; return arr; }

        while (true) {
            arr.add(readValue());
            skipSpaces();
            char sep = current();
            if (sep == ']') { cursor++; break; }
            if (sep == ',') { cursor++; continue; }
            throw new JsonLib.JsonException("Expected ',' or ']' at " + cursor);
        }
        return arr;
    }

    private String readString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (cursor < chars.length) {
            char c = chars[cursor++];
            if (c == '"') return sb.toString();
            if (c == '\\') sb.append(unescape());
            else sb.append(c);
        }
        throw new JsonLib.JsonException("Unterminated string");
    }

    private char unescape() {
        if (cursor >= chars.length) throw new JsonLib.JsonException("Unexpected end in escape");
        char esc = chars[cursor++];
        return switch (esc) {
            case '"' -> '"';
            case '\\' -> '\\';
            case '/' -> '/';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'u' -> readUnicode();
            default -> throw new JsonLib.JsonException("Invalid escape: \\" + esc);
        };
    }

    private char readUnicode() {
        if (cursor + 4 > chars.length) throw new JsonLib.JsonException("Incomplete unicode escape");
        String hex = new String(chars, cursor, 4);
        cursor += 4;
        return (char) Integer.parseInt(hex, 16);
    }

    private Boolean readBool() {
        if (match("true")) return true;
        if (match("false")) return false;
        throw new JsonLib.JsonException("Invalid boolean at " + cursor);
    }

    private Object readNull() {
        if (match("null")) return null;
        throw new JsonLib.JsonException("Invalid null at " + cursor);
    }

    private Number readNum() {
        int start = cursor;
        if (current() == '-') cursor++;
        while (Character.isDigit(current())) cursor++;
        if (current() == '.') {
            cursor++;
            while (Character.isDigit(current())) cursor++;
        }
        if (current() == 'e' || current() == 'E') {
            cursor++;
            if (current() == '+' || current() == '-') cursor++;
            while (Character.isDigit(current())) cursor++;
        }
        String num = new String(chars, start, cursor - start);
        try {
            if (num.contains(".") || num.toLowerCase().contains("e")) {
                return Double.parseDouble(num);
            }
            long l = Long.parseLong(num);
            return (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) ? (int) l : l;
        } catch (NumberFormatException e) {
            throw new JsonLib.JsonException("Bad number: " + num, e);
        }
    }

    private boolean match(String word) {
        if (cursor + word.length() > chars.length) return false;
        for (int i = 0; i < word.length(); i++) {
            if (chars[cursor + i] != word.charAt(i)) return false;
        }
        cursor += word.length();
        return true;
    }

    private char current() {
        if (cursor >= chars.length) return 0;
        return chars[cursor];
    }

    private void expect(char c) {
        if (current() != c) throw new JsonLib.JsonException("Expected '" + c + "' at " + cursor);
        cursor++;
    }

    private void skipSpaces() {
        while (cursor < chars.length && Character.isWhitespace(chars[cursor])) cursor++;
    }
}