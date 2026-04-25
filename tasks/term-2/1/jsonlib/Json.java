package jsonlib;

import java.util.Map;

public class Json {

    public static Object parse(String json) {
        return JsonParser.parse(json);
    }

    public static Map<String, Object> toMap(String json) {
        return (Map<String, Object>) parse(json);
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        return JsonMapper.toObject(json, clazz);
    }
    public static String toJson(Object obj) {
        return JsonMapper.toJson(obj);
    }
}