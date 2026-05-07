import java.lang.reflect.*;
import java.util.*;

class JsonCast {

    private void JsonCast(){};

    @SuppressWarnings("unchecked")
    static <T> T convert(Object obj, Type targetType) {
        try {
            if (targetType == null) return null;

            if (targetType instanceof Class<?> cls && cls.isArray())
                return (T) convertArray(obj, cls.getComponentType());
            else if (targetType instanceof GenericArrayType gArr)
                return (T) convertArray(obj, gArr.getGenericComponentType());
            else if (targetType instanceof Class<?> cls)
                return (T) convertClass(obj, cls);
            else if (targetType instanceof ParameterizedType pType) {
                Class<?> rawType = (Class<?>) pType.getRawType();

                if (Collection.class.isAssignableFrom(rawType))
                    return (T) convertCollection(obj, rawType, pType.getActualTypeArguments()[0]);

                throw new IllegalArgumentException("Unsupported ParameterizedType: " + targetType);
            } else throw new IllegalArgumentException("Unsupported type: " + targetType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object convertArray(Object obj, Type elType) throws Exception {
        if (obj == null) return null;
        if (!(obj instanceof Collection<?>)) throw new RuntimeException();

        Collection c = (Collection<?>) obj;

        Object res = Array.newInstance(getRawCls(elType), c.size());
        int i = 0;
        for (var el : c) Array.set(res, i++, convert(el, elType));
        return res;
    }

    private static Class<?> getRawCls(Type type) {
        if (type instanceof Class<?> cls) return cls;
        if (type instanceof ParameterizedType pType) return (Class<?>) pType.getRawType();
        if (type instanceof GenericArrayType gArray) {
            Class<?> comp = getRawCls(gArray.getGenericComponentType());
            return Array.newInstance(comp, 0).getClass();
        }
        return Object.class;
    }

    private static Object convertClass(Object obj, Class<?> cls) throws Exception {
        if (cls.isPrimitive()) {
            if (obj == null) throw new RuntimeException("Primitive type " + cls.getSimpleName() + " can't be null");

            if (cls == long.class    && (obj instanceof Long || obj instanceof Integer || obj instanceof Short || obj instanceof Byte) ||
                    cls == int.class     && (obj instanceof Integer || obj instanceof Short || obj instanceof Byte) ||
                    cls == short.class   && (obj instanceof Short || obj instanceof Byte) ||
                    cls == byte.class    && (obj instanceof Byte) ||
                    cls == double.class  && (obj instanceof Double || obj instanceof Float) ||
                    cls == float.class   && (obj instanceof Float) ||
                    cls == boolean.class && (obj instanceof Boolean) ||
                    cls == char.class    && (obj instanceof Character)) {
                return obj;
            }
            throw new RuntimeException("Can't cast " + obj.getClass().getSimpleName() + " to primitive " + cls.getSimpleName());
        }
        else if (obj == null) return null;
        else if (Collection.class.isAssignableFrom(cls) && !cls.isInstance(obj))
            return convertCollection(obj,cls,Object.class);
        else if (cls.isInstance(obj)) return cls.cast(obj);
        else if (obj instanceof Map<?, ?> map) {
            Map<String, Object> fieldMap = (Map<String, Object>) map;
            Constructor<?> cnstr = cls.getDeclaredConstructor();
            cnstr.setAccessible(true);
            Object res = cnstr.newInstance();

            for (Field fld : cls.getDeclaredFields()) {
                int md = fld.getModifiers();
                if (Modifier.isStatic(md) || Modifier.isFinal(md)) continue;
                fld.setAccessible(true);

                String fldName = fld.getName();
                if (!fieldMap.containsKey(fldName)) {
                    throw new RuntimeException("Missing field in map: " + fldName);
                }

                fld.set(res, convert(fieldMap.get(fldName), fld.getGenericType()));
                fieldMap.remove(fldName);
            }

            if (!fieldMap.isEmpty()) {
                throw new RuntimeException("Unused keys in map: " + fieldMap.keySet());
            }
            return cls.cast(res);
        }
        throw new RuntimeException(obj.getClass().getSimpleName() + " can't be converted to " + cls.getSimpleName());
    }

    private static Object convertCollection(Object obj, Class<?> rawType, Type elementType) throws Exception {
        if (obj == null) return null;
        if (!(obj instanceof Collection<?> srcCollection)) {
            throw new IllegalArgumentException("Expected Collection for " + rawType.getSimpleName());
        }

        Collection<Object> result = instantiateCollection(rawType);
        for (Object item : srcCollection)
            result.add(convert(item, elementType));
        return result;
    }

    private static Collection<Object> instantiateCollection(Class<?> rawType) throws Exception {
        if (rawType.isInterface()) {
            if (List.class.isAssignableFrom(rawType) || Queue.class.isAssignableFrom(rawType))
                return new LinkedList<>();
            if (Set.class.isAssignableFrom(rawType))
                return new TreeSet<>();
            throw new RuntimeException("Unsupported Collection interface: " + rawType.getSimpleName());
        }
        Constructor<?> cnstr = rawType.getDeclaredConstructor();
        cnstr.setAccessible(true);
        return (Collection<Object>) cnstr.newInstance();
    }

}
