package org.example.json;

import java.util.Map;

public class JsonLib {
    
    public static String stringify(Object obj) {
        return JsonWriter.write(obj);
    }
    
    public static Object read(String json) {
        return JsonReader.process(json);
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> readAsMap(String json) {
        Object result = read(json);
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }
        throw new IllegalArgumentException("JSON не является объектом");
    }
    
    public static <T> T readAsObject(String json, Class<T> targetType) {
        Object data = read(json);
        return ObjectMapper.convert(data, targetType);
    }
}
