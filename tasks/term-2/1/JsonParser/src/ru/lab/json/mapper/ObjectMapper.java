package ru.lab.json.mapper;

import ru.lab.json.exception.JsonException;
import java.lang.reflect.*;
import java.util.*;

public final class ObjectMapper {

    private ObjectMapper() {}

    @SuppressWarnings("unchecked")
    public static Object convertType(Object value, Class<?> targetType, Type genericType) {
        if (value == null) return null;
        if (targetType.isAssignableFrom(value.getClass())) return value;

        if (value instanceof Map<?, ?> map && !Map.class.isAssignableFrom(targetType)) {
            return mapToClass((Map<String, Object>) map, targetType);
        }

        if (value instanceof List<?> list) {
            if (targetType.isArray()) {
                return listToArray(list, targetType.getComponentType());
            } else if (Collection.class.isAssignableFrom(targetType)) {
                Type elementGenericType = Object.class;
                if (genericType instanceof ParameterizedType pt && pt.getActualTypeArguments().length > 0) {
                    elementGenericType = pt.getActualTypeArguments()[0];
                }
                return listToCollection(list, targetType, elementGenericType);
            }
        }

        if (value instanceof Number num) {
            if (targetType == int.class || targetType == Integer.class) return num.intValue();
            if (targetType == long.class || targetType == Long.class) return num.longValue();
            if (targetType == double.class || targetType == Double.class) return num.doubleValue();
            if (targetType == float.class || targetType == Float.class) return num.floatValue();
            if (targetType == short.class || targetType == Short.class) return num.shortValue();
            if (targetType == byte.class || targetType == Byte.class) return num.byteValue();
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    private static <T> T mapToClass(Map<String, Object> map, Class<T> clazz) {
        try {
        	var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            var instance = clazz.getDeclaredConstructor().newInstance();
            for (var entry : map.entrySet()) {
                try {
                    var field = clazz.getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    var convertedValue = convertType(entry.getValue(), field.getType(), field.getGenericType());
                    field.set(instance, convertedValue);
                } catch (NoSuchFieldException ignored) {
                }
            }
            return instance;
        } catch (Exception e) {
            throw new JsonException("Mapping error for class " + clazz.getName(), e);
        }
    }

    private static Object listToArray(List<?> list, Class<?> componentType) {
        var array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, convertType(list.get(i), componentType, componentType));
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    private static Object listToCollection(List<?> list, Class<?> targetType, Type elementGenericType) {
        try {
            Collection<Object> collection;
            if (targetType.isInterface()) {
                collection = Set.class.isAssignableFrom(targetType) ? new HashSet<>() : new ArrayList<>();
            } else {
                collection = (Collection<Object>) targetType.getDeclaredConstructor().newInstance();
            }

            Class<?> elementClass = Object.class;
            if (elementGenericType instanceof Class<?> cl) {
                elementClass = cl;
            } else if (elementGenericType instanceof ParameterizedType pt) {
                elementClass = (Class<?>) pt.getRawType();
            }

            for (Object item : list) {
                collection.add(convertType(item, elementClass, elementGenericType));
            }
            return collection;
        } catch (Exception e) {
            throw new JsonException("Failed to populate collection " + targetType.getName(), e);
        }
    }
}