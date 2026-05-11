package myjson;

import java.util.Map;

public final class JsonParser {

    private JsonParser() {} 
    public static String toJson(Object obj) {
        return JsonSerializer.serialize(obj).toString();
    }
    public static Object parse(String json) {
        return JsonDeserializer.parse(json);
    }
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(String json) {
        Object parsed = parse(json);
        if (parsed instanceof Map) {
            return (Map<String, Object>) parsed;
        }
        throw new RuntimeException("JSON is not an object");
    }
    @SuppressWarnings("unchecked")
    public static <T> T parse(String json, Class<T> clazz) {
        return (T) JsonDeserializer.parseToObject(json, clazz);
    }
}