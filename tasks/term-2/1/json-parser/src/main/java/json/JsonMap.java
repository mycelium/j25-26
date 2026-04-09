package json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class JsonMap {

    private final JsonConfig config;

    JsonMap(JsonConfig config) {
        this.config = config;
    }

    <T> T toObject(Object parsedJson, Class<T> targetClass) {
        if (parsedJson == null) {
            return null;
        }

        if (!(parsedJson instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("JSON root is not an object");
        }

        return mapToClass((Map<String, Object>) map, targetClass);
    }

    private <T> T mapToClass(Map<String, Object> source, Class<T> targetClass) {
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();

            for (Field field : targetClass.getDeclaredFields()) {
                field.setAccessible(true);

                String fieldName = field.getName();
                if (!source.containsKey(fieldName)) {
                    continue;
                }

                Object rawValue = source.get(fieldName);
                Object mappedValue = convertValue(rawValue, field.getType(), field.getGenericType());

                field.set(instance, mappedValue);
            }

            if (!config.isIgnoreUnknownFields()) {
                for (String key : source.keySet()) {
                    boolean found = false;
                    for (Field field : targetClass.getDeclaredFields()) {
                        if (field.getName().equals(key)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        throw new IllegalArgumentException(
                                "Unknown field '" + key + "' for class " + targetClass.getName()
                        );
                    }
                }
            }

            return instance;

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to map JSON to class " + targetClass.getName(), e
            );
        }
    }

    private Object convertValue(Object rawValue, Class<?> targetType, Type genericType) {
        if (rawValue == null) {
            if (targetType.isPrimitive()) {
                throw new IllegalArgumentException(
                        "Cannot assign null to primitive type " + targetType.getName()
                );
            }
            return null;
        }

        if (targetType == String.class) {
            return String.valueOf(rawValue);
        }

        if (targetType == int.class || targetType == Integer.class) {
            return ((Number) rawValue).intValue();
        }

        if (targetType == long.class || targetType == Long.class) {
            return ((Number) rawValue).longValue();
        }

        if (targetType == double.class || targetType == Double.class) {
            return ((Number) rawValue).doubleValue();
        }

        if (targetType == float.class || targetType == Float.class) {
            return ((Number) rawValue).floatValue();
        }

        if (targetType == short.class || targetType == Short.class) {
            return ((Number) rawValue).shortValue();
        }

        if (targetType == byte.class || targetType == Byte.class) {
            return ((Number) rawValue).byteValue();
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            return rawValue;
        }

        if (targetType == char.class || targetType == Character.class) {
            String s = String.valueOf(rawValue);
            if (s.length() != 1) {
                throw new IllegalArgumentException("Cannot convert to char: " + rawValue);
            }
            return s.charAt(0);
        }

        if (targetType.isArray()) {
            return convertArray(rawValue, targetType.getComponentType());
        }

        if (Collection.class.isAssignableFrom(targetType)) {
            return convertCollection(rawValue, genericType);
        }

        if (rawValue instanceof Map<?, ?> nestedMap) {
            return mapToClass((Map<String, Object>) nestedMap, targetType);
        }

        if (targetType.isAssignableFrom(rawValue.getClass())) {
            return rawValue;
        }

        throw new IllegalArgumentException(
                "Unsupported field type: " + targetType.getName()
        );
    }

    private Object convertArray(Object rawValue, Class<?> componentType) {
        if (!(rawValue instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected JSON array for Java array");
        }

        Object array = Array.newInstance(componentType, list.size());

        for (int i = 0; i < list.size(); i++) {
            Object converted = convertValue(list.get(i), componentType, componentType);
            Array.set(array, i, converted);
        }

        return array;
    }

    private Collection<Object> convertCollection(Object rawValue, Type genericType) {
        if (!(rawValue instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected JSON array for Java collection");
        }

        List<Object> result = new ArrayList<>();

        Class<?> elementType = Object.class;

        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] args = parameterizedType.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?>) {
                elementType = (Class<?>) args[0];
            }
        }

        for (Object item : list) {
            Object converted = convertValue(item, elementType, elementType);
            result.add(converted);
        }

        return result;
    }
}