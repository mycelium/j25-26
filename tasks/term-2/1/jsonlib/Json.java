package jsonlib;

import java.util.Map;

public final class Json {
    private Json() {}

    public static <T> T fromJson(String json, Class<T> targetClass) {
        Object parsed = JsonParser.parse(json);
        return JsonMapper.map(parsed, targetClass);
    }

    public static Map<String, Object> fromJsonToMap(String json) {
        Object parsed = JsonParser.parse(json);
        if (parsed instanceof Map) {
            return (Map<String, Object>) parsed;
        }
        throw new RuntimeException("JSON is not an object");
    }

    public static String toJson(Object obj) {
        return JsonGenerator.generate(obj);
    }
}