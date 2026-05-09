package ru.example.json;

import java.util.Map;

public class Json {

    public static Object parse(String json) {
        return new JsonParser(json).parse();
    }

    public static Map<String, Object> parseToMap(String json) {
        return (Map<String, Object>) parse(json);
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        return JsonMapper.fromJson(json, clazz);
    }

    public static String stringify(Object obj) {
        return JsonSerializer.toJson(obj);
    }
}
