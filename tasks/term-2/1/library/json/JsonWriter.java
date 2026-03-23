package json;

import json.exceptions.JsonMappingException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class JsonWriter {
    private final JsonConfig config;
    private final IdentityHashMap<Object, Boolean> activeReferences = new IdentityHashMap<>();

    public JsonWriter(JsonConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    public String write(Object value) {
        StringBuilder builder = new StringBuilder();
        writeValue(value, builder);
        return builder.toString();
    }

    private void writeValue(Object value, StringBuilder builder) {
        if (value == null) {
            builder.append("null");
            return;
        }

        if (value instanceof String stringValue) {
            writeString(stringValue, builder);
            return;
        }
        if (value instanceof Character characterValue) {
            writeString(String.valueOf(characterValue), builder);
            return;
        }
        if (value instanceof Number numberValue) {
            writeNumber(numberValue, builder);
            return;
        }
        if (value instanceof Boolean boolValue) {
            builder.append(boolValue);
            return;
        }
        if (value instanceof Enum<?> enumValue) {
            writeString(enumValue.name(), builder);
            return;
        }
        if (value instanceof Map<?, ?> mapValue) {
            writeMap(mapValue, builder);
            return;
        }
        if (value instanceof Iterable<?> iterableValue) {
            writeIterable(iterableValue, builder);
            return;
        }
        if (value.getClass().isArray()) {
            writeArray(value, builder);
            return;
        }

        writeObject(value, builder);
    }

    private void writeMap(Map<?, ?> mapValue, StringBuilder builder) {
        beginComposite(mapValue);
        try {
            builder.append('{');
            Iterator<? extends Map.Entry<?, ?>> iterator = mapValue.entrySet().iterator();
            boolean first = true;
            while (iterator.hasNext()) {
                Map.Entry<?, ?> entry = iterator.next();
                if (!first) {
                    builder.append(',');
                }
                writeMapKey(entry.getKey(), builder);
                builder.append(':');
                writeValue(entry.getValue(), builder);
                first = false;
            }
            builder.append('}');
        } finally {
            endComposite(mapValue);
        }
    }

    private void writeIterable(Iterable<?> iterableValue, StringBuilder builder) {
        beginComposite(iterableValue);
        try {
            builder.append('[');
            boolean first = true;
            for (Object item : iterableValue) {
                if (!first) {
                    builder.append(',');
                }
                writeValue(item, builder);
                first = false;
            }
            builder.append(']');
        } finally {
            endComposite(iterableValue);
        }
    }

    private void writeArray(Object arrayValue, StringBuilder builder) {
        beginComposite(arrayValue);
        try {
            builder.append('[');
            int length = Array.getLength(arrayValue);
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    builder.append(',');
                }
                writeValue(Array.get(arrayValue, i), builder);
            }
            builder.append(']');
        } finally {
            endComposite(arrayValue);
        }
    }

    private void writeObject(Object objectValue, StringBuilder builder) {
        beginComposite(objectValue);
        try {
            List<Field> fields = ReflectionUtils.getSerializableFields(objectValue.getClass());
            builder.append('{');
            boolean first = true;
            for (Field field : fields) {
                Object fieldValue;
                try {
                    fieldValue = field.get(objectValue);
                } catch (IllegalAccessException ex) {
                    throw new JsonMappingException("Cannot access field '" + field.getName() + "'", ex);
                }

                if (fieldValue == null && !config.isIncludeNullFields()) {
                    continue;
                }

                if (!first) {
                    builder.append(',');
                }
                writeString(field.getName(), builder);
                builder.append(':');
                writeValue(fieldValue, builder);
                first = false;
            }
            builder.append('}');
        } finally {
            endComposite(objectValue);
        }
    }

    private void writeNumber(Number numberValue, StringBuilder builder) {
        if (numberValue instanceof Double doubleValue) {
            if (!Double.isFinite(doubleValue)) {
                throw new JsonMappingException("Double NaN/Infinity cannot be represented in JSON");
            }
            builder.append(doubleValue);
            return;
        }

        if (numberValue instanceof Float floatValue) {
            if (!Float.isFinite(floatValue)) {
                throw new JsonMappingException("Float NaN/Infinity cannot be represented in JSON");
            }
            builder.append(floatValue);
            return;
        }

        if (numberValue instanceof BigDecimal decimalValue) {
            builder.append(decimalValue.toPlainString());
            return;
        }

        builder.append(numberValue);
    }

    private void writeString(String stringValue, StringBuilder builder) {
        builder.append('"');
        for (int i = 0; i < stringValue.length(); i++) {
            char ch = stringValue.charAt(i);
            switch (ch) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        appendUnicodeEscape(ch, builder);
                    } else {
                        builder.append(ch);
                    }
                }
            }
        }
        builder.append('"');
    }

    private void writeMapKey(Object key, StringBuilder builder) {
        if (key == null) {
            throw new JsonMappingException("JSON object key cannot be null");
        }

        if (key instanceof String stringKey) {
            writeString(stringKey, builder);
            return;
        }
        if (key instanceof Character characterKey) {
            writeString(String.valueOf(characterKey), builder);
            return;
        }
        if (key instanceof Number || key instanceof Boolean || key instanceof Enum<?>) {
            writeString(String.valueOf(key), builder);
            return;
        }

        throw new JsonMappingException("Unsupported map key type for JSON object: " + key.getClass().getName());
    }

    private void appendUnicodeEscape(char ch, StringBuilder builder) {
        builder.append("\\u");
        String hex = Integer.toHexString(ch);
        for (int i = hex.length(); i < 4; i++) {
            builder.append('0');
        }
        builder.append(hex);
    }

    private void beginComposite(Object value) {
        if (!config.isDetectCycles()) {
            return;
        }
        if (activeReferences.containsKey(value)) {
            throw new JsonMappingException("Cyclic dependency detected for type " + value.getClass().getName());
        }
        activeReferences.put(value, Boolean.TRUE);
    }

    private void endComposite(Object value) {
        if (!config.isDetectCycles()) {
            return;
        }
        activeReferences.remove(value);
    }
}
