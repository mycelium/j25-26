package org.example.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class JsonParser {

    private final JsonTokenizer tokenizer;

    public JsonParser(JsonTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public Object parseValue() {
        JsonTokenizer.Token token = tokenizer.peek();
        return switch (token.type()) {
            case OBJECT_START -> parseObject();
            case ARRAY_START -> parseArray();
            case STRING -> { tokenizer.next(); yield token.value(); }
            case NUMBER -> { tokenizer.next(); yield parseNumber(token.value()); }
            case BOOLEAN -> { tokenizer.next(); yield Boolean.parseBoolean(token.value()); }
            case NULL -> { tokenizer.next(); yield null; }
            default -> throw new JsonException("Unexpected token: " + token.type());
        };
    }

    private Object parseNumber(String value) {
        if (value.contains(".") || value.contains("e") || value.contains("E")) {
            return Double.parseDouble(value);
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return Double.parseDouble(value);
        }
    }

    private Map<String, Object> parseObject() {
        tokenizer.expect(JsonTokenizer.TokenType.OBJECT_START);
        Map<String, Object> map = new LinkedHashMap<>();
        if (tokenizer.peek().type() == JsonTokenizer.TokenType.OBJECT_END) {
            tokenizer.next();
            return map;
        }
        do {
            String key = tokenizer.expect(JsonTokenizer.TokenType.STRING).value();
            tokenizer.expect(JsonTokenizer.TokenType.COLON);
            Object value = parseValue();
            map.put(key, value);
        } while (tokenizer.peek().type() == JsonTokenizer.TokenType.COMMA && tokenizer.next() != null);
        tokenizer.expect(JsonTokenizer.TokenType.OBJECT_END);
        return map;
    }

    private List<Object> parseArray() {
        tokenizer.expect(JsonTokenizer.TokenType.ARRAY_START);
        List<Object> list = new ArrayList<>();
        if (tokenizer.peek().type() == JsonTokenizer.TokenType.ARRAY_END) {
            tokenizer.next();
            return list;
        }
        do {
            list.add(parseValue());
        } while (tokenizer.peek().type() == JsonTokenizer.TokenType.COMMA && tokenizer.next() != null);
        tokenizer.expect(JsonTokenizer.TokenType.ARRAY_END);
        return list;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T parseAs(Class<T> clazz) {
        Object raw = parseValue();
        return convertTo(raw, clazz);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T convertTo(Object raw, Class<T> clazz) {
        if (raw == null) return null;

        System.err.println("[DEBUG] raw=" + raw + " (" + (raw == null ? "null" : raw.getClass().getName()) + ") -> " + clazz.getName());

        if (clazz == String.class) return (T) raw.toString();

        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(((Number) raw).intValue());
        }
        if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(((Number) raw).longValue());
        }
        if (clazz == double.class || clazz == Double.class) {
            return (T) Double.valueOf(((Number) raw).doubleValue());
        }
        if (clazz == float.class || clazz == Float.class) {
            return (T) Float.valueOf(((Number) raw).floatValue());
        }
        if (clazz == boolean.class || clazz == Boolean.class) {
            return (T) raw;
        }
        if (clazz == byte.class || clazz == Byte.class) {
            return (T) Byte.valueOf(((Number) raw).byteValue());
        }
        if (clazz == short.class || clazz == Short.class) {
            return (T) Short.valueOf(((Number) raw).shortValue());
        }
        if (clazz == Object.class) return (T) raw;

        if (clazz.isArray() && raw instanceof List<?> list) {
            Class<?> componentType = clazz.getComponentType();
            Object arr = Array.newInstance(componentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(arr, i, convertTo(list.get(i), componentType));
            }
            return clazz.cast(arr);
        }

        if (Map.class.isAssignableFrom(clazz) && raw instanceof Map) return clazz.cast(raw);

        if (raw instanceof Map<?, ?> map) {
            try {
                T instance = clazz.getDeclaredConstructor().newInstance();
                for (Field field : getAllFields(clazz)) {
                    field.setAccessible(true);
                    Object val = map.get(field.getName());
                    if (val != null) {
                        field.set(instance, convertTo(val, (Class) field.getType()));
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new JsonException("Cannot instantiate " + clazz.getName(), e);
            }
        }

        throw new JsonException("Cannot convert " + raw.getClass().getSimpleName() + " to " + clazz.getName());
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }
}
