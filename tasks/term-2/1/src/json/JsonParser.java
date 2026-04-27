package json;

import java.util.*;

public class JsonParser {

    private String json;
    private int index;

    public Object parse(String json) {
        this.json = json;
        this.index = 0;
        skipWhitespace();
        return parseValue();
    }

    private Object parseValue() {
        skipWhitespace();
        char c = current();

        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == '-' || Character.isDigit(c)) return parseNumber();

        if (json.startsWith("true", index)) {
            index += 4;
            return true;
        }

        if (json.startsWith("false", index)) {
            index += 5;
            return false;
        }

        if (json.startsWith("null", index)) {
            index += 4;
            return null;
        }

        throw new RuntimeException("Unexpected char: " + c);
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new HashMap<>();
        index++; // {

        while (true) {
            skipWhitespace();

            if (current() == '}') {
                index++;
                return map;
            }

            String key = parseString();

            skipWhitespace();
            expect(':');

            Object value = parseValue();
            map.put(key, value);

            skipWhitespace();

            if (current() == ',') {
                index++;
            } else if (current() == '}') {
                index++;
                return map;
            }
        }
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        index++; // [

        while (true) {
            skipWhitespace();

            if (current() == ']') {
                index++;
                return list;
            }

            list.add(parseValue());

            skipWhitespace();

            if (current() == ',') {
                index++;
            } else if (current() == ']') {
                index++;
                return list;
            }
        }
    }

    private String parseString() {
        index++; // "

        StringBuilder sb = new StringBuilder();

        while (true) {
            char c = current();

            if (c == '"') {
                index++;
                break;
            }

            if (c == '\\') {
                index++;
                char next = current();

                switch (next) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(next);
                }
                index++;
            } else {
                sb.append(c);
                index++;
            }
        }

        return sb.toString();
    }

    private Number parseNumber() {
        int start = index;

        if (current() == '-') index++;

        while (index < json.length() && Character.isDigit(current())) {
            index++;
        }

        if (index < json.length() && current() == '.') {
            index++;
            while (Character.isDigit(current())) {
                index++;
            }
            return Double.parseDouble(json.substring(start, index));
        }

        return Long.parseLong(json.substring(start, index));
    }

    private void skipWhitespace() {
        while (index < json.length() &&
                Character.isWhitespace(json.charAt(index))) {
            index++;
        }
    }

    private void expect(char expected) {
        if (current() != expected) {
            throw new RuntimeException("Expected '" + expected + "' but got '" + current() + "'");
        }
        index++;
    }

    private char current() {
        if (index >= json.length()) {
            throw new RuntimeException("Unexpected end of JSON");
        }
        return json.charAt(index);
    }
}