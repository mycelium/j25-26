package jsonparser;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class JsonSerializer {

    public String toJson(Object value) {
        StringBuilder sb = new StringBuilder();
        serialize(value, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void serialize(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append("null");
            return;
        }

        if (value instanceof Boolean) {
            sb.append(value);
            return;
        }

        if (value instanceof Number) {
            if (value instanceof Double || value instanceof Float) {
                double d = ((Number) value).doubleValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    sb.append(((Number) value).longValue());
                } else {
                    sb.append(value);
                }
            } else {
                sb.append(value);
            }
            return;
        }

        if (value instanceof String) {
            serializeString((String) value, sb);
            return;
        }

        if (value instanceof Map) {
            serializeMap((Map<Object, Object>) value, sb);
            return;
        }

        if (value instanceof Collection) {
            serializeCollection((Collection<Object>) value, sb);
            return;
        }

        if (value.getClass().isArray()) {
            serializeArray(value, sb);
            return;
        }

        serializePojo(value, sb);
    }

    private void serializeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        sb.append('"');
    }

    private void serializeMap(Map<Object, Object> map, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            if (!first)
                sb.append(',');
            first = false;
            serializeString(String.valueOf(entry.getKey()), sb);
            sb.append(':');
            serialize(entry.getValue(), sb);
        }
        sb.append('}');
    }

    private void serializeCollection(Collection<Object> collection, StringBuilder sb) {
        sb.append('[');
        boolean first = true;
        for (Object item : collection) {
            if (!first)
                sb.append(',');
            first = false;
            serialize(item, sb);
        }
        sb.append(']');
    }

    private void serializeArray(Object array, StringBuilder sb) {
        sb.append('[');
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0)
                sb.append(',');
            serialize(Array.get(array, i), sb);
        }
        sb.append(']');
    }

    private void serializePojo(Object obj, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                int mods = field.getModifiers();
                if (java.lang.reflect.Modifier.isStatic(mods))
                    continue;
                if (java.lang.reflect.Modifier.isTransient(mods))
                    continue;
                if (field.isSynthetic())
                    continue;

                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(obj);
                    if (!first)
                        sb.append(',');
                    first = false;
                    serializeString(field.getName(), sb);
                    sb.append(':');
                    serialize(fieldValue, sb);
                } catch (IllegalAccessException e) {
                    throw new JsonException("Cannot access field: " + field.getName(), e);
                }
            }
            clazz = clazz.getSuperclass();
        }
        sb.append('}');
    }
}
