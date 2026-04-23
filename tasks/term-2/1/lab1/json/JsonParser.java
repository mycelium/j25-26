package lab1.json;

import java.util.*;

class JsonParser {

    private String json;
    private int index;

    public Object parse(String json) {
        this.json = json.trim();
        this.index = 0;
        return parseValue();
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

        throw new JsonException("Unexpected character: " + c);
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new HashMap<>();
        index++; // {

        while (true) {
            skipWhitespace();

            if (json.charAt(index) == '}') {
                index++;
                break;
            }

            String key = parseString();

            skipWhitespace();
            index++; // :

            Object value = parseValue();
            map.put(key, value);

            skipWhitespace();

            if (json.charAt(index) == ',') {
                index++;
                continue;
            }

            if (json.charAt(index) == '}') {
                index++;
                break;
            }
        }

        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        index++; // [

        while (true) {
            skipWhitespace();

            if (json.charAt(index) == ']') {
                index++;
                break;
            }

            list.add(parseValue());

            skipWhitespace();

            if (json.charAt(index) == ',') {
                index++;
                continue;
            }

            if (json.charAt(index) == ']') {
                index++;
                break;
            }
        }

        return list;
    }

    private String parseString() {
        index++; // "
        StringBuilder sb = new StringBuilder();

        while (json.charAt(index) != '"') {
            sb.append(json.charAt(index));
            index++;
        }

        index++; // "
        return sb.toString();
    }

    private Object parseNumber() {
        int start = index;

        while (index < json.length() &&
                (Character.isDigit(json.charAt(index)) || json.charAt(index) == '.' || json.charAt(index) == '-')) {
            index++;
        }

        String number = json.substring(start, index);

        if (number.contains(".")) {
            return Double.parseDouble(number);
        } else {
            return Integer.parseInt(number);
        }
    }

    private Boolean parseBoolean() {
        if (json.startsWith("true", index)) {
            index += 4;
            return true;
        } else {
            index += 5;
            return false;
        }
    }

    private Object parseNull() {
        index += 4;
        return null;
    }

    private void skipWhitespace() {
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }
    }
}