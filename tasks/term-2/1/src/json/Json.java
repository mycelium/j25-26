package json;

import java.util.Map;

public class Json {

    private static final JsonParser parser = new JsonParser();
    private static final JsonMapper mapper = new JsonMapper();
    private static final JsonSerializer serializer = new JsonSerializer();

    public static Map<String, Object> parseToMap(String json) {
        return (Map<String, Object>) parser.parse(json);
    }

    public static <T> T parse(String json, Class<T> clazz) {
        Object parsed = parser.parse(json);
        return mapper.map(parsed, clazz);
    }

    public static String toJson(Object obj) {
        return serializer.toJson(obj);
    }
}