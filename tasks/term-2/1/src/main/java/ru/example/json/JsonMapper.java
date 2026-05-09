package ru.example.json;

import java.lang.reflect.*;
import java.util.*;

public class JsonMapper {

    public static <T> T fromJson(String json, Class<T> clazz) {
        Object parsed = new JsonParser(json).parse();
        return mapToClass(parsed, clazz);
    }

    @SuppressWarnings("unchecked")
    private static <T> T mapToClass(Object parsed, Class<T> clazz) {
        if (parsed == null) return null;

        if (!(parsed instanceof Map<?, ?> map)) {
            throw new RuntimeException("Expected JSON object for class " + clazz.getName());
        }

        try {
            T obj = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = map.get(field.getName());

                if (value == null) continue;

                Object converted = convertValue(value, field.getType());
                field.set(obj, converted);
            }

            return obj;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object convertValue(Object value, Class<?> targetType) {

        if (value == null) return null;

        // primitives + wrappers + String
        if (targetType == int.class || targetType == Integer.class) {
            return ((Number) value).intValue();
        }
        if (targetType == double.class || targetType == Double.class) {
            return ((Number) value).doubleValue();
        }
        if (targetType == long.class || targetType == Long.class) {
            return ((Number) value).longValue();
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return value;
        }
        if (targetType == String.class) {
            return value.toString();
        }

        // array
        if (targetType.isArray() && value instanceof List<?> list) {
            Class<?> componentType = targetType.getComponentType();
            Object array = Array.newInstance(componentType, list.size());

            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, convertValue(list.get(i), componentType));
            }

            return array;
        }

        // collection
        if (List.class.isAssignableFrom(targetType) && value instanceof List<?> list) {
            return new ArrayList<>(list);
        }

        // nested object
        if (value instanceof Map<?, ?>) {
            return mapToClass(value, targetType);
        }

        return value;
    }
}