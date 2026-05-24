package lab1.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class JsonSerializer {
    public String toJson(Object object) {
        return serialize(object);
    }

    private String serialize(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (obj instanceof String str) {
            return quote(str);
        }

        if (obj instanceof Character ch) {
            return quote(String.valueOf(ch));
        }

        if (obj instanceof Number number) {
            return serializeNumber(number);
        }

        if (obj instanceof Boolean bool) {
            return bool.toString();
        }

        if (obj instanceof Enum<?> enumValue) {
            return quote(enumValue.name());
        }

        Class<?> clazz = obj.getClass();

        if (clazz.isArray()) {
            return serializeArray(obj);
        }

        if (obj instanceof Collection<?> collection) {
            return serializeCollection(collection);
        }

        if (obj instanceof Map<?, ?> map) {
            return serializeMap(map);
        }

        return serializeObject(obj);
    }

    private String serializeNumber(Number number) {
        if (number instanceof Double doubleValue) {
            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                throw new JsonException("Non-finite double value cannot be represented in JSON");
            }
        }

        if (number instanceof Float floatValue) {
            if (Float.isNaN(floatValue) || Float.isInfinite(floatValue)) {
                throw new JsonException("Non-finite float value cannot be represented in JSON");
            }
        }

        return number.toString();
    }

    private String serializeArray(Object array) {
        int length = Array.getLength(array);
        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(',');
            sb.append(serialize(Array.get(array, i)));
        }

        sb.append(']');
        return sb.toString();
    }

    private String serializeCollection(Collection<?> collection) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        for (Object item : collection) {
            if (!first) sb.append(',');
            first = false;
            sb.append(serialize(item));
        }

        sb.append(']');
        return sb.toString();
    }

    private String serializeMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(quote(String.valueOf(entry.getKey()))).append(':');
            sb.append(serialize(entry.getValue()));
        }

        sb.append('}');
        return sb.toString();
    }

    private String serializeObject(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        for (Field field : getAllFields(obj.getClass())) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }

            field.setAccessible(true);
            try {
                if (!first) sb.append(',');
                first = false;
                sb.append(quote(field.getName())).append(':');
                sb.append(serialize(field.get(obj)));
            } catch (IllegalAccessException e) {
                throw new JsonException("Serialization error for field " + field.getName(), e);
            }
        }

        sb.append('}');
        return sb.toString();
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(List.of(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }

    private String quote(String value) {
        return '"' + escapeString(value) + '"';
    }

    private String escapeString(String str) {
        StringBuilder sb = new StringBuilder();

        for (char c : str.toCharArray()) {
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

        return sb.toString();
    }
}
