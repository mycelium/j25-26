package json;

import java.util.Map;

public final class JsonLib {
    private JsonLib() {}

    public static <T> T deserialize(String json, Class<T> clazz) {
        Object intermediate = JsonReader.read(json);
        return TypeUtils.convert(intermediate, clazz);
    }


    @SuppressWarnings("unchecked")
    public static Map<String, Object> deserializeToMap(String json) {
        Object result = JsonReader.read(json);
        if (!(result instanceof Map)) {
            throw new JsonException("Root element is not a JSON object");
        }
        return (Map<String, Object>) result;
    }


    public static String serialize(Object obj) {
        return JsonWriter.write(obj);
    }


    public static class JsonException extends RuntimeException {
        public JsonException(String msg) { super(msg); }
        public JsonException(String msg, Throwable cause) { super(msg, cause); }
    }
}