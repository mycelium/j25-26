package jsonlib;

import java.util.Map;


public final class Json {

    private Json() {}

    
    /** Parse any JSON value → Map, List, String, Number, Boolean, or null. */
    public static Object parse(String json) {
        return JsonParser.parse(json);
    }

    /**
     * Parse a JSON object into a {@code Map<String, Object>}.
     */
    public static Map<String, Object> toMap(String json) {
        Object result = parse(json);
        if (!(result instanceof Map)) {
            throw new IllegalArgumentException(
                "Expected a JSON object but got: "
                + (result == null ? "null" : result.getClass().getSimpleName())
            );
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        return map;
    }

     
    public static <T> T toObject(String json, Class<T> clazz) {
        return JsonMapper.DEFAULT.fromJson(json, clazz);
    }

    
    public static <T> T toObject(String json, TypeReference<T> typeRef) {
        return JsonMapper.DEFAULT.fromJson(json, typeRef);
    }

    
    public static String toJson(Object obj) {
        return JsonMapper.DEFAULT.toJson(obj);
    }
}
