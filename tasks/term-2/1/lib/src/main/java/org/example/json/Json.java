package org.example.json;

import java.util.Map;

public final class Json {

    private Json() {}

    public static Object parse(String json) {
        return new JsonParser(new JsonTokenizer(json)).parseValue();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(String json) {
        Object result = parse(json);
        if (!(result instanceof Map)) {
            throw new JsonException("Expected JSON object but got: " + (result == null ? "null" : result.getClass().getSimpleName()));
        }
        return (Map<String, Object>) result;
    }

    public static <T> T parse(String json, Class<T> clazz) {
        return new JsonParser(new JsonTokenizer(json)).parseAs(clazz);
    }

    public static String toJson(Object obj) {
        return new JsonSerializer().serialize(obj);
    }
}
