package jsonlib;

import jsonlib.converter.JsonToClassMapper;
import jsonlib.converter.JsonToObjectConverter;
import jsonlib.parser.JsonParser;
import jsonlib.parser.JsonTokenizer;
import jsonlib.node.JsonNode;
import jsonlib.serializer.JsonSerializer;

import java.util.Map;

/**
 * Основной класс библиотеки для работы с JSON.
 * Предоставляет статические методы для парсинга и сериализации.
 */
public final class Json {
    private Json() {}

    /**
     * Разбирает JSON-строку в Object (Map, List, String, Number, Boolean, null).
     */
    public static Object parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new JsonParseException("Input JSON string is null or empty");
        }
        JsonNode root = parseToNode(json);
        return new JsonToObjectConverter().convert(root);
    }

    /**
     * Разбирает JSON-строку в Map<String, Object>. Корень должен быть объектом.
     */
    public static Map<String, Object> parseToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new JsonParseException("Input JSON string is null or empty");
        }
        JsonNode root = parseToNode(json);
        return new JsonToObjectConverter().convertToMap(root);
    }

    /**
     * Разбирает JSON-строку в экземпляр указанного класса.
     */
    public static <T> T parse(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            throw new JsonParseException("Input JSON string is null or empty");
        }
        JsonNode root = parseToNode(json);
        // Специальный случай для Map.class – можно использовать parseToMap
        if (Map.class.isAssignableFrom(clazz)) {
            return clazz.cast(new JsonToObjectConverter().convertToMap(root));
        }
        return new JsonToClassMapper().map(root, clazz);
    }

    /**
     * Сериализует объект в компактную JSON-строку.
     */
    public static String toJson(Object obj) {
        return new JsonSerializer().serialize(obj, false);
    }

    /**
     * Сериализует объект в JSON-строку с опциональным форматированием (pretty print).
     */
    public static String toJson(Object obj, boolean pretty) {
        return new JsonSerializer().serialize(obj, pretty);
    }

    private static JsonNode parseToNode(String json) {
        JsonTokenizer tokenizer = new JsonTokenizer(json);
        JsonParser parser = new JsonParser(tokenizer);
        return parser.parse();
    }
}