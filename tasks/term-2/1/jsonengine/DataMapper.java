package jsonengine;

import java.lang.reflect.*;
import java.util.*;

class DataMapper {
    static <T> T convert(Object source, Class<T> type) {
        return (T) transform(source, (Type) type);
    }

    private static Object transform(Object data, Type target) {
        if (data == null) return null;

        if (target instanceof Class<?> clazz) {
            if (clazz == String.class) return String.valueOf(data);
            if (clazz == Boolean.class || clazz == boolean.class) return Boolean.valueOf(data.toString());
            
            if (clazz.isPrimitive() || Number.class.isAssignableFrom(clazz)) {
                return castNumber((Number) data, clazz);
            }
            if (clazz.isArray()) {
                return convertToArray(data, clazz.getComponentType());
            }
            if (Collection.class.isAssignableFrom(clazz)) {
                return convertToCollection(data, clazz, null);
            }
            if (Map.class.isAssignableFrom(clazz)) {
                return convertToMap(data, clazz, null, null);
            }
            return bindToObject(data, clazz);
        }

        if (target instanceof ParameterizedType pt) {
            Class<?> raw = (Class<?>) pt.getRawType();
            Type[] args = pt.getActualTypeArguments();
            if (Collection.class.isAssignableFrom(raw)) {
                return convertToCollection(data, raw, args[0]);
            }
            if (Map.class.isAssignableFrom(raw)) {
                return convertToMap(data, raw, args[0], args[1]);
            }
        }
        throw new JsonException("Unsupported type: " + target);
    }

    private static Object castNumber(Number n, Class<?> target) {
        if (target == int.class || target == Integer.class) return n.intValue();
        if (target == long.class || target == Long.class) return n.longValue();
        if (target == double.class || target == Double.class) return n.doubleValue();
        if (target == float.class || target == Float.class) return n.floatValue();
        if (target == short.class || target == Short.class) return n.shortValue();
        if (target == byte.class || target == Byte.class) return n.byteValue();
        if (target == char.class) return (char) n.intValue();
        throw new JsonException("Cannot cast number to " + target);
    }

    private static Object convertToArray(Object src, Class<?> component) {
        List<?> list = (List<?>) src;
        Object arr = Array.newInstance(component, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(arr, i, transform(list.get(i), component));
        }
        return arr;
    }

    private static Object convertToCollection(Object src, Class<?> type, Type component) {
        List<?> sourceList = (List<?>) src;
        Collection<Object> result;
        if (type.isInterface()) {
            result = (type == Set.class) ? new LinkedHashSet<>() : new ArrayList<>();
        } else {
            try {
                result = (Collection<Object>) type.getDeclaredConstructor().newInstance();
            } catch (Exception e) { throw new JsonException("Init error", e); }
        }
        for (Object item : sourceList) {
            result.add(component != null ? transform(item, component) : item);
        }
        return result;
    }

    private static Object convertToMap(Object src, Class<?> type, Type keyType, Type valType) {
        Map<?, ?> sourceMap = (Map<?, ?>) src;
        Map<Object, Object> result;
        if (type.isInterface()) {
            result = new LinkedHashMap<>();
        } else {
            try {
                result = (Map<Object, Object>) type.getDeclaredConstructor().newInstance();
            } catch (Exception e) { throw new JsonException("Init error", e); }
        }
        for (Map.Entry<?, ?> e : sourceMap.entrySet()) {
            Object key = (keyType != null && keyType != String.class) ? transform(e.getKey(), keyType) : e.getKey();
            result.put(key, valType != null ? transform(e.getValue(), valType) : e.getValue());
        }
        return result;
    }

    private static Object bindToObject(Object src, Class<?> clazz) {
        Map<String, Object> map = (Map<String, Object>) src;
        try {
            Object obj = clazz.getDeclaredConstructor().newInstance();
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                Object val = map.get(f.getName());
                if (val != null) f.set(obj, transform(val, f.getGenericType()));
            }
            return obj;
        } catch (Exception e) { throw new JsonException("Binding error for " + clazz.getName(), e); }
    }
}