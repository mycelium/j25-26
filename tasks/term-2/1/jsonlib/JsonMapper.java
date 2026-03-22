package jsonlib;

import java.lang.reflect.*;
import java.util.*;

class JsonMapper {
    static <T> T map(Object source, Class<T> targetClass) {
        return (T) map(source, (Type) targetClass);
    }

    static Object map(Object source, Type targetType) {
        if (source == null) return null;

        if (targetType instanceof Class) {
            Class<?> clazz = (Class<?>) targetType;
            if (clazz == String.class) return source.toString();
            if (clazz == Boolean.class || clazz == boolean.class) {
                return source instanceof Boolean ? source : Boolean.parseBoolean(source.toString());
            }
            if (clazz.isPrimitive() || Number.class.isAssignableFrom(clazz)) {
                Number num = (Number) source;
                if (clazz == int.class || clazz == Integer.class) return num.intValue();
                if (clazz == long.class || clazz == Long.class) return num.longValue();
                if (clazz == double.class || clazz == Double.class) return num.doubleValue();
                if (clazz == float.class || clazz == Float.class) return num.floatValue();
                if (clazz == byte.class || clazz == Byte.class) return num.byteValue();
                if (clazz == short.class || clazz == Short.class) return num.shortValue();
                if (clazz == char.class) return (char) num.intValue();
                throw new RuntimeException("Unsupported number type: " + clazz);
            }
            if (clazz.isArray()) {
                return mapArray(source, clazz.getComponentType());
            }
            if (Collection.class.isAssignableFrom(clazz)) {
                return mapCollection(source, clazz, null);
            }
            if (Map.class.isAssignableFrom(clazz)) {
                return mapMap(source, clazz, null, null);
            }
            return mapObject(source, clazz);
        }

        if (targetType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) targetType;
            Class<?> rawClass = (Class<?>) paramType.getRawType();
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (Collection.class.isAssignableFrom(rawClass)) {
                Type elemType = typeArgs.length > 0 ? typeArgs[0] : Object.class;
                return mapCollection(source, rawClass, elemType);
            }
            if (Map.class.isAssignableFrom(rawClass)) {
                Type keyType = typeArgs.length > 0 ? typeArgs[0] : Object.class;
                Type valType = typeArgs.length > 1 ? typeArgs[1] : Object.class;
                return mapMap(source, rawClass, keyType, valType);
            }
            throw new RuntimeException("Unsupported parameterized type: " + targetType);
        }

        throw new RuntimeException("Unsupported type: " + targetType);
    }

    private static Object mapArray(Object source, Class<?> componentType) {
        List<?> list = (List<?>) source;
        Object array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, map(list.get(i), componentType));
        }
        return array;
    }

    private static Object mapCollection(Object source, Class<?> collectionType, Type elementType) {
        List<?> list = (List<?>) source;
        Collection<Object> coll;
        if (collectionType.isInterface()) {
            if (collectionType == List.class) coll = new ArrayList<>();
            else if (collectionType == Set.class) coll = new LinkedHashSet<>();
            else throw new RuntimeException("Cannot instantiate collection: " + collectionType);
        } else {
            try {
                coll = (Collection<Object>) collectionType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate collection: " + collectionType, e);
            }
        }
        for (Object item : list) {
            coll.add(elementType != null ? map(item, elementType) : item);
        }
        return coll;
    }

    private static Object mapMap(Object source, Class<?> mapType, Type keyType, Type valueType) {
        Map<?, ?> srcMap = (Map<?, ?>) source;
        Map<Object, Object> targetMap;
        if (mapType.isInterface()) {
            if (mapType == Map.class) targetMap = new LinkedHashMap<>();
            else throw new RuntimeException("Cannot instantiate map: " + mapType);
        } else {
            try {
                targetMap = (Map<Object, Object>) mapType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate map: " + mapType, e);
            }
        }
        for (Map.Entry<?, ?> entry : srcMap.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (keyType != null && keyType != String.class && keyType != Object.class) {
                key = map(key, keyType);
            }
            targetMap.put(key, valueType != null ? map(value, valueType) : value);
        }
        return targetMap;
    }

    private static Object mapObject(Object source, Class<?> targetClass) {
        Map<String, Object> srcMap = (Map<String, Object>) source;
        try {
            Object instance = targetClass.getDeclaredConstructor().newInstance();
            for (Field field : targetClass.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = srcMap.get(field.getName());
                if (value != null) {
                    field.set(instance, map(value, field.getGenericType()));
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Cannot map to " + targetClass, e);
        }
    }
}