package json;

import java.lang.reflect.*;
import java.util.*;

class TypeUtils {
    static <T> T convert(Object src, Class<T> target) {
        if (src == null) return null;
        if (target.isInstance(src)) return target.cast(src);
        return (T) doConvert(src, target, null);
    }

    private static Object doConvert(Object src, Class<?> target, Type generic) {
        if (target == String.class) return src.toString();
        if (target == boolean.class || target == Boolean.class) {
            return src instanceof Boolean b ? b : Boolean.parseBoolean(src.toString());
        }
        if (Number.class.isAssignableFrom(target) || target.isPrimitive()) {
            Number n = (Number) src;
            if (target == int.class || target == Integer.class) return n.intValue();
            if (target == long.class || target == Long.class) return n.longValue();
            if (target == double.class || target == Double.class) return n.doubleValue();
            if (target == float.class || target == Float.class) return n.floatValue();
            if (target == byte.class || target == Byte.class) return n.byteValue();
            if (target == short.class || target == Short.class) return n.shortValue();
            if (target == char.class) return (char) n.intValue();
        }

        // Массивы
        if (target.isArray()) {
            List<?> list = src instanceof List<?> l ? l : Collections.singletonList(src);
            Class<?> comp = target.getComponentType();
            Object arr = Array.newInstance(comp, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(arr, i, doConvert(list.get(i), comp, null));
            }
            return arr;
        }

        // Коллекции
        if (Collection.class.isAssignableFrom(target)) {
            List<?> list = src instanceof List<?> l ? l : Collections.singletonList(src);
            Collection<Object> coll = createCollection(target);
            Type elemType = extractElementType(generic);
            for (Object item : list) {
                coll.add(elemType instanceof Class<?> c ? doConvert(item, c, null) : item);
            }
            return coll;
        }

        // Map
        if (Map.class.isAssignableFrom(target)) {
            Map<?, ?> srcMap = (Map<?, ?>) src;
            Map<Object, Object> result = createMap(target);
            for (var e : srcMap.entrySet()) {
                result.put(e.getKey(), e.getValue()); // упрощённо
            }
            return result;
        }

        return mapToBean(src, target);
    }

    private static Collection<Object> createCollection(Class<?> type) {
        if (type.isInterface()) {
            if (type == List.class) return new ArrayList<>();
            if (type == Set.class) return new LinkedHashSet<>();
            throw new JsonLib.JsonException("Unsupported collection interface: " + type);
        }
        try {
            return (Collection<Object>) type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new JsonLib.JsonException("Cannot create: " + type, e);
        }
    }

    private static Map<Object, Object> createMap(Class<?> type) {
        if (type.isInterface()) {
            if (type == Map.class) return new LinkedHashMap<>();
            throw new JsonLib.JsonException("Unsupported map interface: " + type);
        }
        try {
            return (Map<Object, Object>) type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new JsonLib.JsonException("Cannot create: " + type, e);
        }
    }

    private static Type extractElementType(Type generic) {
        if (generic instanceof ParameterizedType pt && pt.getActualTypeArguments().length > 0) {
            return pt.getActualTypeArguments()[0];
        }
        return Object.class;
    }

    private static Object mapToBean(Object src, Class<?> target) {
        Map<?, ?> data = src instanceof Map<?, ?> m ? m : Collections.emptyMap();
        try {
            java.lang.reflect.Constructor<?> constructor = target.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object bean = constructor.newInstance();

            for (Field f : target.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) ||
                        java.lang.reflect.Modifier.isTransient(f.getModifiers())) continue;
                f.setAccessible(true);
                Object val = data.get(f.getName());
                if (val != null) {
                    f.set(bean, doConvert(val, f.getType(), f.getGenericType()));
                }
            }
            return bean;
        } catch (Exception e) {
            throw new JsonLib.JsonException("Failed to map to " + target, e);
        }
    }
}