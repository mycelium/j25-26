import java.lang.reflect.*;
import java.util.*;

public class SimpleJson {

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escape((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        
        if (obj instanceof Collection<?>) {
            return collectionToJson((Collection<?>) obj);
        }
        
        if (obj.getClass().isArray()) {
            return arrayToJson(obj);
        }

        if (obj instanceof Map<?, ?>) {
            return mapToJson((Map<?, ?>) obj);
        }

        return objectToJson(obj);
    }

    public static Map<String, Object> fromJson(String json) {
        return (Map<String, Object>) new Parser(json).parse();
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        Map<String, Object> map = fromJson(json);
        return mapToClass(map, clazz);
    }


    private static String collectionToJson(Collection<?> col) {
        StringBuilder sb = new StringBuilder("[");
        Iterator<?> it = col.iterator();
        while (it.hasNext()) {
            sb.append(toJson(it.next()));
            if (it.hasNext()) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private static String arrayToJson(Object array) {
        StringBuilder sb = new StringBuilder("[");
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            sb.append(toJson(Array.get(array, i)));
            if (i < length - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<?, ?> entry = it.next();
            sb.append("\"").append(entry.getKey()).append("\":").append(toJson(entry.getValue()));
            if (it.hasNext()) sb.append(",");
        }
        return sb.append("}").toString();
    }

    private static String objectToJson(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;
        try {
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                if (!first) sb.append(",");
                sb.append("\"").append(field.getName()).append("\":").append(toJson(field.get(obj)));
                first = false;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return sb.append("}").toString();
    }

    private static String escape(String s) {
        return s.replace("\"", "\\\"");
    }


    private static class Parser {
        private final String src;
        private int pos = 0;

        Parser(String src) { this.src = src.trim(); }

        Object parse() {
            skipWhitespace();
            char c = peek();
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') { consume("null"); return null; }
            return parseNumber();
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            consume("{");
            while (peek() != '}') {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                consume(":");
                map.put(key, parse());
                skipWhitespace();
                if (peek() == ',') consume(",");
            }
            consume("}");
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            consume("[");
            while (peek() != ']') {
                list.add(parse());
                skipWhitespace();
                if (peek() == ',') consume(",");
            }
            consume("]");
            return list;
        }

        private String parseString() {
            consume("\"");
            StringBuilder sb = new StringBuilder();
            while (peek() != '"') sb.append(next());
            consume("\"");
            return sb.toString();
        }

        private Number parseNumber() {
            StringBuilder sb = new StringBuilder();
            while (Character.isDigit(peek()) || peek() == '.' || peek() == '-') sb.append(next());
            String val = sb.toString();
            if (val.contains(".")) return Double.parseDouble(val);
            return Integer.parseInt(val);
        }

        private Boolean parseBoolean() {
            if (peek() == 't') { consume("true"); return true; }
            consume("false"); return false;
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
        }

        private char peek() { return pos < src.length() ? src.charAt(pos) : 0; }
        private char next() { return src.charAt(pos++); }
        private void consume(String s) {
            for (char c : s.toCharArray()) {
                if (next() != c) throw new RuntimeException("Expected " + c);
            }
        }
    }


    private static <T> T mapToClass(Map<String, Object> map, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                try {
                    Field field = clazz.getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    Object value = entry.getValue();
                    
                    if (value instanceof Map && !Map.class.isAssignableFrom(field.getType())) {
                        value = mapToClass((Map<String, Object>) value, field.getType());
                    } else if (value instanceof List) {
                        if (field.getType().isArray()) {
                            value = listToArray((List<?>) value, field.getType().getComponentType());
                        } else if (Collection.class.isAssignableFrom(field.getType())) {
                            value = listToCollection((List<?>) value, field.getType());
                        }
                    }
                    field.set(instance, castValue(value, field.getType()));
                } catch (NoSuchFieldException e) {
                    
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Mapping error", e);
        }
    }

    private static Object listToArray(List<?> list, Class<?> componentType) {
        Object array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, castValue(list.get(i), componentType));
        }
        return array;
    }

    private static Object castValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isPrimitive()) {
            if (targetType == int.class) return ((Number) value).intValue();
            if (targetType == double.class) return ((Number) value).doubleValue();
            if (targetType == boolean.class) return (Boolean) value;
        }
        return value;
    }
    private static Object listToCollection(List<?> list, Class<?> targetType) throws Exception {
        Collection<Object> collection;
        if (targetType.isInterface()) {
            if (Set.class.isAssignableFrom(targetType)) collection = new HashSet<>();
            else collection = new ArrayList<>(); 
        } else {
            collection = (Collection<Object>) targetType.getDeclaredConstructor().newInstance();
        }
        collection.addAll(list);
        return collection;
    }
}
