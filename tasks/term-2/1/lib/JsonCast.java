import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class JsonCast {

    private static final ConcurrentHashMap<Class<?>, List<FieldMeta>> fieldsCache = new ConcurrentHashMap<>();

    private static List<FieldMeta> cachedFields(Class<?> cls) {
        return fieldsCache.computeIfAbsent(cls, c -> {
            List<FieldMeta> result = new ArrayList<>();
            for (Class<?> cur = c; cur != null && cur != Object.class; cur = cur.getSuperclass()) {
                for (Field fld : cur.getDeclaredFields()) {
                    int md = fld.getModifiers();
                    if (Modifier.isStatic(md) || Modifier.isTransient(md)) continue;
                    fld.setAccessible(true);
                    result.add(new FieldMeta(fld));
                }
            }
            return Collections.unmodifiableList(result);
        });
    }

    private static final class FieldMeta {
        final Field  field;
        final String name;
        final Type   genericType;

        FieldMeta(Field field) {
            this.field       = field;
            this.name        = field.getName();
            this.genericType = field.getGenericType();
        }
    }

    private JsonCast() {}

    @SuppressWarnings("unchecked")
    static <T> T convert(Object obj, Type targetType) {
        return convert(obj, targetType, JsonConfig.defaultConfig());
    }

    @SuppressWarnings("unchecked")
    static <T> T convert(Object obj, Type targetType, JsonConfig config) {
        try {
            if (targetType == null) return null;

            if (targetType instanceof Class<?> cls && cls.isArray())
                return (T) convertArray(obj, cls.getComponentType(), config);
            else if (targetType instanceof GenericArrayType gArr)
                return (T) convertArray(obj, gArr.getGenericComponentType(), config);
            else if (targetType instanceof Class<?> cls)
                return (T) convertClass(obj, cls, config);
            else if (targetType instanceof ParameterizedType pType) {
                Class<?> rawType = (Class<?>) pType.getRawType();

                if (Collection.class.isAssignableFrom(rawType))
                    return (T) convertCollection(obj, rawType, pType.getActualTypeArguments()[0], config);
                if (Map.class.isAssignableFrom(rawType))
                    return (T) convertMap(obj, rawType, pType.getActualTypeArguments(), config);

                throw new IllegalArgumentException("Unsupported ParameterizedType: " + targetType);
            } else throw new IllegalArgumentException("Unsupported type: " + targetType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object convertArray(Object obj, Type elType, JsonConfig config) throws Exception {
        if (obj == null) return null;
        if (!(obj instanceof Collection<?>)) throw new RuntimeException("Expected array/list, got: " + obj.getClass());

        Collection<?> c = (Collection<?>) obj;
        Object res = Array.newInstance(getRawCls(elType), c.size());
        int i = 0;
        for (var el : c) Array.set(res, i++, convert(el, elType, config));
        return res;
    }

    private static Class<?> getRawCls(Type type) {
        if (type instanceof Class<?> cls)            return cls;
        if (type instanceof ParameterizedType pType) return (Class<?>) pType.getRawType();
        if (type instanceof GenericArrayType gArray) {
            Class<?> comp = getRawCls(gArray.getGenericComponentType());
            return Array.newInstance(comp, 0).getClass();
        }
        return Object.class;
    }

    private static Object convertClass(Object obj, Class<?> cls, JsonConfig config) throws Exception {
        if (cls.isPrimitive()) {
            if (obj == null) throw new RuntimeException("Primitive type " + cls.getSimpleName() + " can't be null");

            if (obj instanceof Number num) {
                if (cls == double.class) return num.doubleValue();
                if (cls == float.class)  return num.floatValue();
                if (cls == long.class)   return num.longValue();
                if (cls == int.class)    return num.intValue();
                if (cls == short.class)  return num.shortValue();
                if (cls == byte.class)   return num.byteValue();
            }
            if (cls == boolean.class && obj instanceof Boolean)   return obj;
            if (cls == char.class    && obj instanceof Character) return obj;

            throw new RuntimeException("Can't cast " + obj.getClass().getSimpleName() + " to primitive " + cls.getSimpleName());
        }

        if (obj == null) return null;
        if (Collection.class.isAssignableFrom(cls) && !cls.isInstance(obj))
            return convertCollection(obj, cls, Object.class, config);
        if (cls.isInstance(obj)) return cls.cast(obj);

        if (obj instanceof Map<?, ?> map) {
            Map<String, Object> fieldMap = new HashMap<>((Map<String, Object>) map);

            Constructor<?> cnstr = cls.getDeclaredConstructor();
            cnstr.setAccessible(true);
            Object res = cnstr.newInstance();

            for (FieldMeta meta : cachedFields(cls)) {
                if (Modifier.isFinal(meta.field.getModifiers())) continue;
                if (fieldMap.containsKey(meta.name)) {
                    meta.field.set(res, convert(fieldMap.remove(meta.name), meta.genericType, config));
                }
            }

            if (!fieldMap.isEmpty() && !config.ignoreUnknownFields) {
                throw new RuntimeException("Unused keys in JSON: " + fieldMap.keySet());
            }
            return cls.cast(res);
        }

        throw new RuntimeException(obj.getClass().getSimpleName() + " can't be converted to " + cls.getSimpleName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convertMap(Object obj, Class<?> rawType, Type[] typeArgs, JsonConfig config) throws Exception {
        if (obj == null) return null;
        if (!(obj instanceof Map<?, ?> srcMap))
            throw new IllegalArgumentException("Expected Map, got: " + obj.getClass());

        Type keyType   = typeArgs[0];
        Type valueType = typeArgs[1];

        Map result = rawType.isInterface() ? new LinkedHashMap<>()
                                           : (Map) rawType.getDeclaredConstructor().newInstance();
        for (var entry : srcMap.entrySet()) {
            Object k = convert(entry.getKey(),   keyType,   config);
            Object v = convert(entry.getValue(), valueType, config);
            result.put(k, v);
        }
        return result;
    }

    private static Object convertCollection(Object obj, Class<?> rawType, Type elementType, JsonConfig config) throws Exception {
        if (obj == null) return null;
        if (!(obj instanceof Collection<?> srcCollection))
            throw new IllegalArgumentException("Expected Collection for " + rawType.getSimpleName());

        Collection<Object> result = instantiateCollection(rawType);
        for (Object item : srcCollection)
            result.add(convert(item, elementType, config));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> instantiateCollection(Class<?> rawType) throws Exception {
        if (rawType.isInterface()) {
            if (List.class.isAssignableFrom(rawType) || Queue.class.isAssignableFrom(rawType))
                return new LinkedList<>();
            if (Set.class.isAssignableFrom(rawType))
                return new TreeSet<>();
            if (Deque.class.isAssignableFrom(rawType))
                return new ArrayDeque<>();
            throw new RuntimeException("Unsupported Collection interface: " + rawType.getSimpleName());
        }
        Constructor<?> cnstr = rawType.getDeclaredConstructor();
        cnstr.setAccessible(true);
        return (Collection<Object>) cnstr.newInstance();
    }
}
