package lab1.json;

import java.lang.reflect.*;
import java.util.*;

class JsonMapper {

    public <T> T convert(Object value, Class<T> clazz) {
        return clazz.cast(convertValue(value, clazz, clazz));
    }

    private Object convertValue(Object value, Class<?> rawType, Type genericType) {
        if (value == null) {
            return null;
        }

        if (rawType == String.class) {
            return String.valueOf(value);
        }

        if (rawType == int.class || rawType == Integer.class) {
            return ((Number) value).intValue();
        }
        if (rawType == double.class || rawType == Double.class) {
            return ((Number) value).doubleValue();
        }
        if (rawType == boolean.class || rawType == Boolean.class) {
            return value;
        }

        if (rawType.isArray()) {
            List<?> list = (List<?>) value;
            Class<?> componentType = rawType.getComponentType();
            Object array = Array.newInstance(componentType, list.size());

            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, convertValue(list.get(i), componentType, componentType));
            }
            return array;
        }

        if (Collection.class.isAssignableFrom(rawType)) {
            List<?> list = (List<?>) value;
            List<Object> result = new ArrayList<>();

            for (Object item : list) {
                result.add(item);
            }
            return result;
        }

        if (value instanceof Map<?, ?> map) {
            try {
                Object obj = rawType.getDeclaredConstructor().newInstance();

                for (Field field : rawType.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object fieldValue = map.get(field.getName());
                    Object converted = convertValue(fieldValue, field.getType(), field.getGenericType());
                    field.set(obj, converted);
                }

                return obj;
            } catch (Exception e) {
                throw new JsonException("Mapping error", e);
            }
        }

        return value;
    }
}