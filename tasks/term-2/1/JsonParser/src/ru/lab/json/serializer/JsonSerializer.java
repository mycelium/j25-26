package ru.lab.json.serializer;

import ru.lab.json.exception.JsonException;
import java.lang.reflect.*;
import java.util.*;

public final class JsonSerializer {

    private JsonSerializer() {}

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String s) return "\"" + escape(s) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Collection<?> col) return collectionToJson(col);
        if (obj.getClass().isArray()) return arrayToJson(obj);
        if (obj instanceof Map<?, ?> map) return mapToJson(map);
        return objectToJson(obj);
    }

    private static String escape(String s) {
        var sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default   -> {
                    if (c <= 0x1F) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String collectionToJson(Collection<?> col) {
        var sb = new StringBuilder("[");
        var it = col.iterator();
        while (it.hasNext()) {
            sb.append(toJson(it.next()));
            if (it.hasNext()) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private static String arrayToJson(Object array) {
        var sb = new StringBuilder("[");
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            sb.append(toJson(Array.get(array, i)));
            if (i < length - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private static String mapToJson(Map<?, ?> map) {
        var sb = new StringBuilder("{");
        var it = map.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            sb.append("\"").append(escape(entry.getKey().toString())).append("\":").append(toJson(entry.getValue()));
            if (it.hasNext()) sb.append(",");
        }
        return sb.append("}").toString();
    }

    private static String objectToJson(Object obj) {
        var sb = new StringBuilder("{");
        var fields = obj.getClass().getDeclaredFields();
        boolean first = true;
        try {
            for (var field : fields) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
                field.setAccessible(true);
                if (!first) sb.append(",");
                sb.append("\"").append(field.getName()).append("\":").append(toJson(field.get(obj)));
                first = false;
            }
        } catch (IllegalAccessException e) {
            throw new JsonException("Serialization failed: cannot access fields", e);
        }
        return sb.append("}").toString();
    }
}