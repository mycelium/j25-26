package com.jsonparser;

import com.jsonparser.core.JsonDeserializer;
import com.jsonparser.core.JsonSerializer;
import com.jsonparser.exception.JsonParseException;
import java.util.Map;

/**
 * Публичное API для JSON парсера
 */
public class JsonParser {
    /**
     * Преобразует JSON строку в Map<String, Object>
     *
     * @param json JSON строка для парсинга
     * @return Map с данными из JSON
     * @throws JsonParseException если JSON невалидный
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        JsonDeserializer deserializer = new JsonDeserializer(json);
        return deserializer.parseAsMap();
    }

    /**
     * Преобразует JSON строку в объект указанного класса
     *
     * @param json JSON строка для парсинга
     * @param clazz целевой класс
     * @param <T> тип целевого объекта
     * @return объект типа T с заполненными полями
     * @throws JsonParseException если JSON невалидный или поля не соответствуют
     */
    public static <T> T fromJsonToObject(String json, Class<T> clazz) {
        JsonDeserializer deserializer = new JsonDeserializer(json);
        return deserializer.parseAsObject(clazz);
    }

    /**
     * Преобразует Java объект в JSON строку
     *
     * @param obj Java объект для сериализации
     * @return JSON строка
     * @throws JsonParseException если объект не может быть сериализован
     */
    public static String toJson(Object obj) {
        JsonSerializer serializer = new JsonSerializer();
        return serializer.serialize(obj);
    }
}