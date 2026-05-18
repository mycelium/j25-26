package org.example.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class JsonSerializer {

    public String serialize(Object obj) {
        StringBuilder sb = new StringBuilder();
        serialize(obj, sb);
        return sb.toString();
    }

    private void serialize(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof Boolean) {
            sb.append(obj);
        } else if (obj instanceof Number) {
            sb.append(obj);
        } else if (obj instanceof String s) {
            serializeString(s, sb);
        } else if (obj instanceof Map<?, ?> map) {
            serializeMap(map, sb);
        } else if (obj instanceof Collection<?> col) {
            serializeCollection(col, sb);
        } else if (obj.getClass().isArray()) {
            serializeArray(obj, sb);
        } else {
            serializeObject(obj, sb);
        }
    }

    private void serializeString(String s, StringBuilder sb) {
        sb.append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private void serializeMap(Map<?, ?> map, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            serializeString(String.valueOf(entry.getKey()), sb);
            sb.append(':');
            serialize(entry.getValue(), sb);
        }
        sb.append('}');
    }

    private void serializeCollection(Collection<?> col, StringBuilder sb) {
        sb.append('[');
        boolean first = true;
        for (Object item : col) {
            if (!first) sb.append(',');
            first = false;
            serialize(item, sb);
        }
        sb.append(']');
    }

    private void serializeArray(Object arr, StringBuilder sb) {
        sb.append('[');
        int len = Array.getLength(arr);
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(',');
            serialize(Array.get(arr, i), sb);
        }
        sb.append(']');
    }

    private void serializeObject(Object obj, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        Class<?> clazz = obj.getClass();
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (!first) sb.append(',');
                first = false;
                serializeString(field.getName(), sb);
                sb.append(':');
                serialize(value, sb);
            } catch (IllegalAccessException e) {
                throw new JsonException("Cannot access field: " + field.getName(), e);
            }
        }
        sb.append('}');
    }
}
