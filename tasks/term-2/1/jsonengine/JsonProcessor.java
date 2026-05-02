package jsonengine;

import java.util.Map;

public final class JsonProcessor {
    private JsonProcessor() {}

    public static <T> T read(String json, Class<T> clazz) {
        Object rawData = ValueParser.parse(json);
        return DataMapper.convert(rawData, clazz);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> readAsMap(String json) {
        Object result = ValueParser.parse(json);
        if (!(result instanceof Map)) {
            throw new JsonException("Root element is not a JSON Object");
        }
        return (Map<String, Object>) result;
    }

    public static String write(Object target) {
        return ValueSerializer.stringify(target);
    }
}