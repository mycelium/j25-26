package json;

import java.util.Map;
import java.util.Objects;

public final class Json {
    private static final JsonMapper DEFAULT_MAPPER = new JsonMapper();

    private Json() {
    }

    public static Object parse(String json) {
        return DEFAULT_MAPPER.read(json);
    }

    public static Map<String, Object> parseToMap(String json) {
        return DEFAULT_MAPPER.readAsMap(json);
    }

    public static <T> T parse(String json, Class<T> targetClass) {
        return DEFAULT_MAPPER.read(json, targetClass);
    }

    public static String stringify(Object value) {
        return DEFAULT_MAPPER.write(value);
    }

    public static JsonMapper mapper(JsonConfig config) {
        return new JsonMapper(Objects.requireNonNull(config, "config must not be null"));
    }
}
