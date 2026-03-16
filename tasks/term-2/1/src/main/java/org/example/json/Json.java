package org.example.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Публичный API библиотеки для работы с JSON.
 */
public class Json {

    // Публичный API

    /**
     * Конвертирует Java объект в JSON строку.
     */
    public static String toJson(Object obj) {
        return Serializer.serialize(obj);
    }

    /**
     * Читает JSON строку в базовый Java Object (Map, List, String, Number, Boolean, null).
     */
    public static Object parse(String json) {
        return new Parser(json).parseValue();
    }

    /**
     * Читает JSON строку в Map<String, Object>.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(String json) {
        Object result = parse(json);
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }
        throw new IllegalArgumentException("JSON string does not represent a JSON object");
    }

    /**
     * Читает JSON строку и мапит ее в указанный класс.
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        Object parsed = parse(json);
        return Mapper.map(parsed, clazz);
    }


    /**
     * Сериализатор: Java Object -> JSON String
     */
    private static class Serializer {

        public static String serialize(Object obj) {
            // Создаем Set, который сравнивает объекты по строгим ссылкам в памяти (==), а не по equals()
            Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            return serializeInternal(obj, visited);
        }

        private static String serializeInternal(Object obj, Set<Object> visited) {
            if (obj == null) return "null";

            // 1. ОГРАНИЧЕНИЕ: Проверка на циклические зависимости
            if (visited.contains(obj)) {
                return "\"__CIRCULAR_REFERENCE__\""; // Заменяем цикл безопасной строкой
            }

            Class<?> clazz = obj.getClass();

            // 2. ОГРАНИЧЕНИЕ: Непредставимые типы
            if (isNonRepresentable(clazz)) {
                return "null";
            }
            if (obj instanceof Float && (((Float) obj).isNaN() || ((Float) obj).isInfinite())) {
                return "null"; // JSON не поддерживает NaN и Infinity
            }
            if (obj instanceof Double && (((Double) obj).isNaN() || ((Double) obj).isInfinite())) {
                return "null";
            }

            // Базовые типы не могут образовать цикл, обрабатываем их сразу
            if (clazz == String.class || clazz == Character.class) {
                return "\"" + escapeString(obj.toString()) + "\"";
            }
            if (Number.class.isAssignableFrom(clazz) || clazz == Boolean.class || clazz.isPrimitive()) {
                return obj.toString();
            }

            // Добавляем сложный объект в "посещенные", прежде чем нырять в него
            visited.add(obj);
            
            String result;
            if (clazz.isArray()) {
                result = serializeArray(obj, visited);
            } else if (Collection.class.isAssignableFrom(clazz)) {
                result = serializeCollection((Collection<?>) obj, visited);
            } else if (Map.class.isAssignableFrom(clazz)) {
                result = serializeMap((Map<?, ?>) obj, visited);
            } else {
                result = serializeObject(obj, visited);
            }

            // Убираем объект из "посещенных" при выходе из рекурсии (чтобы один и тот же объект 
            // мог безопасно лежать в разных элементах одного массива)
            visited.remove(obj);

            return result;
        }

        // Вспомогательный метод для фильтрации системных/непредставимых типов
        private static boolean isNonRepresentable(Class<?> clazz) {
            return Thread.class.isAssignableFrom(clazz) ||
                   java.io.InputStream.class.isAssignableFrom(clazz) ||
                   java.io.OutputStream.class.isAssignableFrom(clazz) ||
                   java.io.File.class.isAssignableFrom(clazz) ||
                   Class.class.isAssignableFrom(clazz);
        }

        // --- Дальше идут обновленные методы коллекций, куда мы пробрасываем visited ---

        private static String serializeArray(Object array, Set<Object> visited) {
            int length = Array.getLength(array);
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            for (int i = 0; i < length; i++) {
                joiner.add(serializeInternal(Array.get(array, i), visited));
            }
            return joiner.toString();
        }

        private static String serializeCollection(Collection<?> collection, Set<Object> visited) {
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            for (Object item : collection) {
                joiner.add(serializeInternal(item, visited));
            }
            return joiner.toString();
        }

        private static String serializeMap(Map<?, ?> map, Set<Object> visited) {
            StringJoiner joiner = new StringJoiner(",", "{", "}");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = "\"" + escapeString(String.valueOf(entry.getKey())) + "\"";
                String value = serializeInternal(entry.getValue(), visited);
                joiner.add(key + ":" + value);
            }
            return joiner.toString();
        }

        private static String serializeObject(Object obj, Set<Object> visited) {
            StringJoiner joiner = new StringJoiner(",", "{", "}");
            Class<?> clazz = obj.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    String key = "\"" + escapeString(field.getName()) + "\"";
                    joiner.add(key + ":" + serializeInternal(value, visited));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot access field: " + field.getName(), e);
                }
            }
            return joiner.toString();
        }

        private static String escapeString(String str) {
            return str.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    /**
     * Парсер: JSON String -> Map/List/Primitives
     */
    private static class Parser {
        private final String json;
        private int pos = 0;

        public Parser(String json) {
            this.json = json;
        }

        public Object parseValue() {
            skipWhitespace();
            if (pos >= json.length()) throw new IllegalArgumentException("Unexpected end of JSON");

            char c = json.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (Character.isDigit(c) || c == '-') return parseNumber();

            throw new IllegalArgumentException("Unexpected character at pos " + pos + ": " + c);
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // skip '{'
            skipWhitespace();
            if (json.charAt(pos) == '}') {
                pos++;
                return map;
            }

            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                if (json.charAt(pos) != ':') throw new IllegalArgumentException("Expected ':' at " + pos);
                pos++; // skip ':'
                Object value = parseValue();
                map.put(key, value);

                skipWhitespace();
                if (json.charAt(pos) == '}') {
                    pos++;
                    break;
                }
                if (json.charAt(pos) == ',') {
                    pos++;
                } else {
                    throw new IllegalArgumentException("Expected ',' or '}' at " + pos);
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // skip '['
            skipWhitespace();
            if (json.charAt(pos) == ']') {
                pos++;
                return list;
            }

            while (true) {
                list.add(parseValue());
                skipWhitespace();
                if (json.charAt(pos) == ']') {
                    pos++;
                    break;
                }
                if (json.charAt(pos) == ',') {
                    pos++;
                } else {
                    throw new IllegalArgumentException("Expected ',' or ']' at " + pos);
                }
            }
            return list;
        }

        private String parseString() {
            pos++; // skip opening '"'
            StringBuilder sb = new StringBuilder();
            while (pos < json.length()) {
                char c = json.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    c = json.charAt(pos++);
                    // Basic escaping support
                    if (c == '"' || c == '\\') sb.append(c);
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("Unterminated string");
        }

        private Number parseNumber() {
            int start = pos;
            while (pos < json.length()) {
                char c = json.charAt(pos);
                if (Character.isDigit(c) || c == '-' || c == '.' || c == 'e' || c == 'E') {
                    pos++;
                } else {
                    break;
                }
            }
            String numStr = json.substring(start, pos);
            if (numStr.contains(".")) {
                return Double.parseDouble(numStr);
            } else {
                long l = Long.parseLong(numStr);
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
                return l;
            }
        }

        private Boolean parseBoolean() {
            if (json.startsWith("true", pos)) {
                pos += 4;
                return true;
            }
            if (json.startsWith("false", pos)) {
                pos += 5;
                return false;
            }
            throw new IllegalArgumentException("Invalid boolean at " + pos);
        }

        private Object parseNull() {
            if (json.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Invalid null at " + pos);
        }

        private void skipWhitespace() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }
        }
    }

    /**
     * Маппер: Map/List -> Java Object (через Reflection)
     */
    private static class Mapper {
        @SuppressWarnings("unchecked")
        public static <T> T map(Object parsedVal, Class<T> clazz) {
            if (parsedVal == null) return null;

            // Базовые типы
            if (clazz == String.class) return (T) parsedVal.toString();
            if (clazz == Integer.class || clazz == int.class) return (T) Integer.valueOf(((Number) parsedVal).intValue());
            if (clazz == Double.class || clazz == double.class) return (T) Double.valueOf(((Number) parsedVal).doubleValue());
            if (clazz == Long.class || clazz == long.class) return (T) Long.valueOf(((Number) parsedVal).longValue());
            if (clazz == Boolean.class || clazz == boolean.class) return (T) parsedVal;

            // Коллекции (Упрощенно: мапим в ArrayList, если требуется List)
            if (Collection.class.isAssignableFrom(clazz)) {
                return (T) new ArrayList<>((List<?>) parsedVal);
            }

            // Массивы
            if (clazz.isArray()) {
                List<?> list = (List<?>) parsedVal;
                Class<?> componentType = clazz.getComponentType();
                Object array = Array.newInstance(componentType, list.size());
                for (int i = 0; i < list.size(); i++) {
                    Array.set(array, i, map(list.get(i), componentType));
                }
                return (T) array;
            }

            // Пользовательские объекты (маппинг из Map)
            if (parsedVal instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) parsedVal;
                try {
                    T instance = clazz.getDeclaredConstructor().newInstance();
                    for (Field field : clazz.getDeclaredFields()) {
                        if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                            continue;
                        }
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        if (map.containsKey(fieldName)) {
                            Object mapValue = map.get(fieldName);
                            Object convertedValue = map(mapValue, field.getType());
                            field.set(instance, convertedValue);
                        }
                    }
                    return instance;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate or map class: " + clazz.getName(), e);
                }
            }

            return (T) parsedVal;
        }
    }
}