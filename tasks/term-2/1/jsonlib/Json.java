package jsonlib;

import java.util.Map;


public final class Json {

    private Json() {}


    public static <T> T fromJson(String json, Class<T> targetClass) {
        return JsonMapper.map(JsonParser.parse(json), targetClass);
    }


    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJsonToMap(String json) {
        Object root = JsonParser.parse(json);
        if (!(root instanceof Map))
            throw new RuntimeException("Root JSON element is not an object");
        return (Map<String, Object>) root;
    }

    public static String toJson(Object value) {
        return new JsonSerializer().toJson(value);
    }
}