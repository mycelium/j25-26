package json;

import java.util.Map;

public final class Json {

    private Json() {}

    public static String toJson(Object obj) {
        return JsonSerializer.serialize(obj);
    }

    public static Object parse(String json) {
        return new JsonParser(json).parse();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(String json) {
        var result = parse(json);
        if (result instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new JsonException("Expected a JSON object but got: " + (result == null ? "null" : result.getClass().getSimpleName()));
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        var parsed = parse(json);
        return JsonMapper.map(parsed, clazz);
    }
}
