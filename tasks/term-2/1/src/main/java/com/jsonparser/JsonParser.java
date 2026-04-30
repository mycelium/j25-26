package com.jsonparser;

import com.jsonparser.core.JsonDeserializer;
import com.jsonparser.core.JsonSerializer;
import com.jsonparser.exception.JsonParseException;
import java.util.Map;

/**
 * JSON парсер
 */
public class JsonParser {
    // JSON строку в Java объект
    public static Object fromJson(String json) {
        JsonDeserializer deserializer = new JsonDeserializer(json);
        return deserializer.parse();
    }

    // JSON строку в Map<String, Object>
    public static Map<String, Object> fromJsonToMap(String json) {
        JsonDeserializer deserializer = new JsonDeserializer(json);
        return deserializer.parseAsMap();
    }

    // JSON строку в объект указанного класса
    public static <T> T fromJsonToObject(String json, Class<T> clazz) {
        JsonDeserializer deserializer = new JsonDeserializer(json);
        return deserializer.parseAsObject(clazz);
    }

    // Java объект в JSON строку
    public static String toJson(Object obj) {
        JsonSerializer serializer = new JsonSerializer();
        return serializer.serialize(obj);
    }
}