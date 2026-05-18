package jsonparser;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class JsonMapper {

    private JsonMapper() {
    }

    public static String toJson(Object object) {
        return new JsonSerializer().toJson(object);
    }

    public static Object fromJson(String json) {
        return new JsonParser(json).parse();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String json) {
        Object result = fromJson(json);
        if (!(result instanceof Map)) {
            throw new JsonException("Expected a JSON object but got: " + result.getClass().getSimpleName());
        }
        return (Map<String, Object>) result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String json, Class<T> clazz) {
        Object parsed = fromJson(json);
        return (T) convertTo(parsed, clazz);
    }

    @SuppressWarnings("unchecked")
    private static Object convertTo(Object value, Class<?> targetType) {
        if (value == null)
            return null;

        if (targetType == Object.class)
            return value;

        if (targetType == String.class)
            return String.valueOf(value);

        if (targetType == int.class || targetType == Integer.class) {
            return ((Number) value).intValue();
        }
        if (targetType == long.class || targetType == Long.class) {
            return ((Number) value).longValue();
        }
        if (targetType == double.class || targetType == Double.class) {
            return ((Number) value).doubleValue();
        }
        if (targetType == float.class || targetType == Float.class) {
            return ((Number) value).floatValue();
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return value;
        }
        if (targetType == short.class || targetType == Short.class) {
            return ((Number) value).shortValue();
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return ((Number) value).byteValue();
        }
        if (targetType == char.class || targetType == Character.class) {
            String s = String.valueOf(value);
            return s.isEmpty() ? '\0' : s.charAt(0);
        }

        if (targetType.isArray()) {
            List<Object> list = (List<Object>) value;
            Class<?> componentType = targetType.getComponentType();
            Object array = Array.newInstance(componentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, convertTo(list.get(i), componentType));
            }
            return array;
        }

        if (List.class.isAssignableFrom(targetType)) {
            return value;
        }

        if (Map.class.isAssignableFrom(targetType)) {
            return value;
        }

        if (value instanceof Map) {
            return mapToPojo((Map<String, Object>) value, targetType);
        }

        throw new JsonException("Cannot convert " + value.getClass().getSimpleName()
                + " to " + targetType.getSimpleName());
    }

    private static <T> T mapToPojo(Map<String, Object> map, Class<T> clazz) {
        T instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new JsonException("Cannot instantiate " + clazz.getName()
                    + ". Make sure it has a public no-arg constructor.", e);
        }

        Map<String, Field> fields = new HashMap<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    fields.putIfAbsent(field.getName(), field);
                }
            }
            current = current.getSuperclass();
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Field field = fields.get(entry.getKey());
            if (field == null)
                continue;

            field.setAccessible(true);
            try {
                Object converted = convertTo(entry.getValue(), field.getType());
                field.set(instance, converted);
            } catch (IllegalAccessException e) {
                throw new JsonException("Cannot set field: " + field.getName(), e);
            }
        }
        return instance;
    }
}
