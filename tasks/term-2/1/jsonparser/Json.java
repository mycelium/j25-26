package jsonparser;

import java.util.Map;

/**
 * Public API for the JSON parser library.
 *
 * <pre>
 *   // Parse JSON to a generic Java object
 *   Object obj = Json.parse("[1, 2, 3]");
 *
 *   // Parse JSON object to Map<String, Object>
 *   Map<String, Object> map = Json.parseToMap("{\"key\": 42}");
 *
 *   // Parse JSON to a specific class (requires no-arg constructor)
 *   MyClass obj = Json.parse(json, MyClass.class);
 *
 *   // Convert any Java object to a JSON string
 *   String json = Json.stringify(obj);
 * </pre>
 */
public class Json {
    private static final JsonGenerator GENERATOR = new JsonGenerator();

    private Json() {}

    /** Parses a JSON string into a Java object:
     *  objects → {@code Map<String,Object>}, arrays → {@code List<Object>},
     *  strings → {@code String}, numbers → {@code Integer}/{@code Long}/{@code Double},
     *  booleans → {@code Boolean}, null → {@code null}.
     */
    public static Object parse(String json) {
        return new JsonParser(json).parse();
    }

    /** Parses a JSON object string into {@code Map<String, Object>}.
     *  Throws {@link JsonException} if the root value is not a JSON object.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(String json) {
        Object result = parse(json);
        if (!(result instanceof Map))
            throw new JsonException("Expected JSON object at root, got: " +
                    (result == null ? "null" : result.getClass().getSimpleName()));
        return (Map<String, Object>) result;
    }

    /** Parses a JSON string and maps it to an instance of {@code clazz}.
     *  The class must have a no-arg constructor (can be private).
     *  Fields are matched by name; missing fields are left at their default values.
     */
    public static <T> T parse(String json, Class<T> clazz) {
        return new ObjectMapper().convert(parse(json), clazz);
    }

    /** Converts any Java object to a JSON string.
     *  Supports primitives, boxed types, {@code String}, arrays, {@link java.util.Collection},
     *  {@link java.util.Map}, and arbitrary objects (fields serialized via reflection).
     *  {@code null} values are written as JSON {@code null}.
     */
    public static String stringify(Object obj) {
        return GENERATOR.generate(obj);
    }
}
