package myjson;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class JsonParser {

    private JsonParser() {}

    public static String toJson(Object obj) {
        return serialize(obj).toString();
    }

    public static Object parse(String json) {
        return new InternalParser(json.trim()).parse();
    }

    public static Map<String, Object> parseToMap(String json) {
        Object parsed = parse(json);
        if (parsed instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) parsed;
            return map;
        }
        throw new RuntimeException("JSON is not an object");
    }

    public static <T> T parse(String json, Class<T> clazz) {
        Object parsed = parse(json);
        if (clazz == Map.class || clazz == Object.class) {
            T result = (T) parsed;
            return result;
        }
        if (parsed instanceof Map) {
            return mapToObject((Map<String, Object>) parsed, clazz);
        }
        if (parsed instanceof List) {
            if (clazz.isArray()) {
                return listToArray((List<?>) parsed, clazz);
            }
            if (Collection.class.isAssignableFrom(clazz)) {
                Collection<Object> collection = createCollection(clazz);
                collection.addAll((List<?>) parsed);
                T result = (T) collection;
                return result;
            }
        }
        throw new RuntimeException("Cannot map JSON to " + clazz.getName());
    }

    private static StringBuilder serialize(Object obj) {
        if (obj == null) {
            return new StringBuilder("null");
        }
        if (obj instanceof String) {
            return new StringBuilder("\"").append(escape((String) obj)).append("\"");
        }
        if (obj instanceof Boolean) {
            return new StringBuilder(obj.toString());
        }
        if (obj instanceof Number) {
            return new StringBuilder(obj.toString());
        }
        if (obj instanceof Collection) {
            return serializeCollection((Collection<?>) obj);
        }
        if (obj.getClass().isArray()) {
            return serializeArray(obj);
        }
        if (obj instanceof Map) {
            return serializeMap((Map<?, ?>) obj);
        }
        return serializeObject(obj);
    }

    private static StringBuilder serializeCollection(Collection<?> col) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : col) {
            if (!first) sb.append(",");
            sb.append(serialize(item));
            first = false;
        }
        sb.append("]");
        return sb;
    }

    private static StringBuilder serializeArray(Object arr) {
        StringBuilder sb = new StringBuilder("[");
        int len = Array.getLength(arr);
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(",");
            sb.append(serialize(Array.get(arr, i)));
        }
        sb.append("]");
        return sb;
    }

    private static StringBuilder serializeMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey().toString()).append("\":");
            sb.append(serialize(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb;
    }

    private static StringBuilder serializeObject(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            try {
                Object value = f.get(obj);
                if (!first) sb.append(",");
                sb.append("\"").append(f.getName()).append("\":");
                sb.append(serialize(value));
                first = false;
            } catch (IllegalAccessException ignored) {}
        }
        sb.append("}");
        return sb;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static class InternalParser {
        private final String input;
        private int pos;

        InternalParser(String input) {
            this.input = input;
            this.pos = 0;
        }

        Object parse() {
            skipSpaces();
            if (pos >= input.length()) return null;
            char c = input.charAt(pos);
            switch (c) {
                case '{': return parseObject();
                case '[': return parseArray();
                case '"': return parseString();
                case 't': case 'f': return parseBoolean();
                case 'n': return parseNull();
                default:  return parseNumber();
            }
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++;
            skipSpaces();
            if (pos < input.length() && input.charAt(pos) == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipSpaces();
                if (input.charAt(pos) != '"')
                    throw new RuntimeException("Expected string key at " + pos);
                String key = parseString();
                skipSpaces();
                if (input.charAt(pos) != ':')
                    throw new RuntimeException("Expected ':' at " + pos);
                pos++;
                skipSpaces();
                Object value = parse();
                map.put(key, value);
                skipSpaces();
                if (pos >= input.length()) break;
                char ch = input.charAt(pos);
                if (ch == '}') {
                    pos++;
                    break;
                }
                if (ch == ',') {
                    pos++;
                } else {
                    throw new RuntimeException("Unexpected character in object: " + ch);
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++;
            skipSpaces();
            if (pos < input.length() && input.charAt(pos) == ']') {
                pos++;
                return list;
            }
            while (true) {
                skipSpaces();
                list.add(parse());
                skipSpaces();
                if (pos >= input.length()) break;
                char ch = input.charAt(pos);
                if (ch == ']') {
                    pos++;
                    break;
                }
                if (ch == ',') {
                    pos++;
                }
            }
            return list;
        }

        private String parseString() {
            pos++;
            StringBuilder sb = new StringBuilder();
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (c == '"') {
                    pos++;
                    return sb.toString();
                }
                if (c == '\\') {
                    pos++;
                    if (pos >= input.length()) throw new RuntimeException("Unexpected end after escape");
                    char escaped = input.charAt(pos);
                    switch (escaped) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        default: sb.append(escaped);
                    }
                    pos++;
                } else {
                    sb.append(c);
                    pos++;
                }
            }
            throw new RuntimeException("Unterminated string");
        }

        private Object parseBoolean() {
            if (input.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (input.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            throw new RuntimeException("Expected boolean at " + pos);
        }

        private Object parseNull() {
            if (input.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new RuntimeException("Expected null at " + pos);
        }

        private Number parseNumber() {
            int start = pos;
            while (pos < input.length() && !isDelimiter(input.charAt(pos))) {
                pos++;
            }
            String numStr = input.substring(start, pos);
            if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                return Double.parseDouble(numStr);
            } else {
                return Long.parseLong(numStr);
            }
        }

        private boolean isDelimiter(char c) {
            return c == ',' || c == '}' || c == ']' || Character.isWhitespace(c);
        }

        private void skipSpaces() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }
    }

    private static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                String fieldName = field.getName();
                if (!map.containsKey(fieldName)) continue;

                Object value = map.get(fieldName);
                field.setAccessible(true);
                if (value == null) {
                    field.set(obj, null);
                    continue;
                }
                setFieldValue(field, obj, value);
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private static void setFieldValue(Field field, Object target, Object value) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (type == int.class || type == Integer.class) {
            field.set(target, ((Number) value).intValue());
            return;
        }
        if (type == long.class || type == Long.class) {
            field.set(target, ((Number) value).longValue());
            return;
        }
        if (type == double.class || type == Double.class) {
            field.set(target, ((Number) value).doubleValue());
            return;
        }
        if (type == float.class || type == Float.class) {
            field.set(target, ((Number) value).floatValue());
            return;
        }
        if (type == boolean.class || type == Boolean.class) {
            field.set(target, value);
            return;
        }
        if (type == byte.class || type == Byte.class) {
            field.set(target, ((Number) value).byteValue());
            return;
        }
        if (type == short.class || type == Short.class) {
            field.set(target, ((Number) value).shortValue());
            return;
        }
        if (type == char.class || type == Character.class) {
            String s = value.toString();
            if (s.length() > 0) field.set(target, s.charAt(0));
            return;
        }
        if (type == String.class) {
            field.set(target, value.toString());
            return;
        }

        if (type.isArray()) {
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                Class<?> compType = type.getComponentType();
                Object array = Array.newInstance(compType, list.size());
                for (int i = 0; i < list.size(); i++) {
                    Object elem = list.get(i);
                    if (elem instanceof Map && !Map.class.isAssignableFrom(compType) && compType != Object.class) {
                        elem = mapToObject((Map<String, Object>) elem, compType);
                    }
                    if (compType.isPrimitive()) {
                        setPrimitiveArrayElement(array, i, elem, compType);
                    } else {
                        Array.set(array, i, elem);
                    }
                }
                field.set(target, array);
            } else {
                field.set(target, value);
            }
            return;
        }

        if (Collection.class.isAssignableFrom(type)) {
            if (value instanceof List) {
                Collection<Object> coll = createCollection(type);
                coll.addAll((List<?>) value);
                field.set(target, coll);
            } else {
                Collection<Object> coll = createCollection(type);
                coll.add(value);
                field.set(target, coll);
            }
            return;
        }

        if (value instanceof Map && !Map.class.isAssignableFrom(type)) {
            Object nested = mapToObject((Map<String, Object>) value, type);
            field.set(target, nested);
            return;
        }

        if (Map.class.isAssignableFrom(type)) {
            field.set(target, value);
            return;
        }

        field.set(target, value);
    }

    private static Collection<Object> createCollection(Class<?> type) {
        if (List.class.isAssignableFrom(type)) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(type)) {
            return new LinkedHashSet<>();
        } else {
            try {
                Collection<Object> instance = (Collection<Object>) type.getDeclaredConstructor().newInstance();
                return instance;
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
    }

    private static void setPrimitiveArrayElement(Object array, int index, Object value, Class<?> compType) {
        if (compType == int.class)           Array.setInt(array, index, ((Number) value).intValue());
        else if (compType == long.class)     Array.setLong(array, index, ((Number) value).longValue());
        else if (compType == double.class)   Array.setDouble(array, index, ((Number) value).doubleValue());
        else if (compType == float.class)    Array.setFloat(array, index, ((Number) value).floatValue());
        else if (compType == boolean.class)  Array.setBoolean(array, index, (Boolean) value);
        else if (compType == byte.class)     Array.setByte(array, index, ((Number) value).byteValue());
        else if (compType == char.class)     Array.setChar(array, index, (Character) value);
        else if (compType == short.class)    Array.setShort(array, index, ((Number) value).shortValue());
    }

    
    private static <T> T listToArray(List<?> list, Class<T> arrayClass) {
        Class<?> compType = arrayClass.getComponentType();
        Object array = Array.newInstance(compType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Object elem = list.get(i);
            if (elem instanceof Map && !Map.class.isAssignableFrom(compType) && compType != Object.class) {
                elem = mapToObject((Map<String, Object>) elem, compType);
            }
            if (compType.isPrimitive()) {
                setPrimitiveArrayElement(array, i, elem, compType);
            } else {
                Array.set(array, i, elem);
            }
        }
        return (T) array;
    }
}