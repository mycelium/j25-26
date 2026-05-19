package ru.lab.json;

import ru.lab.json.mapper.ObjectMapper;
import ru.lab.json.parser.JsonStringParser;
import ru.lab.json.serializer.JsonSerializer;
import ru.lab.json.exception.JsonException;
import java.util.Map;

public class Json {

    private Json() {} 

    public static String toJson(Object obj) {
        return JsonSerializer.toJson(obj);
    }

    public static Object fromJson(String json) {
        return new JsonStringParser(json).parse();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJsonAsMap(String json) {
        Object parsed = new JsonStringParser(json).parse();
        if (parsed instanceof Map<?,?> map) { 
            return (Map<String, Object>) map;
        }
        throw new JsonException("Root element is not a JSON object");
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String json, Class<T> clazz) {
        Object parsed = new JsonStringParser(json).parse();
        return (T) ObjectMapper.convertType(parsed, clazz, clazz);
    }
}