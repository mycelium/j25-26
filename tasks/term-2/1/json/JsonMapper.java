package json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class JsonMapper {

   
    public String toJson(Object obj) {
        if (obj == null) return "null";

        Class<?> clazz = obj.getClass();

        if (clazz == String.class || clazz == Character.class) {
            return "\"" + escapeString(obj.toString()) + "\"";
        }
        if (Number.class.isAssignableFrom(clazz) || clazz == Boolean.class || clazz.isPrimitive()) {
            return obj.toString();
        }
        if (clazz.isArray()) return arrayToJson(obj);
        if (Collection.class.isAssignableFrom(clazz)) return collectionToJson((Collection<?>) obj);
        if (Map.class.isAssignableFrom(clazz)) return mapToJson((Map<?, ?>) obj);

        return objectToJson(obj);
    }

    
    public Object fromJson(String json) {
        return parseValue(new StringIterator(json.trim()));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fromJsonAsMap(String json) {
        Object result = fromJson(json);
        if (result instanceof Map) return (Map<String, Object>) result;
        throw new IllegalArgumentException("JSON root is not an object");
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Class<T> clazz) {
        Object parsedValue = fromJson(json);
        return (T) mapToTargetType(parsedValue, clazz);
    }


    @SuppressWarnings("unchecked")
    private Object mapToTargetType(Object jsonValue, Type targetType) {
        if (jsonValue == null) return null;

        Class<?> clazz;
        Type[] typeArguments = null;


        if (targetType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) targetType;
            clazz = (Class<?>) pt.getRawType();
            typeArguments = pt.getActualTypeArguments();
        } else if (targetType instanceof Class) {
            clazz = (Class<?>) targetType;
        } else {
            return jsonValue;
        }

     
        if (clazz.isAssignableFrom(jsonValue.getClass())) {
            return jsonValue;
        }

  
        if (jsonValue instanceof Double) {
            Double num = (Double) jsonValue;
            if (clazz == int.class || clazz == Integer.class) return num.intValue();
            if (clazz == long.class || clazz == Long.class) return num.longValue();
            if (clazz == float.class || clazz == Float.class) return num.floatValue();
            return num;
        }


        if (clazz.isArray() && jsonValue instanceof List) {
            List<?> list = (List<?>) jsonValue;
            Class<?> componentType = clazz.getComponentType();
            Object array = Array.newInstance(componentType, list.size());
            for (int i = 0; i < list.size(); i++) {
    
                Array.set(array, i, mapToTargetType(list.get(i), componentType));
            }
            return array;
        }


        if (Collection.class.isAssignableFrom(clazz) && jsonValue instanceof List) {
            List<?> jsonList = (List<?>) jsonValue;
            Collection<Object> collection;

    
            if (clazz.isInterface()) {
                if (Set.class.isAssignableFrom(clazz)) collection = new HashSet<>();
                else collection = new ArrayList<>();
            } else {
                try {
                    collection = (Collection<Object>) clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    collection = new ArrayList<>();
                }
            }

       
            Type elementType = (typeArguments != null && typeArguments.length > 0) ? typeArguments[0] : Object.class;

            for (Object item : jsonList) {
                collection.add(mapToTargetType(item, elementType));
            }
            return collection;
        }

  
        if (jsonValue instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) jsonValue;
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    if (map.containsKey(fieldName)) {
                        Object fieldValue = map.get(fieldName);
                      
                        field.set(instance, mapToTargetType(fieldValue, field.getGenericType()));
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate: " + clazz.getName(), e);
            }
        }

        return jsonValue;
    }

 
    private Object parseValue(StringIterator it) {
        it.skipWhitespace();
        if (!it.hasNext()) return null;
        char c = it.peek();

        if (c == '{') return parseObject(it);
        if (c == '[') return parseArray(it);
        if (c == '"') return parseString(it);
        if (c == 't' || c == 'f') return parseBoolean(it);
        if (c == 'n') return parseNull(it);
        if (Character.isDigit(c) || c == '-') return parseNumber(it);

        throw new IllegalArgumentException("Unexpected character: " + c);
    }

    private Map<String, Object> parseObject(StringIterator it) {
        Map<String, Object> map = new HashMap<>();
        it.next();
        it.skipWhitespace();
        if (it.peek() == '}') { it.next(); return map; }

        while (true) {
            it.skipWhitespace();
            String key = parseString(it);
            it.skipWhitespace();
            if (it.next() != ':') throw new IllegalArgumentException("Expected ':'");
            it.skipWhitespace();

            map.put(key, parseValue(it));

            it.skipWhitespace();
            char c = it.next();
            if (c == '}') break;
            if (c != ',') throw new IllegalArgumentException("Expected ',' or '}'");
        }
        return map;
    }

    private List<Object> parseArray(StringIterator it) {
        List<Object> list = new ArrayList<>();
        it.next(); 
        it.skipWhitespace();
        if (it.peek() == ']') { it.next(); return list; }

        while (true) {
            list.add(parseValue(it));
            it.skipWhitespace();
            char c = it.next();
            if (c == ']') break;
            if (c != ',') throw new IllegalArgumentException("Expected ',' or ']'");
        }
        return list;
    }

    private String parseString(StringIterator it) {
        it.next(); 
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = it.next();
            if (c == '"') break;
  
            if (c == '\\') {
                char escaped = it.next();
                if (escaped == '"') sb.append('"');
                else if (escaped == '\\') sb.append('\\');
                else sb.append('\\').append(escaped);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Double parseNumber(StringIterator it) {
        StringBuilder sb = new StringBuilder();
        while (it.hasNext() && (Character.isDigit(it.peek()) || it.peek() == '.' || it.peek() == '-' || it.peek() == 'e' || it.peek() == 'E')) {
            sb.append(it.next());
        }
        return Double.parseDouble(sb.toString());
    }

    private Boolean parseBoolean(StringIterator it) {
        return it.peek() == 't' ? (it.match("true") ? true : null) : (it.match("false") ? false : null);
    }

    private Object parseNull(StringIterator it) {
        it.match("null");
        return null;
    }

    private static class StringIterator {
        private final String str;
        private int index = 0;

        StringIterator(String str) { this.str = str; }

        boolean hasNext() { return index < str.length(); }
        char peek() { return str.charAt(index); }
        char next() { return str.charAt(index++); }

        void skipWhitespace() {
            while (hasNext() && Character.isWhitespace(peek())) index++;
        }

        boolean match(String expected) {
            for (int i = 0; i < expected.length(); i++) {
                if (!hasNext() || next() != expected.charAt(i)) throw new IllegalArgumentException("Expected '" + expected + "'");
            }
            return true;
        }
    }

  
    private String objectToJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    if (!first) sb.append(",");
                    sb.append("\"").append(field.getName()).append("\":").append(toJson(value));
                    first = false;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field", e);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String arrayToJson(Object array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            sb.append(toJson(Array.get(array, i)));
            if (i < length - 1) sb.append(",");
        }
        sb.append("}");
        return sb.toString().replace("]", "").replace("}", "]") + (length == 0 ? "]" : ""); // Фикс запятых для пустых массивов
    }

    private String collectionToJson(Collection<?> collection) {
        return arrayToJson(collection.toArray());
    }

    private String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey().toString()).append("\":").append(toJson(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeString(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}