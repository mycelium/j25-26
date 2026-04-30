import java.lang.reflect.*;
import java.util.*;

public class JsonParser {

    public static Map<String, Object> parseToMap(String json) {
        return (Map<String, Object>) parseValue(json.trim());
    }

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        return serializeValue(obj);
    }

    private static Object parseValue(String json) {
        json = json.trim();
        if (json.startsWith("{")) return parseObject(json);
        if (json.startsWith("[")) return parseArray(json);
        if (json.startsWith("\"")) return json.substring(1, json.length() - 1);
        if (json.equals("null")) return null;
        if (json.equals("true") || json.equals("false")) return Boolean.parseBoolean(json);
        if (json.contains(".")) return Double.parseDouble(json);
        return Long.parseLong(json);
    }

    private static Map<String, Object> parseObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return map;

        for (String pair : split(json)) {
            int colon = findColon(pair);
            if (colon == -1) continue;
            String key = pair.substring(0, colon).trim().replaceAll("^\"|\"$", "");
            String val = pair.substring(colon + 1).trim();
            map.put(key, parseValue(val));
        }
        return map;
    }

    private static List<Object> parseArray(String json) {
        List<Object> list = new ArrayList<>();
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return list;
        for (String el : split(json)) list.add(parseValue(el.trim()));
        return list;
    }

    private static List<String> split(String json) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        boolean inQ = false;
        int start = 0;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) inQ = !inQ;
            if (!inQ) {
                if (c == '{' || c == '[') depth++;
                if (c == '}' || c == ']') depth--;
                if (c == ',' && depth == 0) {
                    parts.add(json.substring(start, i));
                    start = i + 1;
                }
            }
        }
        if (start < json.length()) parts.add(json.substring(start));
        return parts;
    }

    private static int findColon(String str) {
        boolean inQ = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '"' && (i == 0 || str.charAt(i - 1) != '\\')) inQ = !inQ;
            if (!inQ && c == ':') return i;
        }
        return -1;
    }

    private static String serializeValue(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + obj + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map) return serializeMap((Map<?, ?>) obj);
        if (obj instanceof Collection) return serializeCollection((Collection<?>) obj);
        return serializeObject(obj);
    }

    private static String serializeMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":").append(serializeValue(e.getValue()));
        }
        return sb.append("}").toString();
    }

    private static String serializeCollection(Collection<?> col) {
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (Object o : col) {
            if (i++ > 0) sb.append(",");
            sb.append(serializeValue(o));
        }
        return sb.append("]").toString();
    }

    private static String serializeObject(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        int i = 0;
        for (Field f : fields) {
            f.setAccessible(true);
            try {
                if (i++ > 0) sb.append(",");
                sb.append("\"").append(f.getName()).append("\":").append(serializeValue(f.get(obj)));
            } catch (IllegalAccessException ignored) {}
        }
        return sb.append("}").toString();
    }
}
