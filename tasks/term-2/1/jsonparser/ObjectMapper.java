package jsonparser;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class ObjectMapper {

    @SuppressWarnings("unchecked")
    <T> T convert(Object parsed, Class<T> clazz) {
        if (parsed == null) return null;

        if (clazz == Object.class)                          return (T) parsed;
        if (clazz == String.class)                          return clazz.cast(String.valueOf(parsed));
        if (clazz == int.class || clazz == Integer.class)  return (T) toInt(parsed);
        if (clazz == long.class || clazz == Long.class)    return (T) toLong(parsed);
        if (clazz == double.class || clazz == Double.class)return (T) toDouble(parsed);
        if (clazz == float.class || clazz == Float.class)  return (T) toFloat(parsed);
        if (clazz == boolean.class || clazz == Boolean.class) {
            if (parsed instanceof Boolean) return (T) parsed;
            throw new JsonException("Expected boolean, got: " + parsed.getClass().getSimpleName());
        }

        if (clazz.isArray() && parsed instanceof List<?> list)
            return (T) convertToArray(list, clazz.getComponentType());

        if (parsed instanceof Map<?, ?> map)
            return fromMap((Map<String, Object>) map, clazz);

        throw new JsonException("Cannot convert " + parsed.getClass().getSimpleName() + " to " + clazz.getName());
    }

    private <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        try {
            T instance = createInstance(clazz);
            for (Field field : collectFields(clazz)) {
                if (!map.containsKey(field.getName())) continue;
                field.setAccessible(true);
                field.set(instance, convertForField(map.get(field.getName()), field));
            }
            return instance;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException("Failed to map JSON to " + clazz.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Object convertForField(Object raw, Field field) {
        Class<?> type = field.getType();
        if (raw == null)                                      return null;
        if (type == String.class)                             return String.valueOf(raw);
        if (type == int.class || type == Integer.class)       return toInt(raw);
        if (type == long.class || type == Long.class)         return toLong(raw);
        if (type == double.class || type == Double.class)     return toDouble(raw);
        if (type == float.class || type == Float.class)       return toFloat(raw);
        if (type == boolean.class || type == Boolean.class)   return raw;
        if (type.isArray() && raw instanceof List<?> list)    return convertToArray(list, type.getComponentType());
        if (List.class.isAssignableFrom(type) && raw instanceof List<?> list)
            return new ArrayList<>(list);
        if (raw instanceof Map<?, ?> map && !Map.class.isAssignableFrom(type))
            return fromMap((Map<String, Object>) map, type);
        return raw;
    }

    private Object convertToArray(List<?> list, Class<?> componentType) {
        Object arr = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++)
            Array.set(arr, i, convert(list.get(i), componentType));
        return arr;
    }

    private <T> T createInstance(Class<T> clazz) throws Exception {
        try {
            Constructor<T> c = clazz.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (NoSuchMethodException e) {
            throw new JsonException("Class " + clazz.getName() + " requires a no-arg constructor");
        }
    }

    private List<Field> collectFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private Integer toInt(Object n) {
        if (n instanceof Integer i) return i;
        if (n instanceof Long l)    return l.intValue();
        if (n instanceof Double d)  return d.intValue();
        if (n instanceof String s)  return Integer.parseInt(s);
        throw new JsonException("Cannot convert to int: " + n);
    }

    private Long toLong(Object n) {
        if (n instanceof Long l)    return l;
        if (n instanceof Integer i) return i.longValue();
        if (n instanceof Double d)  return d.longValue();
        if (n instanceof String s)  return Long.parseLong(s);
        throw new JsonException("Cannot convert to long: " + n);
    }

    private Double toDouble(Object n) {
        if (n instanceof Double d)  return d;
        if (n instanceof Integer i) return i.doubleValue();
        if (n instanceof Long l)    return l.doubleValue();
        if (n instanceof String s)  return Double.parseDouble(s);
        throw new JsonException("Cannot convert to double: " + n);
    }

    private Float toFloat(Object n) {
        return toDouble(n).floatValue();
    }
}
