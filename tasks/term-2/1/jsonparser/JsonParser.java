package jsonparser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class JsonParser {
    private final JsonTokenizer t;

    JsonParser(String json) {
        this.t = new JsonTokenizer(json);
    }

    Object parse() {
        Object result = parseValue();
        if (t.type() != TokenType.EOF)
            throw new JsonException("Unexpected token after JSON value: " + t.type());
        return result;
    }

    private Object parseValue() {
        return switch (t.type()) {
            case BEGIN_OBJECT -> parseObject();
            case BEGIN_ARRAY  -> parseArray();
            case STRING  -> { String v = t.value(); t.advance(); yield v; }
            case NUMBER  -> { Object n = parseNumber(t.value()); t.advance(); yield n; }
            case BOOLEAN -> { boolean b = "true".equals(t.value()); t.advance(); yield b; }
            case NULL    -> { t.advance(); yield null; }
            default -> throw new JsonException("Unexpected token: " + t.type());
        };
    }

    private Map<String, Object> parseObject() {
        t.advance(); // consume '{'
        Map<String, Object> map = new LinkedHashMap<>();
        if (t.type() == TokenType.END_OBJECT) { t.advance(); return map; }

        while (true) {
            if (t.type() != TokenType.STRING)
                throw new JsonException("Expected object key (string), got: " + t.type());
            String key = t.value();
            t.advance();

            if (t.type() != TokenType.COLON)
                throw new JsonException("Expected ':', got: " + t.type());
            t.advance();

            map.put(key, parseValue());

            if (t.type() == TokenType.END_OBJECT) { t.advance(); return map; }
            if (t.type() != TokenType.COMMA)
                throw new JsonException("Expected ',' or '}', got: " + t.type());
            t.advance();
        }
    }

    private List<Object> parseArray() {
        t.advance(); // consume '['
        List<Object> list = new ArrayList<>();
        if (t.type() == TokenType.END_ARRAY) { t.advance(); return list; }

        while (true) {
            list.add(parseValue());

            if (t.type() == TokenType.END_ARRAY) { t.advance(); return list; }
            if (t.type() != TokenType.COMMA)
                throw new JsonException("Expected ',' or ']', got: " + t.type());
            t.advance();
        }
    }

    private Object parseNumber(String s) {
        if (s.contains(".") || s.contains("e") || s.contains("E"))
            return Double.parseDouble(s);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return Long.parseLong(s);
        }
    }
}
