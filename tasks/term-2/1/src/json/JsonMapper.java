package json;

import java.lang.reflect.*;
import java.util.*;

public class JsonMapper {

    public <T> T map(Object source, Class<T> clazz) {
        try {
            if (source == null) return null;

            if (isPrimitive(clazz)) {
                return (T) source;
            }

            if (source instanceof Map) {
                return mapObject((Map<String, Object>) source, clazz);
            }

            if (source instanceof List && clazz.isArray()) {
                return mapArray((List<?>) source, clazz);
            }

            return (T) source;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T mapObject(Map<String, Object> map, Class<T> clazz) throws Exception {
        T obj = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            Object value = map.get(field.getName());
            if (value == null) continue;

            Class<?> type = field.getType();

            if (value instanceof Map) {
                value = mapObject((Map<String, Object>) value, type);
            } else if (value instanceof List) {
                value = mapList((List<?>) value, field);
            }

            field.set(obj, cast(value, type));
        }

        return obj;
    }

    private Object mapList(List<?> list, Field field) throws Exception {
        Class<?> elementType = Object.class;

        if (field.getGenericType() instanceof ParameterizedType pt) {
            elementType = (Class<?>) pt.getActualTypeArguments()[0];
        }

        List<Object> result = new ArrayList<>();

        for (Object item : list) {
            if (item instanceof Map) {
                result.add(mapObject((Map<String, Object>) item, elementType));
            } else {
                result.add(item);
            }
        }

        return result;
    }

    private <T> T mapArray(List<?> list, Class<T> clazz) {
        Class<?> componentType = clazz.getComponentType();
        Object array = Array.newInstance(componentType, list.size());

        for (int i = 0; i < list.size(); i++) {
            Object val = list.get(i);
            Array.set(array, i, cast(val, componentType));
        }

        return (T) array;
    }

    private Object cast(Object value, Class<?> type) {
        if (value == null) return null;

        if (type == int.class || type == Integer.class) return ((Number) value).intValue();
        if (type == long.class || type == Long.class) return ((Number) value).longValue();
        if (type == double.class || type == Double.class) return ((Number) value).doubleValue();
        if (type == boolean.class || type == Boolean.class) return value;
        if (type == String.class) return value.toString();

        return value;
    }

    private boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == Boolean.class;
    }
}