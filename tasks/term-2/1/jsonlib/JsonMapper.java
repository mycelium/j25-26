package jsonlib;

import java.lang.reflect.*;
import java.util.*;

class JsonMapper {

    // ── Public entry point ────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    static <T> T map(Object source, Class<T> targetClass) {
        return (T) convert(source, targetClass);
    }

    // ── Core dispatcher ───────────────────────────────────────────────────────

    static Object convert(Object src, Type targetType) {
        if (src == null) return null;

        if (targetType instanceof ParameterizedType)
            return convertParameterized(src, (ParameterizedType) targetType);

        if (targetType instanceof Class<?>)
            return convertClass(src, (Class<?>) targetType);

        throw new RuntimeException("Unsupported target type: " + targetType);
    }

    // ── Parameterized types (List<X>, Map<K,V>, …) ────────────────────────────

    private static Object convertParameterized(Object src, ParameterizedType pt) {
        Class<?> raw  = (Class<?>) pt.getRawType();
        Type[]   args = pt.getActualTypeArguments();

        if (Collection.class.isAssignableFrom(raw))
            return buildCollection(src, raw, args.length > 0 ? args[0] : Object.class);

        if (Map.class.isAssignableFrom(raw)) {
            Type kt = args.length > 0 ? args[0] : Object.class;
            Type vt = args.length > 1 ? args[1] : Object.class;
            return buildMap(src, raw, kt, vt);
        }

        throw new RuntimeException("Unsupported parameterized type: " + pt);
    }

    // ── Raw class dispatch ────────────────────────────────────────────────────

    private static Object convertClass(Object src, Class<?> cls) {
        if (cls == String.class)
            return src.toString();

        if (cls == boolean.class || cls == Boolean.class)
            return src instanceof Boolean ? src : Boolean.parseBoolean(src.toString());

        if (cls.isPrimitive() || Number.class.isAssignableFrom(cls))
            return coerceNumber((Number) src, cls);

        if (cls.isArray())
            return buildArray(src, cls.getComponentType());

        if (Collection.class.isAssignableFrom(cls))
            return buildCollection(src, cls, Object.class);

        if (Map.class.isAssignableFrom(cls))
            return buildMap(src, cls, Object.class, Object.class);

        return populateBean(src, cls);
    }

    // ── Number coercion ───────────────────────────────────────────────────────

    private static Number coerceNumber(Number n, Class<?> cls) {
        if (cls == int.class    || cls == Integer.class) return n.intValue();
        if (cls == long.class   || cls == Long.class)    return n.longValue();
        if (cls == double.class || cls == Double.class)  return n.doubleValue();
        if (cls == float.class  || cls == Float.class)   return n.floatValue();
        if (cls == short.class  || cls == Short.class)   return n.shortValue();
        if (cls == byte.class   || cls == Byte.class)    return n.byteValue();
        if (cls == char.class)                           return (int) n.intValue();
        throw new RuntimeException("No numeric coercion for: " + cls);
    }

    // ── Array ─────────────────────────────────────────────────────────────────

    private static Object buildArray(Object src, Class<?> component) {
        List<?> list = (List<?>) src;
        Object  arr  = Array.newInstance(component, list.size());
        for (int i = 0; i < list.size(); i++)
            Array.set(arr, i, convert(list.get(i), component));
        return arr;
    }

    // ── Collection ────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static Collection<Object> buildCollection(Object src, Class<?> cls, Type elemType) {
        List<?>             list   = (List<?>) src;
        Collection<Object>  target = newCollection(cls);
        for (Object item : list)
            target.add(convert(item, elemType));
        return target;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> newCollection(Class<?> cls) {
        if (!cls.isInterface()) {
            try { return (Collection<Object>) cls.getDeclaredConstructor().newInstance(); }
            catch (Exception e) { throw new RuntimeException("Cannot create: " + cls, e); }
        }
        if (cls == List.class) return new ArrayList<>();
        if (cls == Set.class)  return new LinkedHashSet<>();
        throw new RuntimeException("Cannot instantiate collection interface: " + cls);
    }

    // ── Map ───────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> buildMap(Object src, Class<?> cls, Type kt, Type vt) {
        Map<?, ?>           srcMap = (Map<?, ?>) src;
        Map<Object, Object> dest   = newMap(cls);
        for (Map.Entry<?, ?> e : srcMap.entrySet()) {
            Object k = (kt == String.class || kt == Object.class)
                    ? e.getKey()
                    : convert(e.getKey(), kt);
            dest.put(k, convert(e.getValue(), vt));
        }
        return dest;
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> newMap(Class<?> cls) {
        if (!cls.isInterface()) {
            try { return (Map<Object, Object>) cls.getDeclaredConstructor().newInstance(); }
            catch (Exception e) { throw new RuntimeException("Cannot create: " + cls, e); }
        }
        if (cls == Map.class) return new LinkedHashMap<>();
        throw new RuntimeException("Cannot instantiate map interface: " + cls);
    }

    // ── POJO population ───────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static Object populateBean(Object src, Class<?> cls) {
        Map<String, Object> data = (Map<String, Object>) src;
        try {
            Object obj = cls.getDeclaredConstructor().newInstance();
            for (Field f : cls.getDeclaredFields()) {
                f.setAccessible(true);
                Object raw = data.get(f.getName());
                if (raw != null) f.set(obj, convert(raw, f.getGenericType()));
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Cannot populate " + cls.getSimpleName(), e);
        }
    }
}