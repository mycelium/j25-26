package lab1.json;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

class JsonMapper {
    public <T> T convert(Object value, Class<T> clazz) {
        return clazz.cast(convertValue(value, clazz, clazz));
    }

    private Object convertValue(Object value, Class<?> rawType, Type genericType) {
        if (rawType == Object.class) {
            return value;
        }

        if (value == null) {
            if (rawType.isPrimitive()) {
                throw new JsonException("Cannot assign null to primitive type " + rawType.getName());
            }
            return null;
        }

        if (rawType == String.class) {
            return String.valueOf(value);
        }

        if (rawType == char.class || rawType == Character.class) {
            String str = String.valueOf(value);
            if (str.length() != 1) {
                throw new JsonException("Cannot convert value to char: " + value);
            }
            return str.charAt(0);
        }

        if (rawType == boolean.class || rawType == Boolean.class) {
            if (!(value instanceof Boolean)) {
                throw new JsonException("Expected boolean value, got " + value.getClass().getName());
            }
            return value;
        }

        if (rawType == byte.class || rawType == Byte.class) {
            return requireNumber(value).byteValue();
        }
        if (rawType == short.class || rawType == Short.class) {
            return requireNumber(value).shortValue();
        }
        if (rawType == int.class || rawType == Integer.class) {
            return requireNumber(value).intValue();
        }
        if (rawType == long.class || rawType == Long.class) {
            return requireNumber(value).longValue();
        }
        if (rawType == float.class || rawType == Float.class) {
            return requireNumber(value).floatValue();
        }
        if (rawType == double.class || rawType == Double.class) {
            return requireNumber(value).doubleValue();
        }

        if (rawType.isEnum()) {
            if (!(value instanceof String text)) {
                throw new JsonException("Expected string for enum " + rawType.getName());
            }
            return convertEnum(text, rawType);
        }

        if (rawType.isArray()) {
            if (!(value instanceof List<?> list)) {
                throw new JsonException("Expected JSON array for " + rawType.getName());
            }

            Class<?> componentType = rawType.getComponentType();
            Object array = Array.newInstance(componentType, list.size());

            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, convertValue(list.get(i), componentType, componentType));
            }

            return array;
        }

        if (Collection.class.isAssignableFrom(rawType)) {
            if (!(value instanceof List<?> list)) {
                throw new JsonException("Expected JSON array for collection " + rawType.getName());
            }

            Collection<Object> result = createCollection(rawType);
            Type elementType = extractTypeArgument(genericType, 0);
            Class<?> elementClass = rawClassOf(elementType);

            for (Object item : list) {
                result.add(convertValue(item, elementClass, elementType));
            }

            return result;
        }

        if (Map.class.isAssignableFrom(rawType)) {
            if (!(value instanceof Map<?, ?> source)) {
                throw new JsonException("Expected JSON object for map " + rawType.getName());
            }

            Map<Object, Object> result = createMap(rawType);
            Type valueType = extractTypeArgument(genericType, 1);
            Class<?> valueClass = rawClassOf(valueType);

            for (Map.Entry<?, ?> entry : source.entrySet()) {
                result.put(String.valueOf(entry.getKey()), convertValue(entry.getValue(), valueClass, valueType));
            }

            return result;
        }

        if (value instanceof Map<?, ?> map) {
            return convertObject(map, rawType);
        }

        if (rawType.isAssignableFrom(value.getClass())) {
            return value;
        }

        throw new JsonException("Cannot convert " + value.getClass().getName() + " to " + rawType.getName());
    }

    private Object convertObject(Map<?, ?> map, Class<?> rawType) {
        try {
            Constructor<?> constructor = rawType.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object obj = constructor.newInstance();

            for (Field field : getAllFields(rawType)) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                    continue;
                }

                String fieldName = field.getName();
                if (!map.containsKey(fieldName)) {
                    continue;
                }

                field.setAccessible(true);
                Object fieldValue = map.get(fieldName);
                Object converted = convertValue(fieldValue, field.getType(), field.getGenericType());
                field.set(obj, converted);
            }

            return obj;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException("Mapping error for type " + rawType.getName(), e);
        }
    }

    private Number requireNumber(Object value) {
        if (!(value instanceof Number number)) {
            throw new JsonException("Expected number value, got " + value.getClass().getName());
        }
        return number;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object convertEnum(String text, Class<?> rawType) {
        return Enum.valueOf((Class<? extends Enum>) rawType.asSubclass(Enum.class), text);
    }

    private Collection<Object> createCollection(Class<?> rawType) {
        if (rawType.isInterface()) {
            if (Set.class.isAssignableFrom(rawType)) {
                return new LinkedHashSet<>();
            }
            if (Queue.class.isAssignableFrom(rawType)) {
                return new ArrayDeque<>();
            }
            return new ArrayList<>();
        }

        try {
            Constructor<?> constructor = rawType.getDeclaredConstructor();
            constructor.setAccessible(true);
            return castCollection(constructor.newInstance());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private Map<Object, Object> createMap(Class<?> rawType) {
        if (rawType.isInterface()) {
            return new LinkedHashMap<>();
        }

        try {
            Constructor<?> constructor = rawType.getDeclaredConstructor();
            constructor.setAccessible(true);
            return castMap(constructor.newInstance());
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> castCollection(Object value) {
        return (Collection<Object>) value;
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object> castMap(Object value) {
        return (Map<Object, Object>) value;
    }

    private Type extractTypeArgument(Type genericType, int index) {
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] args = parameterizedType.getActualTypeArguments();
            if (index < args.length) {
                return args[index];
            }
        }
        return Object.class;
    }

    private Class<?> rawClassOf(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }

        if (type instanceof ParameterizedType parameterizedType && parameterizedType.getRawType() instanceof Class<?> clazz) {
            return clazz;
        }

        return Object.class;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(List.of(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }
}
