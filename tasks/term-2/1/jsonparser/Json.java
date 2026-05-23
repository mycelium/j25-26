package jsonparser;

import java.util.Map;

public class Json {

    private static final JsonSerializer SERIALIZER = new JsonSerializer();

    private Json() {}


    public static Object parse(String json) {
        return new JsonParser(json).parse();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(String json) {
        Object result = parse(json);
        if (!(result instanceof Map)) {
            throw new JsonException("Ожидался JSON-объект, получен: " +
                    (result == null ? "null" : result.getClass().getSimpleName()));
        }
        return (Map<String, Object>) result;
    }

    public static <T> T parse(String json, Class<T> clazz) {
        return new ObjectMapper().map(parse(json), clazz);
    }

    public static String toJson(Object obj) {
        return SERIALIZER.serialize(obj);
    }
}
