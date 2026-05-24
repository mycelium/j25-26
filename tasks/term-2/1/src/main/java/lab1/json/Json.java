package lab1.json;

import java.util.Map;

/**
 * Public API for JSON parsing and serialization.
 *
 * <p>Example usage:
 * <pre>{@code
 * Json json = new Json();
 * User user = json.parse(jsonString, User.class);
 * String result = json.toJson(user);
 * }</pre>
 */
public class Json {
    private final JsonMapper mapper = new JsonMapper();
    private final JsonSerializer serializer = new JsonSerializer();

    /**
     * Parses a JSON string into a Java object.
     *
     * <p>Returns {@link java.util.Map} for JSON objects,
     * {@link java.util.List} for JSON arrays, {@link String}, {@link Number},
     * {@link Boolean} or {@code null} for primitive JSON values.
     *
     * @param json the JSON string to parse
     * @return parsed Java object
     * @throws JsonException if the input is not valid JSON
     */
    public Object parse(String json) {
        return new JsonParser().parse(json);
    }

    /**
     * Parses a JSON object string into a {@code Map<String, Object>}.
     *
     * @param json the JSON string to parse
     * @return parsed map
     * @throws JsonException if the root element is not a JSON object
     */
    public Map<String, Object> parseToMap(String json) {
        Object value = new JsonParser().parse(json);
        if (!(value instanceof Map<?, ?> map)) {
            throw new JsonException("JSON root is not an object");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) map;
        return result;
    }

    /**
     * Parses a JSON string and maps it to an instance of the given class.
     *
     * @param json  the JSON string to parse
     * @param clazz target class
     * @param <T>   target type
     * @return mapped instance
     * @throws JsonException if parsing or mapping fails
     */
    public <T> T parse(String json, Class<T> clazz) {
        Object value = new JsonParser().parse(json);
        return mapper.convert(value, clazz);
    }

    /**
     * Serializes a Java object to a JSON string.
     *
     * @param object the object to serialize, may be {@code null}
     * @return JSON string representation
     * @throws JsonException if serialization fails
     */
    public String toJson(Object object) {
        return serializer.toJson(object);
    }
}
