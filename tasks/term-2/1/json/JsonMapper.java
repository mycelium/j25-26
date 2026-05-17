package json;

import java.lang.reflect.*;
import java.util.*;

class JsonMapper {

    private JsonMapper() {}

    static <T> T map(Object parsed, Class<T> clazz) {
        return clazz.cast(convert(parsed, clazz, clazz));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static Object convert(Object value, Class<?> rawType, Type genericType) {
        if (value == null) return defaultValue(rawType);

        if (rawType == boolean.class || rawType == Boolean.class) return value;
        if (rawType == String.class)  return String.valueOf(value);
        if (rawType == char.class || rawType == Character.class) return toChar(value);
        if (rawType == byte.class || rawType == Byte.class) return ((Number) value).byteValue();
        if (rawType == short.class || rawType == Short.class) return ((Number) value).shortValue();
        if (rawType == int.class || rawType == Integer.class) return toInt(value);
        if (rawType == long.class || rawType == Long.class) return toLong(value);
        if (rawType == float.class || rawType == Float.class) return toFloat(value);
        if (rawType == double.class || rawType == Double.class) return toDouble(value);
        if (rawType == Object.class) return value;

        if (rawType.isEnum()) {
            return Enum.valueOf((Class<Enum>) rawType, String.valueOf(value));
        }

        // arrays
        if (rawType.isArray() && value instanceof List<?> list) {
            return convertArray(list, rawType.getComponentType());
        }

        // collections
        if (value instanceof List<?> list && Collection.class.isAssignableFrom(rawType)) {
            return convertCollection(list, rawType, genericType);
        }

        // maps
        if (value instanceof Map<?, ?> srcMap && Map.class.isAssignableFrom(rawType)) {
            return convertMap(srcMap, rawType, genericType);
        }

        // objects
        if (value instanceof Map<?, ?> srcMap) {
            return convertObject((Map<String, Object>) srcMap, rawType);
        }

        if (rawType.isInstance(value)) return value;

        throw new JsonException("Cannot convert " + value.getClass().getSimpleName()
                + " to " + rawType.getSimpleName());
    }

    private static Object convertArray(List<?> list, Class<?> componentType) {
        var array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, convert(list.get(i), componentType, componentType));
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> convertCollection(List<?> src, Class<?> collType, Type genericType) {
        var collection = createCollection(collType);
        Class<?> elementType = Object.class;
        Type elementGeneric = Object.class;
        if (genericType instanceof ParameterizedType pt) {
            elementGeneric = pt.getActualTypeArguments()[0];
            elementType = rawType(elementGeneric);
        }
        for (var item : src) {
            collection.add(convert(item, elementType, elementGeneric));
        }
        return collection;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> createCollection(Class<?> type) {
        if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
            try {
                return (Collection<Object>) type.getDeclaredConstructor().newInstance();
            } catch (Exception ignored) {}
        }
        if (Set.class.isAssignableFrom(type)) return new LinkedHashSet<>();
        if (Queue.class.isAssignableFrom(type) || Deque.class.isAssignableFrom(type)) return new ArrayDeque<>();
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> convertMap(Map<?, ?> src, Class<?> mapType, Type genericType) {
        var result = createMap(mapType);
        Class<?> keyType = String.class;
        Class<?> valType = Object.class;
        Type keyGeneric = String.class;
        Type valGeneric = Object.class;
        if (genericType instanceof ParameterizedType pt) {
            keyGeneric = pt.getActualTypeArguments()[0];
            valGeneric = pt.getActualTypeArguments()[1];
            keyType = rawType(keyGeneric);
            valType = rawType(valGeneric);
        }
        for (var entry : src.entrySet()) {
            var k = convert(entry.getKey(), keyType, keyGeneric);
            var v = convert(entry.getValue(), valType, valGeneric);
            result.put(k, v);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> createMap(Class<?> type) {
        if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
            try {
                return (Map<Object, Object>) type.getDeclaredConstructor().newInstance();
            } catch (Exception ignored) {}
        }
        return new LinkedHashMap<>();
    }

    private static Object convertObject(Map<String, Object> src, Class<?> clazz) {
        if (clazz.isRecord()) return convertRecord(src, clazz);
        return convertPojo(src, clazz);
    }

    private static Object convertPojo(Map<String, Object> src, Class<?> clazz) {
        try {
            var instance = createInstance(clazz);
            for (var c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
                for (var field : c.getDeclaredFields()) {
                    int mods = field.getModifiers();
                    if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) continue;
                    if (!src.containsKey(field.getName())) continue;
                    field.setAccessible(true);
                    var rawType = field.getType();
                    var generic = field.getGenericType();
                    field.set(instance, convert(src.get(field.getName()), rawType, generic));
                }
            }
            return instance;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException("Failed to map JSON to " + clazz.getSimpleName(), e);
        }
    }

    private static Object convertRecord(Map<String, Object> src, Class<?> clazz) {
        try {
            var components = clazz.getRecordComponents();
            var paramTypes = new Class<?>[components.length];
            var args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                paramTypes[i] = components[i].getType();
                var value = src.get(components[i].getName());
                args[i] = convert(value, components[i].getType(), components[i].getGenericType());
            }
            var ctor = clazz.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException("Failed to map JSON to record " + clazz.getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInstance(Class<T> clazz) throws Exception {
        try {
            var ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new JsonException(clazz.getSimpleName()
                    + " has no no-arg constructor. Use a record or add one.");
        }
    }

    static Class<?> rawType(Type type) {
        return switch (type) {
            case Class<?> c -> c;
            case ParameterizedType pt -> (Class<?>) pt.getRawType();
            default -> Object.class;
        };
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        return switch (type.getName()) {
            case "boolean" -> false;
            case "char" -> '\0';
            case "byte" -> (byte)  0;
            case "short" -> (short) 0;
            case "int" -> 0;
            case "long" -> 0L;
            case "float" -> 0.0f;
            case "double" -> 0.0;
            default -> null;
        };
    }

    private static int toInt(Object v) {
        return switch (v) {
            case Integer i -> i;
            case Long l -> l.intValue();
            case Double d -> d.intValue();
            case String s -> Integer.parseInt(s);
            default -> throw new JsonException("Cannot coerce " + v.getClass().getSimpleName() + " to int");
        };
    }

    private static long toLong(Object v) {
        return switch (v) {
            case Integer i -> i.longValue();
            case Long l -> l;
            case Double d -> d.longValue();
            case String s -> Long.parseLong(s);
            default -> throw new JsonException("Cannot coerce " + v.getClass().getSimpleName() + " to long");
        };
    }

    private static float toFloat(Object v) {
        return switch (v) {
            case Float f -> f;
            case Double d -> d.floatValue();
            case Integer i -> i.floatValue();
            case Long l -> l.floatValue();
            case String s -> Float.parseFloat(s);
            default -> throw new JsonException("Cannot coerce " + v.getClass().getSimpleName() + " to float");
        };
    }

    private static double toDouble(Object v) {
        return switch (v) {
            case Double d -> d;
            case Float f -> f.doubleValue();
            case Integer i -> i.doubleValue();
            case Long l -> l.doubleValue();
            case String s -> Double.parseDouble(s);
            default -> throw new JsonException("Cannot coerce " + v.getClass().getSimpleName() + " to double");
        };
    }

    private static char toChar(Object v) {
        var s = String.valueOf(v);
        if (s.isEmpty()) return '\0';
        return s.charAt(0);
    }
}
