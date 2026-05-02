import java.lang.reflect.*;
import java.util.*;

class Address {
    String city;
    public String toString() { return "Address{city='" + city + "'}"; }
}

class User {
    String name;
    int age;
    Integer rank;
    String[] tags;
    List<Integer> scores;
    Address address;
    
    public String toString() {
        return "User{name='" + name + "', age=" + age + ", rank=" + rank + 
               ", tags=" + Arrays.toString(tags) + ", scores=" + scores + 
               ", address=" + address + "}";
    }
}

class SimpleJson {

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + ((String)obj).replace("\"", "\\\"") + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        
        if (obj.getClass().isArray()) {
            StringBuilder sb = new StringBuilder("[");
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(Array.get(obj, i)));
            }
            return sb.append("]").toString();
        }
        
        if (obj instanceof Collection) {
            StringBuilder sb = new StringBuilder("[");
            Iterator<?> it = ((Collection<?>) obj).iterator();
            while (it.hasNext()) {
                sb.append(toJson(it.next()));
                if (it.hasNext()) sb.append(",");
            }
            return sb.append("]").toString();
        }

        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            try {
                if (!first) sb.append(",");
                sb.append("\"").append(f.getName()).append("\":").append(toJson(f.get(obj)));
                first = false;
            } catch (Exception e) { throw new RuntimeException(e); }
        }
        return sb.append("}").toString();
    }

    public static Map<String, Object> fromJson(String json) {
        return (Map<String, Object>) new Parser(json.trim()).parse();
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return mapToObj(fromJson(json), clazz);
    }


    @SuppressWarnings("unchecked")
    private static <T> T mapToObj(Map<String, Object> map, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> e : map.entrySet()) {
                try {
                    Field f = clazz.getDeclaredField(e.getKey());
                    f.setAccessible(true);
                    f.set(obj, convertType(e.getValue(), f.getType()));
                } catch (NoSuchFieldException ignored) {}
            }
            return obj;
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }

    private static Object convertType(Object val, Class<?> type) {
        if (val == null) return null;
        
        // Вложенный объект
        if (val instanceof Map && !type.isPrimitive() && !type.getName().startsWith("java")) {
            return mapToObj((Map<String, Object>) val, type);
        }
        
        if (val instanceof List && type.isArray()) {
            List<?> list = (List<?>) val;
            Object arr = Array.newInstance(type.getComponentType(), list.size());
            for (int i = 0; i < list.size(); i++) 
                Array.set(arr, i, convertType(list.get(i), type.getComponentType()));
            return arr;
        }
        
        if (val instanceof List && Collection.class.isAssignableFrom(type)) {
            return new ArrayList<>((List<?>) val); 
        }

        if (val instanceof Number) {
            Number n = (Number) val;
            if (type == int.class || type == Integer.class) return n.intValue();
            if (type == long.class || type == Long.class) return n.longValue();
            if (type == double.class || type == Double.class) return n.doubleValue();
            if (type == float.class || type == Float.class) return n.floatValue();
        }
        
        return val;
    }

    static class Parser {
        private final String s;
        private int pos = 0;

        Parser(String s) { this.s = s; }

        Object parse() {
            skipWS();
            if (pos >= s.length()) return null;
            char c = s.charAt(pos);
            if (c == '{') return parseObj();
            if (c == '[') return parseArr();
            if (c == '"') return parseStr();
            if (c == 't' || c == 'f') return parseBool();
            if (c == 'n') { pos += 4; return null; }
            return parseNum();
        }

        private Map<String, Object> parseObj() {
            Map<String, Object> map = new LinkedHashMap<>();
            consume('{');
            if (peek() == '}') { consume('}'); return map; }
            while (true) {
                skipWS();
                String key = parseStr();
                consume(':');
                map.put(key, parse());
                skipWS();
                if (peek() == ',') consume(','); else break;
            }
            consume('}');
            return map;
        }

        private List<Object> parseArr() {
            List<Object> list = new ArrayList<>();
            consume('[');
            if (peek() == ']') { consume(']'); return list; }
            while (true) {
                list.add(parse());
                skipWS();
                if (peek() == ',') consume(','); else break;
            }
            consume(']');
            return list;
        }

        private String parseStr() {
            consume('"');
            StringBuilder sb = new StringBuilder();
            while (pos < s.length() && s.charAt(pos) != '"') {
                if (s.charAt(pos) == '\\') {
                    pos++;
                    if (pos < s.length()) sb.append(s.charAt(pos));
                } else {
                    sb.append(s.charAt(pos));
                }
                pos++;
            }
            consume('"');
            return sb.toString();
        }

        private Number parseNum() {
            int start = pos;
            while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.' || s.charAt(pos) == '-')) pos++;
            String sub = s.substring(start, pos);
            return sub.contains(".") ? Double.parseDouble(sub) : Long.parseLong(sub);
        }

        private Boolean parseBool() {
            if (s.startsWith("true", pos)) { pos += 4; return true; }
            pos += 5; return false;
        }

        private void skipWS() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }
        
        private char peek() { skipWS(); return pos < s.length() ? s.charAt(pos) : 0; }
        
        private void consume(char exp) {
            skipWS();
            if (pos < s.length() && s.charAt(pos) == exp) pos++;
            else throw new RuntimeException("Expected " + exp);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        User user = new User();
        user.name = "Anna";
        user.age = 21;
        user.rank = null;
        user.tags = new String[]{"java", "lab"};
        user.scores = new ArrayList<>(Arrays.asList(10, 20));
        user.address = new Address();
        user.address.city = "Moscow";

        String json = SimpleJson.toJson(user);
        System.out.println("JSON: " + json);

        Map<String, Object> map = SimpleJson.fromJson(json);
        System.out.println("Map: " + map);

        User u2 = SimpleJson.fromJson(json, User.class);
        System.out.println("Object: " + u2);
        
        System.out.println(u2.scores.size() == 2 ? "Успех!" : "Ошибка");
    }
}
