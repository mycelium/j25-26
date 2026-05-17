package json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class JsonMapper {

    public String toJson(Object obj) {
        if (obj == null) return "null";

        var clazz = obj.getClass();

        if (clazz == String.class || clazz == Character.class)
            return "\"" + escapeString(obj.toString()) + "\"";
        if (Number.class.isAssignableFrom(clazz) || clazz == Boolean.class || clazz.isPrimitive())
            return obj.toString();
        if (clazz.isArray()) return arrayToJson(obj);
        if (Collection.class.isAssignableFrom(clazz)) return collectionToJson((Collection<?>) obj);
        if (Map.class.isAssignableFrom(clazz)) return mapToJson((Map<?, ?>) obj);

        return objectToJson(obj);
    }

    public Object fromJson(String json) {
        return parseValue(new StringIterator(json.trim()));
    }

    public Map<String, Object> fromJsonAsMap(String json) {
        var result = fromJson(json);
        if (result instanceof Map<?, ?> m) {
            @SuppressWarnings("unchecked")
            var typed = (Map<String, Object>) m;
            return typed;
        }
        throw new IllegalArgumentException("JSON root is not an object");
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        var parsedValue = fromJson(json);
        @SuppressWarnings("unchecked")
        var result = (T) mapToTargetType(parsedValue, clazz);
        return result;
    }

    private Object mapToTargetType(Object jsonValue, Type targetType) {
        if (jsonValue == null) return null;

        Class<?> clazz;
        Type[] typeArguments = null;

        if (targetType instanceof ParameterizedType pt) {
            clazz = (Class<?>) pt.getRawType();
            typeArguments = pt.getActualTypeArguments();
        } else if (targetType instanceof Class<?> c) {
            clazz = c;
        } else {
            return jsonValue;
        }

        if (clazz.isAssignableFrom(jsonValue.getClass())) return jsonValue;

        if (jsonValue instanceof Double num) {
            return switch (clazz.getName()) {
                case "int", "java.lang.Integer" -> num.intValue();
                case "long", "java.lang.Long" -> num.longValue();
                case "float", "java.lang.Float" -> num.floatValue();
                default -> num;
            };
        }

        if (clazz.isArray() && jsonValue instanceof List<?> list) {
            var componentType = clazz.getComponentType();
            var array = Array.newInstance(componentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, mapToTargetType(list.get(i), componentType));
            }
            return array;
        }

        if (Collection.class.isAssignableFrom(clazz) && jsonValue instanceof List<?> jsonList) {
            Collection<Object> collection;
            if (clazz.isInterface()) {
                collection = Set.class.isAssignableFrom(clazz) ? new HashSet<>() : new ArrayList<>();
            } else {
                try {
                    @SuppressWarnings("unchecked")
                    var c = (Collection<Object>) clazz.getDeclaredConstructor().newInstance();
                    collection = c;
                } catch (Exception e) {
                    collection = new ArrayList<>();
                }
            }
            var elementType = (typeArguments != null && typeArguments.length > 0) ? typeArguments[0] : Object.class;
            for (var item : jsonList) collection.add(mapToTargetType(item, elementType));
            return collection;
        }

        if (jsonValue instanceof Map<?, ?> rawMap) {
            @SuppressWarnings("unchecked")
            var map = (Map<String, Object>) rawMap;
            try {
                var instance = clazz.getDeclaredConstructor().newInstance();
                for (var field : getAllFields(clazz)) {
                    field.setAccessible(true);
                    if (map.containsKey(field.getName())) {
                        field.set(instance, mapToTargetType(map.get(field.getName()), field.getGenericType()));
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate: " + clazz.getName(), e);
            }
        }

        return jsonValue;
    }

    /** Collects declared fields from the class and all its superclasses. */
    private List<Field> getAllFields(Class<?> clazz) {
        var fields = new ArrayList<Field>();
        for (var c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    private Object parseValue(StringIterator it) {
        it.skipWhitespace();
        if (!it.hasNext()) return null;
        return switch (it.peek()) {
            case '{' -> parseObject(it);
            case '[' -> parseArray(it);
            case '"' -> parseString(it);
            case 't', 'f' -> parseBoolean(it);
            case 'n' -> parseNull(it);
            default -> parseNumber(it);
        };
    }

    private Map<String, Object> parseObject(StringIterator it) {
        var map = new LinkedHashMap<String, Object>();
        it.next();
        it.skipWhitespace();
        if (it.peek() == '}') { it.next(); return map; }

        while (true) {
            it.skipWhitespace();
            var key = parseString(it);
            it.skipWhitespace();
            if (it.next() != ':') throw new IllegalArgumentException("Expected ':'");
            it.skipWhitespace();
            map.put(key, parseValue(it));
            it.skipWhitespace();
            var c = it.next();
            if (c == '}') break;
            if (c != ',') throw new IllegalArgumentException("Expected ',' or '}'");
        }
        return map;
    }

    private List<Object> parseArray(StringIterator it) {
        var list = new ArrayList<>();
        it.next();
        it.skipWhitespace();
        if (it.peek() == ']') { it.next(); return list; }

        while (true) {
            list.add(parseValue(it));
            it.skipWhitespace();
            var c = it.next();
            if (c == ']') break;
            if (c != ',') throw new IllegalArgumentException("Expected ',' or ']'");
        }
        return list;
    }

    private String parseString(StringIterator it) {
        it.next(); // consume opening "
        var sb = new StringBuilder();
        while (true) {
            char c = it.next();
            if (c == '"') break;
            if (c == '\\') {
                char escaped = it.next();
                switch (escaped) {
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/'  -> sb.append('/');
                    case 'n'  -> sb.append('\n');
                    case 't'  -> sb.append('\t');
                    case 'r'  -> sb.append('\r');
                    case 'b'  -> sb.append('\b');
                    case 'f'  -> sb.append('\f');
                    case 'u'  -> {
                        var hex = new char[]{it.next(), it.next(), it.next(), it.next()};
                        sb.append((char) Integer.parseInt(new String(hex), 16));
                    }
                    default   -> sb.append('\\').append(escaped);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Double parseNumber(StringIterator it) {
        var sb = new StringBuilder();
        while (it.hasNext() && "0123456789.+-eE".indexOf(it.peek()) >= 0) {
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
                if (!hasNext() || next() != expected.charAt(i))
                    throw new IllegalArgumentException("Expected '" + expected + "'");
            }
            return true;
        }
    }

    private String objectToJson(Object obj) {
        var fields = getAllFields(obj.getClass());
        var entries = fields.stream()
            .map(field -> {
                field.setAccessible(true);
                try {
                    return "\"" + field.getName() + "\":" + toJson(field.get(obj));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field", e);
                }
            })
            .collect(Collectors.joining(","));
        return "{" + entries + "}";
    }

    private String arrayToJson(Object array) {
        int length = Array.getLength(array);
        var elements = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) elements.add(toJson(Array.get(array, i)));
        return "[" + String.join(",", elements) + "]";
    }

    private String collectionToJson(Collection<?> collection) {
        return "[" + collection.stream().map(this::toJson).collect(Collectors.joining(",")) + "]";
    }

    private String mapToJson(Map<?, ?> map) {
        var entries = map.entrySet().stream()
            .map(e -> "\"" + e.getKey() + "\":" + toJson(e.getValue()))
            .collect(Collectors.joining(","));
        return "{" + entries + "}";
    }

    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f");
    }
}
