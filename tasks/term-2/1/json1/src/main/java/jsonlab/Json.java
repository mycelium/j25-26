package jsonlab;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;


public final class Json {
    private Json() {}


    public static Object decode(String source) {
        if (source == null || source.isBlank()) {
            throw new JsonException("Input is null or empty");
        }
        return new JsonReader(source).readValue();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> decodeMap(String source) {
        Object root = decode(source);
        if (!(root instanceof Map<?, ?> map)) {
            throw new JsonException("Expected JSON object at root, got: " + root.getClass().getSimpleName());
        }
        return (Map<String, Object>) map;
    }

    public static <T> T decodeTo(String source, Class<T> clazz) {
        if (clazz == null) throw new JsonException("Target class cannot be null");
        if (clazz == String.class) {
            Object val = decode(source);
            return clazz.cast(val == null ? null : val.toString());
        }
        if (clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || clazz == Boolean.class) {
            return castPrimitive(decode(source), clazz);
        }
        Map<String, Object> map = decodeMap(source);
        return ObjectBinder.bind(map, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T decodeTo(String source, Type genericType) {
        if (genericType instanceof Class<?> rawClass) {
            @SuppressWarnings("unchecked")
            Class<T> targetClass = (Class<T>) rawClass;
            return decodeTo(source, targetClass);
        }
        Object parsed = decode(source);
        @SuppressWarnings("unchecked")
        Class<T> raw = (Class<T>) extractRaw(genericType);

        if (parsed instanceof Map<?, ?> m) {
            return ObjectBinder.bind((Map<String, Object>) m, raw);
        }
        throw new JsonException("Cannot map " + parsed.getClass().getSimpleName() + " to " + genericType.getTypeName());
    }

    public static String encode(Object target) {
        return JsonSerializer.write(target);
    }
    @SuppressWarnings("unchecked")
    private static <T> T castPrimitive(Object val, Class<T> clazz) {
        if (val == null) return null;
        try {
            if (clazz == int.class || clazz == Integer.class) return (T) Integer.valueOf(val.toString());
            if (clazz == long.class || clazz == Long.class) return (T) Long.valueOf(val.toString());
            if (clazz == double.class || clazz == Double.class) return (T) Double.valueOf(val.toString());
            if (clazz == float.class || clazz == Float.class) return (T) Float.valueOf(val.toString());
            if (clazz == boolean.class || clazz == Boolean.class) return (T) Boolean.valueOf(val.toString());
            if (clazz == byte.class || clazz == Byte.class) return (T) Byte.valueOf(val.toString());
            if (clazz == short.class || clazz == Short.class) return (T) Short.valueOf(val.toString());
        } catch (NumberFormatException e) {
            throw new JsonException("Cannot cast '" + val + "' to " + clazz.getSimpleName(), e);
        }
        return (T) val;
    }

    @SuppressWarnings("unchecked")
    private static Class<?> extractRaw(Type type) {
        if (type instanceof Class<?> c) return c;
        if (type instanceof java.lang.reflect.ParameterizedType pt) return (Class<?>) pt.getRawType();
        return Object.class;
    }
}