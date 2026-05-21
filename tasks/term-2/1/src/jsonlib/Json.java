package jsonlib;

import java.util.Map;



public class Json {

    public static String toJson(Object object) {
        return JsonSerializer.serialize(object);
    }

    
    public static Object parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }
        return JsonParser.parse(json);
    }

    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(String json) {
        Object result = parse(json);
        if (!(result instanceof Map)) {
            throw new ClassCastException("Top level JSON is not an object");
        }
        return (Map<String, Object>) result;
    }

   
    public static <T> T fromJson(String json, Class<T> clazz) {
        Object parsedData = parse(json);
        return JsonMapper.map(parsedData, clazz);
    }
}