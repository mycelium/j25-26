package jsonparser;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.IdentityHashMap;
import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;

public class JsonLib {
    
    public static String stringify(Object obj) {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        return serialize(obj, visited);
    }
    
    public static Object read(String json) {
        return new Parser(json).parse();
    }
    
    public static Map<String, Object> readAsMap(String json) {
        Object result = read(json);
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }
        throw new IllegalArgumentException("Not a JSON object");
    }
    
    private static String serialize(Object obj, Set<Object> visited) {
        if (obj == null) return "null";
        if (visited.contains(obj)) return "null";
        Class<?> clazz = obj.getClass();
        
        if (clazz == String.class) {
            return "\"" + escape(obj.toString()) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        visited.add(obj);
        String result;
        if (clazz.isArray()) {
            result = arrayToJson(obj, visited);
        } else if (obj instanceof Collection) {
            result = collectionToJson((Collection<?>) obj, visited);
        } else if (obj instanceof Map) {
            result = mapToJson((Map<?, ?>) obj, visited);
        } else {
            result = objectToJson(obj, visited);
        }
        visited.remove(obj);
        return result;
    }
    
    private static String arrayToJson(Object array, Set<Object> visited) {
        int len = Array.getLength(array);
        StringJoiner sj = new StringJoiner(",", "[", "]");
        for (int i = 0; i < len; i++) {
            sj.add(serialize(Array.get(array, i), visited));
        }
        return sj.toString();
    }
    
    private static String collectionToJson(Collection<?> coll, Set<Object> visited) {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        for (Object item : coll) {
            sj.add(serialize(item, visited));
        }
        return sj.toString();
    }
    
    private static String mapToJson(Map<?, ?> map, Set<Object> visited) {
        StringJoiner sj = new StringJoiner(",", "{", "}");
        for (Map.Entry<?, ?> e : map.entrySet()) {
            String key = "\"" + escape(String.valueOf(e.getKey())) + "\"";
            sj.add(key + ":" + serialize(e.getValue(), visited));
        }
        return sj.toString();
    }
    
    private static String objectToJson(Object obj, Set<Object> visited) {
        StringJoiner sj = new StringJoiner(",", "{", "}");
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            if (Modifier.isTransient(f.getModifiers())) continue;
            f.setAccessible(true);
            try {
                sj.add("\"" + f.getName() + "\":" + serialize(f.get(obj), visited));
            } catch (IllegalAccessException e) {
            }
        }
        return sj.toString();
    }
    
    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    
    private static class Parser {
        private final String src;
        private int pos;
        
        Parser(String src) { this.src = src; this.pos = 0; }
        
        Object parse() {
            skipSpace();
            if (pos >= src.length()) return null;
            char c = src.charAt(pos);
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
            skipSpace();
            while (src.charAt(pos) != '}') {
                String key = parseString();
                skipSpace();
                consume(":");
                Object val = parse();
                map.put(key, val);
                skipSpace();
                if (src.charAt(pos) == ',') {
                    consume(",");
                    skipSpace();
                }
            }
            consume("}");
            return map;
        }
        
        private java.util.List<Object> parseArray() {
            java.util.List<Object> list = new ArrayList<>();
            consume("[");
            skipSpace();
            while (src.charAt(pos) != ']') {
                list.add(parse());
                skipSpace();
                if (src.charAt(pos) == ',') {
                    consume(",");
                    skipSpace();
                }
            }
            consume("]");
            return list;
        }
        
        private String parseString() {
            consume("\"");
            StringBuilder sb = new StringBuilder();
            while (src.charAt(pos) != '"') {
                char c = src.charAt(pos++);
                if (c == '\\') {
                    c = src.charAt(pos++);
                }
                sb.append(c);
            }
            consume("\"");
            return sb.toString();
        }
        
        private Number parseNumber() {
            int start = pos;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.' || src.charAt(pos) == '-')) {
                pos++;
            }
            String num = src.substring(start, pos);
            if (num.contains(".")) {
                return Double.parseDouble(num);
            }
            long l = Long.parseLong(num);
            if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                return (int) l;
            }
            return l;
        }
        
        private Boolean parseBoolean() {
            if (src.startsWith("true", pos)) {
                pos += 4;
                return true;
            }
            consume("false");
            return false;
        }
        
        private void skipSpace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }
        
        private void consume(String s) {
            for (char c : s.toCharArray()) {
                if (pos >= src.length() || src.charAt(pos++) != c) {
                    throw new RuntimeException("Expected " + c);
                }
            }
        }
    }
}
