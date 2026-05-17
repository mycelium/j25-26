package jsonlib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class JsonOutput {
    private final FieldNamePolicy fieldNaming;
    private final boolean writeNulls;
    private final Map<Class<?>, JsonConverter<?>> converters;

    JsonOutput(FieldNamePolicy fieldNaming, boolean writeNulls, Map<Class<?>, JsonConverter<?>> converters) {
        this.fieldNaming = fieldNaming;
        this.writeNulls = writeNulls;
        this.converters = converters;
    }

    String write(Object value) {
        StringBuilder builder = new StringBuilder();
        appendValue(value, builder, new IdentityHashMap<>());
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private void appendValue(Object value, StringBuilder builder, IdentityHashMap<Object, Boolean> visiting) {
        if (value == null) {
            builder.append("null");
            return;
        }

        JsonConverter<Object> adapter = (JsonConverter<Object>) converters.get(value.getClass());
        if (adapter != null) {
            appendValue(adapter.encode(value), builder, visiting);
            return;
        }

        if (value instanceof String text) {
            appendString(text, builder);
        } else if (value instanceof Character character) {
            appendString(String.valueOf(character), builder);
        } else if (value instanceof Number number) {
            appendNumber(number, builder);
        } else if (value instanceof Boolean bool) {
            builder.append(bool);
        } else if (value instanceof Enum<?> enumValue) {
            appendString(enumValue.name(), builder);
        } else if (value instanceof Map<?, ?> map) {
            appendMap(map, builder, visiting);
        } else if (value instanceof Iterable<?> iterable) {
            appendIterable(iterable, builder, visiting);
        } else if (value.getClass().isArray()) {
            appendArray(value, builder, visiting);
        } else {
            appendObject(value, builder, visiting);
        }
    }

    private void appendNumber(Number number, StringBuilder builder) {
        if (number instanceof Double value && !Double.isFinite(value)) {
            throw new JsonException("Double value is not representable in JSON: " + value);
        }
        if (number instanceof Float value && !Float.isFinite(value)) {
            throw new JsonException("Float value is not representable in JSON: " + value);
        }
        builder.append(number);
    }

    private void appendMap(Map<?, ?> map, StringBuilder builder, IdentityHashMap<Object, Boolean> visiting) {
        beginComplexValue(map, visiting);
        builder.append('{');

        Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
        boolean first = true;
        while (iterator.hasNext()) {
            Map.Entry<?, ?> entry = iterator.next();
            if (entry.getKey() == null) {
                throw new JsonException("JSON object key must not be null");
            }
            if (!first) {
                builder.append(',');
            }
            first = false;
            appendString(String.valueOf(entry.getKey()), builder);
            builder.append(':');
            appendValue(entry.getValue(), builder, visiting);
        }

        builder.append('}');
        visiting.remove(map);
    }

    private void appendIterable(Iterable<?> values, StringBuilder builder, IdentityHashMap<Object, Boolean> visiting) {
        beginComplexValue(values, visiting);
        builder.append('[');

        Iterator<?> iterator = values.iterator();
        boolean first = true;
        while (iterator.hasNext()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            appendValue(iterator.next(), builder, visiting);
        }

        builder.append(']');
        visiting.remove(values);
    }

    private void appendArray(Object array, StringBuilder builder, IdentityHashMap<Object, Boolean> visiting) {
        beginComplexValue(array, visiting);
        builder.append('[');

        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            appendValue(Array.get(array, i), builder, visiting);
        }

        builder.append(']');
        visiting.remove(array);
    }

    private void appendObject(Object value, StringBuilder builder, IdentityHashMap<Object, Boolean> visiting) {
        beginComplexValue(value, visiting);
        builder.append('{');

        boolean first = true;
        for (Field field : fieldsOf(value.getClass())) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object fieldValue = field.get(value);
                if (fieldValue == null && !writeNulls) {
                    continue;
                }
                if (!first) {
                    builder.append(',');
                }
                first = false;
                appendString(fieldNaming.apply(field.getName()), builder);
                builder.append(':');
                appendValue(fieldValue, builder, visiting);
            } catch (IllegalAccessException exception) {
                throw new JsonException("Cannot read field " + field.getName(), exception);
            }
        }

        builder.append('}');
        visiting.remove(value);
    }

    private void beginComplexValue(Object value, IdentityHashMap<Object, Boolean> visiting) {
        if (visiting.containsKey(value)) {
            throw new JsonException("Cyclic dependency is not supported");
        }
        visiting.put(value, Boolean.TRUE);
    }

    private List<Field> fieldsOf(Class<?> type) {
        ArrayList<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            Collections.addAll(fields, current.getDeclaredFields());
            current = current.getSuperclass();
        }
        return fields;
    }

    private void appendString(String value, StringBuilder builder) {
        builder.append('"');
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (current < 0x20) {
                        builder.append("\\u");
                        String hex = Integer.toHexString(current);
                        builder.append("0".repeat(4 - hex.length())).append(hex);
                    } else {
                        builder.append(current);
                    }
                }
            }
        }
        builder.append('"');
    }
}
