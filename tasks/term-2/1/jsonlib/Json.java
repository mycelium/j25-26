package jsonlib;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Json {

    public static <T> T fromJson(String json, Class<T> clazz) {
        Object parsed = parseJson(json);
        return deserialize(parsed, clazz);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJsonToMap(String json) {
        Object parsed = parseJson(json);
        if (parsed instanceof Map) {
            return (Map<String, Object>) parsed;
        }
        throw new JsonException("JSON does not represent a Map");
    }

    public static Object fromJson(String json) {
        return parseJson(json);
    }

    public static String toJson(Object obj) {
        return serialize(obj);
    }

    private static Object parseJson(String json) {
        JsonTokenizer tokenizer = new JsonTokenizer(json);
        return parseValue(tokenizer);
    }

    private static Object parseValue(JsonTokenizer tokenizer) {
        String token = tokenizer.peek();
        if (token == null) {
            throw new JsonException("Unexpected end of input");
        }

        switch (token) {
            case "{": 
                return parseObject(tokenizer);
            case "[": 
                return parseArray(tokenizer);
            case "true": 
                tokenizer.next(); return 
                Boolean.TRUE;
            case "false": 
                tokenizer.next(); 
                return Boolean.FALSE;
            case "null": 
                tokenizer.next();
                return null;
            default:
                if (token.startsWith("\"")) {
                    return parseString(tokenizer);
                }
                else {
                    return parseNumber(tokenizer);
                }
        }
    }

    private static Map<String, Object> parseObject(JsonTokenizer tokenizer) {
        Map<String, Object> map = new LinkedHashMap<>();
        tokenizer.next();
        while (true) {
            String token = tokenizer.peek();
            
            if (token == null)  {
                throw new JsonException("Unclosed object");
            }
            
            if (token.equals("}")) {
                tokenizer.next(); break; 
            }
            
            if (!token.startsWith("\"")) {
                throw new JsonException("Expected string key, got: " + token);
            }
            
            String key = parseString(tokenizer);
            if (!":".equals(tokenizer.next())) {
                throw new JsonException("Expected ':'");
            }

            Object value = parseValue(tokenizer);
            map.put(key, value);
            String next = tokenizer.peek();

            if (next.equals(",")) { 
                tokenizer.next(); continue; 
            }

            if (next.equals("}")) continue;

            throw new JsonException("Expected ',' or '}', got: " + next);
        }
        return map;
    }

    private static List<Object> parseArray(JsonTokenizer tokenizer) {
        List<Object> list = new ArrayList<>();
        tokenizer.next(); // '['
        while (true) {
            String token = tokenizer.peek();
            if (token == null) throw new JsonException("Unclosed array");
            if (token.equals("]")) { tokenizer.next(); break; }
            list.add(parseValue(tokenizer));
            String next = tokenizer.peek();
            if (next.equals(",")) { tokenizer.next(); continue; }
            if (next.equals("]")) continue;
            throw new JsonException("Expected ',' or ']', got: " + next);
        }
        return list;
    }

    private static String parseString(JsonTokenizer tokenizer) {
        String token = tokenizer.next();
        String raw = token.substring(1, token.length() - 1);
        return raw.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\/", "/");
    }

    private static Number parseNumber(JsonTokenizer tokenizer) {
        String token = tokenizer.next();
        try {
            if (token.contains(".") || token.contains("e") || token.contains("E")) {
                return Double.parseDouble(token);
            }

            long longVal = Long.parseLong(token);
            if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                return (int) longVal;
            }

            return longVal;
        } catch (NumberFormatException e) {
            throw new JsonException("Invalid number: " + token, e);
        }
    }

    private static String serialize(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + escapeString((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map) {
            return serializeMap((Map<?, ?>) obj);
        }
        if (obj instanceof Collection) {
            return serializeCollection((Collection<?>) obj);
        }
        if (obj.getClass().isArray()) {
            return serializeArray(obj);
        }
        return serializeObject(obj);
    }

    private static String serializeMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<?, ?> entry = it.next();
            sb.append("\"").append(escapeString(String.valueOf(entry.getKey()))).append("\":");
            sb.append(serialize(entry.getValue()));
            if (it.hasNext()) sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String serializeCollection(Collection<?> col) {
        StringBuilder sb = new StringBuilder("[");
        Iterator<?> it = col.iterator();
        while (it.hasNext()) {
            sb.append(serialize(it.next()));
            if (it.hasNext()) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String serializeArray(Object array) {
        int len = Array.getLength(array);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < len; i++) {
            sb.append(serialize(Array.get(array, i)));
            if (i < len - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String serializeObject(Object obj) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (first) first = false;
                else sb.append(",");
                sb.append("\"").append(escapeString(field.getName())).append("\":");
                sb.append(serialize(value));
            } catch (IllegalAccessException e) {
                throw new JsonException("Cannot access field: " + field.getName(), e);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escapeString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("/", "\\/")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    @SuppressWarnings("unchecked")
    private static <T> T deserialize(Object source, Class<T> targetClass) {
        if (source == null) return null;

        if (targetClass == Object.class) {
            return (T) source;
        }

        if (targetClass == String.class) {
            return (T) source.toString();
        }

        if (targetClass == Boolean.class || targetClass == boolean.class) {
            return (T) source;
        }
        if (targetClass == Integer.class || targetClass == int.class) {
            return (T) Integer.valueOf(((Number) source).intValue());
        }
        if (targetClass == Long.class || targetClass == long.class) {
            return (T) Long.valueOf(((Number) source).longValue());
        }
        if (targetClass == Double.class || targetClass == double.class) {
            return (T) Double.valueOf(((Number) source).doubleValue());
        }
        if (targetClass == Float.class || targetClass == float.class) {
            return (T) Float.valueOf(((Number) source).floatValue());
        }
        if (targetClass == Byte.class || targetClass == byte.class) {
            return (T) Byte.valueOf(((Number) source).byteValue());
        }
        if (targetClass == Short.class || targetClass == short.class) {
            return (T) Short.valueOf(((Number) source).shortValue());
        }
        if (targetClass == Character.class || targetClass == char.class) {
            String s = source.toString();
            if (s.length() != 1) throw new JsonException("Cannot convert to char: " + s);
            return (T) Character.valueOf(s.charAt(0));
        }

        if (targetClass.isArray()) {
            return (T) deserializeArray(source, targetClass);
        }

        if (Collection.class.isAssignableFrom(targetClass)) {
            return (T) deserializeCollection(source, targetClass);
        }

        if (Map.class.isAssignableFrom(targetClass)) {
            return (T) deserializeMap(source, targetClass);
        }

        return deserializeObject(source, targetClass);
    }

    private static Object deserializeArray(Object source, Class<?> targetClass) {
        if (!(source instanceof List)) {
            throw new JsonException("Expected array, got: " + source);
        }
        List<?> list = (List<?>) source;
        Class<?> componentType = targetClass.getComponentType();
        Object array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, deserialize(list.get(i), componentType));
        }
        return array;
    }

    private static Collection<?> deserializeCollection(Object source, Class<?> targetClass) {
        if (!(source instanceof List)) {
            throw new JsonException("Expected array, got: " + source);
        }
        List<?> list = (List<?>) source;
        Collection<Object> collection;
        if (targetClass == List.class || targetClass == ArrayList.class) {
            collection = new ArrayList<>();
        } else if (targetClass == Set.class || targetClass == HashSet.class) {
            collection = new HashSet<>();
        } else {
            try {
                collection = (Collection<Object>) targetClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new JsonException("Cannot instantiate collection: " + targetClass, e);
            }
        }
        for (Object item : list) {
            collection.add(deserialize(item, Object.class));
        }
        return collection;
    }

    private static Map<?, ?> deserializeMap(Object source, Class<?> targetClass) {
        if (!(source instanceof Map)) {
            throw new JsonException("Expected object, got: " + source);
        }
        Map<?, ?> sourceMap = (Map<?, ?>) source;
        Map<Object, Object> map;
        if (targetClass == Map.class || targetClass == HashMap.class) {
            map = new HashMap<>();
        } else if (targetClass == LinkedHashMap.class) {
            map = new LinkedHashMap<>();
        } else {
            
            try {
                map = (Map<Object, Object>) targetClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new JsonException("Cannot instantiate map: " + targetClass, e);
            }

        }

        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            String key = entry.getKey().toString();
            map.put(key, deserialize(entry.getValue(), Object.class));
        }
        return map;
    }

    private static <T> T deserializeObject(Object source, Class<T> targetClass) {
        if (!(source instanceof Map)) {
            throw new JsonException("Expected object, got: " + source);
        }

        Map<String, Object> sourceMap = (Map<String, Object>) source;
        T instance;

        try {
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();
        } catch (Exception e) {
            throw new JsonException("Cannot instantiate " + targetClass, e);
        }

        for (Field field : targetClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            
            String fieldName = field.getName();

            if (!sourceMap.containsKey(fieldName)) continue;
            
            Object value = sourceMap.get(fieldName);
            field.setAccessible(true);
            
            try {
                field.set(instance, deserialize(value, field.getType()));
            } 
            catch (IllegalAccessException e) {
                throw new JsonException("Cannot set field: " + fieldName, e);
            }
        }
        return instance;
    }
}