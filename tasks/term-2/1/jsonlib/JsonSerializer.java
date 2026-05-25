package jsonlib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

class JsonSerializer {

    String toJson(Object obj) {
        if (obj == null)                    return "null";
        if (obj instanceof String s)        return encodeString(s);
        if (obj instanceof Boolean b)       return b.toString();
        if (obj instanceof Number n)        return n.toString();
        if (obj instanceof Collection<?> c) return encodeCollection(c);
        if (obj instanceof Map<?, ?> m)     return encodeMap(m);
        if (obj.getClass().isArray())       return encodeArray(obj);
        return encodeObject(obj);
    }

    private String encodeString(String s) {
        return "\"" + escape(s) + "\"";
    }

    private String encodeCollection(Collection<?> col) {
        List<String> items = new ArrayList<>();
        for (Object item : col) items.add(toJson(item));
        return "[" + String.join(",", items) + "]";
    }

    private String encodeArray(Object arr) {
        int len = Array.getLength(arr);
        List<String> items = new ArrayList<>();
        for (int i = 0; i < len; i++) items.add(toJson(Array.get(arr, i)));
        return "[" + String.join(",", items) + "]";
    }

    private String encodeMap(Map<?, ?> map) {
        List<String> items = new ArrayList<>();
        for (var entry : map.entrySet()) {
            items.add("\"" + entry.getKey() + "\":" + toJson(entry.getValue()));
        }
        return "{" + String.join(",", items) + "}";
    }

    private String encodeObject(Object obj) {
        List<String> fields = new ArrayList<>();
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                fields.add("\"" + f.getName() + "\":" + toJson(f.get(obj)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return "{" + String.join(",", fields) + "}";
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}