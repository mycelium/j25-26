package json;

import java.util.*;
import java.lang.reflect.*;

public class MyJsonLibrary {

    public static Object parse(String json){ //json to Object
        if (json == null || json.isEmpty()) {
            return null;
        }
        json = json.trim();
        if (json.startsWith("{")) {
            return parseObject(json);
        } else if (json.startsWith("[")) {
            return parseArray(json);
        } else if (json.startsWith("\"")) {
            return parseString(json);
        } else if (json.equals("true") || json.equals("false")) {
            return Boolean.parseBoolean(json); //стандартный метод
        } else if (json.equals("null")) {
            return null;
        } else {
            return Double.parseDouble(json); //стандартный метод
        }
    }

    private static Map<String, Object> parseObject(String json) {
        var res = new HashMap<String, Object>();
        json = json.trim();
        json = delBrackets(json, '{', '}');
        if (json.isEmpty()){
            return res;
        }
        var pairs = splitTopLevel(json, ',');
        for (var pair : pairs) {
            var kv = splitTopLevel(pair, ':');
            var key = parseString(kv.get(0).trim());
            var valuePart = kv.get(1).trim();
            var value = parse(valuePart);
            res.put(key, value);
        }
        return res;
    }

    private static List<Object> parseArray(String json) {
        var res = new ArrayList<Object>();
        json = delBrackets(json, '[', ']');
        if (json.isEmpty()){
            return res;
        }
        var elements = splitTopLevel(json, ',');
        for (var element : elements) {
            res.add(parse(element.trim()));
        }
        return res;
    }

private static String parseString(String json) {
    String str = delBrackets(json, '"', '"');
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
        char c = str.charAt(i);
        if (c == '\\') {
            char next = str.charAt(++i);
            char escaped = switch (next) {
                case 'b' -> '\b';
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 't' -> '\t';
                case '"' -> '"';
                case '\\' -> '\\';
                case 'f' -> '\f';
                case '/' -> '/';
                default -> throw new RuntimeException("Invalid escape sequence: \\" + next);
            };
            result.append(escaped);
        } else {
            result.append(c);
        }
    }

    return result.toString();
}

    private static String delBrackets(String json, char br, char closeBr){
        json = json.trim();
        if (json.startsWith(String.valueOf(br)) && json.endsWith(String.valueOf(closeBr))) {
            return json.substring(1, json.length() - 1).trim();
        }
        return json;
    }

    private static List<String> splitTopLevel(String text, char delimiter) {
        List<String> parts = new ArrayList<>();
        int objectLevel = 0;
        int arrayLevel = 0;
        boolean inString = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"') {
                int backslashes = 0;
                int j = i - 1;
                while (j >= 0 && text.charAt(j) == '\\') {
                    backslashes++;
                    j--;
                }
                if (backslashes % 2 == 0) { // чётное число слешей = " не экранирована
                    inString = !inString;
                }
            }
            if (!inString) {
                if (c == '{') objectLevel++;
                if (c == '}') objectLevel--;
                if (c == '[') arrayLevel++;
                if (c == ']') arrayLevel--;
            }
            if (!inString && c == delimiter && objectLevel == 0 && arrayLevel == 0) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        parts.add(current.toString());
        return parts;
    }

    public static Map<String, Object> parseToMap(String json){
        Object obj = parse(json);
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        } else {
            throw new RuntimeException("JSON is not an object");
        }
    }

    public static <T> T parse(String json, Class<T> clas){
        try{
            Map<String, Object> map = parseToMap(json);
            T instance = clas.getDeclaredConstructor().newInstance();
            for(Field field : clas.getDeclaredFields()){
                field.setAccessible(true);
                Object value = map.get(field.getName());
                if(value == null) continue;
                field.set(instance, convertValue(value, field.getGenericType()));
            }
            return instance;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private static Object convertValue(Object value, Type type) throws Exception {
        if (value == null) {
            return null;
        }
        if (type instanceof Class<?> raw) {
            if (raw == String.class) {
                return value.toString();
            }
            if ((raw == int.class || raw == Integer.class) && value instanceof Number) {
                return ((Number) value).intValue();
            }
            if ((raw == long.class || raw == Long.class) && value instanceof Number) {
                return ((Number) value).longValue();
            }
            if ((raw == float.class || raw == Float.class) && value instanceof Number) {
                return ((Number) value).floatValue();
            }
            if ((raw == double.class || raw == Double.class) && value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if ((raw == boolean.class || raw == Boolean.class) && value instanceof Boolean) {
                return value;
            }
            if ((raw == byte.class || raw == Byte.class) && value instanceof Number) {
                return ((Number) value).byteValue();
            }
            if ((raw == short.class || raw == Short.class) && value instanceof Number) {
                return ((Number) value).shortValue();
            }
            if ((raw == char.class || raw == Character.class) && value instanceof String) {
                return ((String) value).charAt(0);
            }
            if (raw.isArray() && value instanceof List<?>) {
                var list = (List<?>) value;
                var componentType = raw.getComponentType();
                var array = Array.newInstance(componentType, list.size());
                for (int i = 0; i < list.size(); i++) {
                    Array.set(array, i, convertValue(list.get(i), componentType));
                }
                return array;
            }
            if (Collection.class.isAssignableFrom(raw) && value instanceof List<?>) {
                return convertCollection((List<?>) value, raw, Object.class);
            }
            if (Map.class.isAssignableFrom(raw) && value instanceof Map<?, ?>) {
                return convertMap((Map<?, ?>) value, raw, Object.class, Object.class);
            }
            if (value instanceof Map<?, ?>) {
                return parse(toJson(value), raw);
            }
            return value;
        }
        if (type instanceof ParameterizedType parameterized) {
            var raw = (Class<?>) parameterized.getRawType();
            var arguments = parameterized.getActualTypeArguments();
            if (Collection.class.isAssignableFrom(raw) && value instanceof List<?>) {
                return convertCollection((List<?>) value, raw, arguments[0]);
            }
            if (Map.class.isAssignableFrom(raw) && value instanceof Map<?, ?>) {
                return convertMap((Map<?, ?>) value, raw, arguments[0], arguments[1]);
            }
            return convertValue(value, raw);
        }
        return value;
    }

    private static Object convertCollection(List<?> source, Class<?> target, Type elementType) throws Exception {
        Collection<Object> result;
        if (Set.class.isAssignableFrom(target)) {
            result = new LinkedHashSet<>();
        } else {
            result = new ArrayList<>();
        }
        for (var item : source) {
            result.add(convertValue(item, elementType));
        }
        return result;
    }

    private static Object convertMap(Map<?, ?> source, Class<?> target, Type keyType, Type valueType) throws Exception {
        Map<Object, Object> result;
        if (EnumMap.class.isAssignableFrom(target)) {
            result = new LinkedHashMap<>();
        } else {
            result = new LinkedHashMap<>();
        }
        for (var entry : source.entrySet()) {
            var key = convertValue(entry.getKey(), keyType);
            var value = convertValue(entry.getValue(), valueType);
            result.put(key, value);
        }
        return result;
    }

public static String toJson(Object obj) {
    if (obj == null) {
        return "null";
    }
    if (obj instanceof String) {
        String s = (String) obj;
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String escaped = switch (c) {
                case '"' -> "\\\"";
                case '\\' -> "\\\\";
                case '\b' -> "\\b";
                case '\f' -> "\\f";
                case '\n' -> "\\n";
                case '\r' -> "\\r";
                case '\t' -> "\\t";
                default -> String.valueOf(c);
            };
            sb.append(escaped);
        }
        sb.append("\"");
        return sb.toString();
    }
    if (obj instanceof Boolean) {
        return obj.toString();
    }
    if (obj instanceof Number) {
        Number num = (Number) obj;
        if (num instanceof Double || num instanceof Float) {
            double d = num.doubleValue();
            if (d == (long) d) {
                return String.valueOf((long) d);
            }
        }
        return num.toString();
    }

    if (obj instanceof Map) {
        Map<?, ?> map = (Map<?, ?>) obj;
        StringJoiner sj = new StringJoiner(",", "{", "}");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sj.add(toJson(String.valueOf(entry.getKey())) + ":" + toJson(entry.getValue()));
        }
        return sj.toString();
    }

    if (obj instanceof Collection) {
        Collection<?> col = (Collection<?>) obj;
        StringJoiner sj = new StringJoiner(",", "[", "]");
        for (Object item : col) {
            sj.add(toJson(item));
        }
        return sj.toString();
    }

    if (obj.getClass().isArray()) {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        int length = java.lang.reflect.Array.getLength(obj);
        for (int i = 0; i < length; i++) {
            sj.add(toJson(java.lang.reflect.Array.get(obj, i)));
        }
        return sj.toString();
    }

    try {
        StringJoiner sj = new StringJoiner(",", "{", "}");
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(obj);
            sj.add(toJson(field.getName()) + ":" + toJson(value));
        }
        return sj.toString();

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
}
