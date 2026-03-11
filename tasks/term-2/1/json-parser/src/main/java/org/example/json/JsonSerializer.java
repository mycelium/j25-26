package org.example.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class JsonSerializer {

    private final JsonConfig config;

    public JsonSerializer() {
        this(new JsonConfig());
    }

    public JsonSerializer(JsonConfig config) {
        this.config = config;
    }

    public String serialize(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(value, sb, 0);
        return sb.toString();
    }

    private void writeValue(Object value, StringBuilder sb, int indentLevel) {
        if (value == null) {
            sb.append("null");
            return;
        }

        if (value instanceof String str) {
            writeString(str, sb);
            return;
        }

        if (value instanceof Character ch) {
            writeString(String.valueOf(ch), sb);
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
            return;
        }

        if (value instanceof Map<?, ?> map) {
            writeMap(map, sb, indentLevel);
            return;
        }

        if (value instanceof Collection<?> collection) {
            writeCollection(collection, sb, indentLevel);
            return;
        }

        if (value.getClass().isArray()) {
            writeArray(value, sb, indentLevel);
            return;
        }

        writeObject(value, sb, indentLevel);
    }

    private void writeMap(Map<?, ?> map, StringBuilder sb, int indentLevel) {
        sb.append('{');

        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            if (!(key instanceof String)) {
                throw new IllegalArgumentException("JSON object keys must be strings");
            }

            Object value = entry.getValue();
            if (value == null && !config.isIncludeNulls()) {
                continue;
            }

            if (!first) {
                sb.append(',');
            }

            if (config.isPrettyPrint()) {
                sb.append('\n');
                indent(sb, indentLevel + 1);
            }

            writeString((String) key, sb);
            sb.append(':');
            if (config.isPrettyPrint()) {
                sb.append(' ');
            }

            writeValue(value, sb, indentLevel + 1);
            first = false;
        }

        if (config.isPrettyPrint() && !first) {
            sb.append('\n');
            indent(sb, indentLevel);
        }

        sb.append('}');
    }

    private void writeCollection(Collection<?> collection, StringBuilder sb, int indentLevel) {
        sb.append('[');

        Iterator<?> iterator = collection.iterator();
        boolean first = true;

        while (iterator.hasNext()) {
            Object item = iterator.next();

            if (!first) {
                sb.append(',');
            }

            if (config.isPrettyPrint()) {
                sb.append('\n');
                indent(sb, indentLevel + 1);
            }

            writeValue(item, sb, indentLevel + 1);
            first = false;
        }

        if (config.isPrettyPrint() && !first) {
            sb.append('\n');
            indent(sb, indentLevel);
        }

        sb.append(']');
    }

    private void writeArray(Object array, StringBuilder sb, int indentLevel) {
        sb.append('[');

        int length = Array.getLength(array);

        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(',');
            }

            if (config.isPrettyPrint()) {
                sb.append('\n');
                indent(sb, indentLevel + 1);
            }

            Object item = Array.get(array, i);
            writeValue(item, sb, indentLevel + 1);
        }

        if (config.isPrettyPrint() && length > 0) {
            sb.append('\n');
            indent(sb, indentLevel);
        }

        sb.append(']');
    }

    private void writeObject(Object object, StringBuilder sb, int indentLevel) {
        sb.append('{');

        Field[] fields = object.getClass().getDeclaredFields();
        boolean first = true;

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            try {
                Object fieldValue = field.get(object);

                if (fieldValue == null && !config.isIncludeNulls()) {
                    continue;
                }

                if (!first) {
                    sb.append(',');
                }

                if (config.isPrettyPrint()) {
                    sb.append('\n');
                    indent(sb, indentLevel + 1);
                }

                writeString(field.getName(), sb);
                sb.append(':');
                if (config.isPrettyPrint()) {
                    sb.append(' ');
                }

                writeValue(fieldValue, sb, indentLevel + 1);
                first = false;

            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(
                        "Cannot access field '" + field.getName() +
                                "' of class " + object.getClass().getName(), e
                );
            }
        }

        if (config.isPrettyPrint() && !first) {
            sb.append('\n');
            indent(sb, indentLevel);
        }

        sb.append('}');
    }

    private void writeString(String value, StringBuilder sb) {
        sb.append('"');

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                }
            }
        }

        sb.append('"');
    }

    private void indent(StringBuilder sb, int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append("  ");
        }
    }
}