package jsonlab;

import java.lang.reflect.*;
import java.util.*;

final class ObjectBinder {
    private ObjectBinder() {}

    static <T> T bind(Map<String, Object> source, Class<T> target) {
        if (source == null) return null;
        try {
            T instance = createInstance(target);
            for (Class<?> c = target; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (isSkippable(f)) continue;
                    f.setAccessible(true);
                    String name = f.getName();
                    if (source.containsKey(name)) {
                        f.set(instance, convertValue(source.get(name), f.getGenericType()));
                    }
                }
            }
            return instance;
        } catch (JsonException e) { throw e; }
        catch (Exception e) { throw new JsonException("Binding failed for " + target.getName(), e); }
    }

    private static boolean isSkippable(Field f) {
        int m = f.getModifiers();
        return Modifier.isStatic(m) || Modifier.isTransient(m) || Modifier.isFinal(m);
    }

    @SuppressWarnings("unchecked")
    private static Object convertValue(Object src, Type targetType) {
        if (src == null) return null;
        if (targetType instanceof Class<?> tc) {
            return switch (tc) {
                case Class<?> c when c == String.class -> src.toString();
                case Class<?> c when c == Integer.class || c == int.class -> toInt(src);
                case Class<?> c when c == Long.class || c == long.class -> toLong(src);
                case Class<?> c when c == Double.class || c == double.class -> toDouble(src);
                case Class<?> c when c == Float.class || c == float.class -> ((Number) src).floatValue();
                case Class<?> c when c == Boolean.class || c == boolean.class ->
                        src instanceof Boolean b ? b : Boolean.parseBoolean(src.toString());
                case Class<?> c when c == Byte.class || c == byte.class -> ((Number) src).byteValue();
                case Class<?> c when c == Short.class || c == short.class -> ((Number) src).shortValue();
                case Class<?> c when c == Character.class || c == char.class -> {
                    String s = src.toString(); yield s.isEmpty() ? null : s.charAt(0); }
                case Class<?> c when c.isArray() -> toArray(src, c);
                case Class<?> c when Collection.class.isAssignableFrom(c) ->
                        toCollection(src, (Class<? extends Collection<?>>) c, Object.class);
                case Class<?> c when Map.class.isAssignableFrom(c) ->
                        src instanceof Map<?, ?> m ? new LinkedHashMap<>(m) :
                                fail("Cannot convert to Map");
                case Class<?> c when c.isEnum() ->
                        Enum.valueOf((Class<? extends Enum>) c, src.toString());
                default -> src instanceof Map<?, ?> m ? bind((Map<String, Object>) m, tc) : src;
            };
        }

        if (targetType instanceof ParameterizedType pt) {
            Class<?> raw = (Class<?>) pt.getRawType();
            Type[] args = pt.getActualTypeArguments();
            Type elem = args.length > 0 ? args[0] : Object.class;
            if (Collection.class.isAssignableFrom(raw)) {
                return toCollection(src, (Class<? extends Collection<?>>) raw, elem);
            }
            if (Map.class.isAssignableFrom(raw) && src instanceof Map<?, ?> m) {
                return new LinkedHashMap<>(m);
            }
        }
        return src;
    }

    private static int toInt(Object s) { return s instanceof Number n ? n.intValue() : Integer.parseInt(s.toString().trim()); }
    private static long toLong(Object s) { return s instanceof Number n ? n.longValue() : Long.parseLong(s.toString().trim()); }
    private static double toDouble(Object s) { return s instanceof Number n ? n.doubleValue() : Double.parseDouble(s.toString().trim()); }

    private static Object toArray(Object src, Class<?> arrType) {
        if (!(src instanceof Collection<?>)) {
            fail("Expected collection for array");
            return null; // unreachable, нужно для компилятора
        }
        Collection<?> c = (Collection<?>) src;
        Object arr = Array.newInstance(arrType.getComponentType(), c.size());
        int i = 0;
        for (Object v : c) {
            Array.set(arr, i++, convertValue(v, arrType.getComponentType()));
        }
        return arr;
    }

    @SuppressWarnings("unchecked")
    private static Collection<?> toCollection(Object src, Class<? extends Collection<?>> target, Type elem) {
        if (!(src instanceof Collection<?>)) {
            fail("Expected collection, got " + src.getClass());
            return null;
        }
        Collection<?> c = (Collection<?>) src;
        Collection<Object> res;

        String name = target.getSimpleName();
        if (name.equals("ArrayList") || name.equals("List")) {
            res = new ArrayList<>();
        } else if (name.equals("LinkedList")) {
            res = new LinkedList<>();
        } else if (name.equals("HashSet") || name.equals("Set")) {
            res = new HashSet<>();
        } else if (name.equals("LinkedHashSet")) {
            res = new LinkedHashSet<>();
        } else if (name.equals("TreeSet")) {
            res = new TreeSet<>();
        } else if (name.equals("ArrayDeque") || name.equals("Deque")) {
            res = new ArrayDeque<>();
        } else if (name.equals("PriorityQueue") || name.equals("Queue")) {
            res = new PriorityQueue<>();
        } else {
            try {
                res = (Collection<Object>) target.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                fail("Cannot instantiate " + target);
                return null;
            }
        }

        for (Object v : c) {
            res.add(convertValue(v, elem));
        }
        return res;
    }

    private static <T> T createInstance(Class<T> c) {
        try { Constructor<T> ctor = c.getDeclaredConstructor(); ctor.setAccessible(true); return ctor.newInstance(); }
        catch (Exception e) { throw new JsonException("Cannot instantiate " + c.getName(), e); }
    }

    private static <T> T fail(String msg) { throw new JsonException(msg); }
}