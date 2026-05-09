package ru.example.json;

import java.util.*;

public class JsonParser {

    private final JsonTokenizer tokenizer;

    public JsonParser(String json) {
        this.tokenizer = new JsonTokenizer(json);
    }

    public Object parse() {
        tokenizer.skipWhitespace();
        char c = tokenizer.peek();

        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return tokenizer.readString();
        if (Character.isDigit(c)) return Double.parseDouble(tokenizer.readNumber());

        String literal = tokenizer.readLiteral();
        return switch (literal) {
            case "true" -> true;
            case "false" -> false;
            case "null" -> null;
            default -> throw new RuntimeException("Unknown token: " + literal);
        };
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new HashMap<>();
        tokenizer.next();

        while (true) {
            tokenizer.skipWhitespace();
            if (tokenizer.peek() == '}') {
                tokenizer.next();
                break;
            }

            String key = tokenizer.readString();

            tokenizer.skipWhitespace();
            tokenizer.next(); // :

            Object value = parse();
            map.put(key, value);

            tokenizer.skipWhitespace();
            char c = tokenizer.next();
            if (c == '}') break;
        }
        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        tokenizer.next(); // [

        while (true) {
            tokenizer.skipWhitespace();
            if (tokenizer.peek() == ']') {
                tokenizer.next();
                break;
            }

            list.add(parse());

            tokenizer.skipWhitespace();
            char c = tokenizer.next();
            if (c == ']') break;
        }
        return list;
    }
}
