package json;

import java.lang.reflect.*;
import java.util.*;

public class JsonSerializer {

    public String toJson(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof String) return "\"" + escape((String) obj) + "\"";

        if (obj instanceof Number || obj instanceof Boolean)
            return obj.toString();

        if (obj instanceof Collection<?> col) {
            List<String> items = new ArrayList<>();
            for (Object item : col) items.add(toJson(item));
            return "[" + String.join(",", items) + "]";
        }

        if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            List<String> items = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                items.add(toJson(Array.get(obj, i)));
            }
            return "[" + String.join(",", items) + "]";
        }

        if (obj instanceof Map<?, ?> map) {
            List<String> items = new ArrayList<>();
            for (var e : map.entrySet()) {
                items.add("\"" + e.getKey() + "\":" + toJson(e.getValue()));
            }
            return "{" + String.join(",", items) + "}";
        }

        return serializeObject(obj);
    }

    private String serializeObject(Object obj) {
        List<String> fields = new ArrayList<>();

        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                fields.add("\"" + f.getName() + "\":" + toJson(f.get(obj)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return "{" + String.join(",", fields) + "}";
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}