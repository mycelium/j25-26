package tokenizer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonMapper {

    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        return fromJsonValue(map, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJsonValue(Object jsonValue, Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }
        if (jsonValue == null) {
            if (clazz.isPrimitive()) {
                throw new IllegalArgumentException("Cannot assign null to primitive " + clazz.getName());
            }
            return null;
        }

        if (clazz == Object.class) {
            return (T) jsonValue;
        }
        if (clazz == String.class) {
            return (T) String.valueOf(jsonValue);
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) toBoolean(jsonValue, clazz);
        }
        if (Number.class.isAssignableFrom(wrap(clazz)) || clazz.isPrimitive()) {
            return (T) toNumber(jsonValue, clazz);
        }
        if (clazz.isArray()) {
            return (T) toArray(jsonValue, clazz.getComponentType());
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return (T) toCollection(jsonValue, clazz, Object.class);
        }
        if (clazz.isEnum()) {
            return (T) toEnum(jsonValue, (Class<? extends Enum<?>>) clazz);
        }

        if (!(jsonValue instanceof Map)) {
            throw new IllegalArgumentException("Expected object for class " + clazz.getName());
        }

        T instance = newInstance(clazz);
        Map<String, Object> map = (Map<String, Object>) jsonValue;
        for (Field field : getAllFields(clazz)) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            if (!map.containsKey(field.getName())) {
                continue;
            }

            field.setAccessible(true);
            Object rawValue = map.get(field.getName());
            Object mappedValue = mapFieldValue(field, rawValue);
            try {
                field.set(instance, mappedValue);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Cannot set field " + field.getName(), e);
            }
        }

        return instance;
    }

    private static Object mapFieldValue(Field field, Object rawValue) {
        Class<?> targetType = field.getType();
        if (Collection.class.isAssignableFrom(targetType)) {
            Class<?> elementType = resolveElementType(field.getGenericType());
            return toCollection(rawValue, targetType, elementType);
        }
        if (targetType.isArray()) {
            return toArray(rawValue, targetType.getComponentType());
        }
        return fromJsonValue(rawValue, targetType);
    }

    @SuppressWarnings("unchecked")
    private static Object toCollection(Object rawValue, Class<?> collectionType, Class<?> elementType) {
        if (rawValue == null) {
            return null;
        }
        if (!(rawValue instanceof List)) {
            throw new IllegalArgumentException("Expected array for collection");
        }
        List<Object> sourceList = (List<Object>) rawValue;
        Collection<Object> result = instantiateCollection((Class<? extends Collection<?>>) collectionType);
        for (int i = 0; i < sourceList.size(); i++) {
            Object item = sourceList.get(i);
            Object mappedItem = fromJsonValue(item, elementType);
            result.add(mappedItem);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object toArray(Object rawValue, Class<?> componentType) {
        if (rawValue == null) {
            return null;
        }
        if (!(rawValue instanceof List)) {
            throw new IllegalArgumentException("Expected array for field");
        }
        List<Object> sourceList = (List<Object>) rawValue;
        Object array = Array.newInstance(componentType, sourceList.size());
        for (int i = 0; i < sourceList.size(); i++) {
            Array.set(array, i, fromJsonValue(sourceList.get(i), componentType));
        }
        return array;
    }

    private static Object toBoolean(Object value, Class<?> targetType) {
        if (value instanceof Boolean) {
            return value;
        }
        throw new IllegalArgumentException("Cannot convert to " + targetType.getName() + ": " + value);
    }

    private static Object toNumber(Object value, Class<?> targetType) {
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Cannot convert to " + targetType.getName() + ": " + value);
        }
        Class<?> wrapped = wrap(targetType);
        if (wrapped == Integer.class) {
            return number.intValue();
        }
        if (wrapped == Long.class) {
            return number.longValue();
        }
        if (wrapped == Double.class) {
            return number.doubleValue();
        }
        if (wrapped == Float.class) {
            return number.floatValue();
        }
        if (wrapped == Short.class) {
            return number.shortValue();
        }
        if (wrapped == Byte.class) {
            return number.byteValue();
        }
        throw new IllegalArgumentException("Unsupported numeric type: " + targetType.getName());
    }

    private static Enum<?> toEnum(Object value, Class<? extends Enum<?>> enumClass) {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Enum value must be string for " + enumClass.getName());
        }
        String enumName = (String) value;
        for (Enum<?> constant : enumClass.getEnumConstants()) {
            if (constant.name().equals(enumName)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + enumName);
    }

    private static Class<?> resolveElementType(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class) {
                return (Class<?>) args[0];
            }
        }
        return Object.class;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> instantiateCollection(Class<? extends Collection<?>> collectionType) {
        if (collectionType.isInterface()) {
            if (Set.class.isAssignableFrom(collectionType)) {
                return new LinkedHashSet<>();
            }
            return new ArrayList<>();
        }
        try {
            return (Collection<Object>) collectionType.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            if (Set.class.isAssignableFrom(collectionType)) {
                return new LinkedHashSet<>();
            }
            return new ArrayList<>();
        }
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("No no-arg constructor for " + clazz.getName(), e);
        }
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                fields.add(field);
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }
}
