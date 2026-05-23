package jsonparser;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// Маппер: преобразует разобранный граф объектов в экземпляр конкретного класса
class ObjectMapper {

    @SuppressWarnings("unchecked")
    <T> T map(Object parsed, Class<T> clazz) {
        if (parsed == null) return null;

        if (clazz == Object.class)                            return (T) parsed;
        if (clazz == String.class)                            return clazz.cast(String.valueOf(parsed));
        if (clazz == int.class || clazz == Integer.class)    return (T) toInt(parsed);
        if (clazz == long.class || clazz == Long.class)      return (T) toLong(parsed);
        if (clazz == double.class || clazz == Double.class)  return (T) toDouble(parsed);
        if (clazz == float.class || clazz == Float.class)    return (T) toFloat(parsed);
        if (clazz == boolean.class || clazz == Boolean.class) {
            if (parsed instanceof Boolean) return (T) parsed;
            throw new JsonException("Ожидался boolean, получен: " + parsed.getClass().getSimpleName());
        }

        if (clazz.isArray() && parsed instanceof List<?> list) {
            return (T) convertToArray(list, clazz.getComponentType());
        }

        if (parsed instanceof Map<?, ?> rawMap) {
            return fromMap((Map<String, Object>) rawMap, clazz);
        }

        throw new JsonException("Не удалось преобразовать " + parsed.getClass().getSimpleName() + " в " + clazz.getName());
    }

    private <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        try {
            T instance = newInstance(clazz);
            for (Field field : getAllFields(clazz)) {
                if (!map.containsKey(field.getName())) continue;
                field.setAccessible(true);
                Object rawValue = map.get(field.getName());
                field.set(instance, convertField(rawValue, field));
            }
            return instance;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException("Ошибка маппинга в класс " + clazz.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Object convertField(Object raw, Field field) {
        Class<?> type = field.getType();
        if (raw == null)                                        return null;
        if (type == String.class)                               return String.valueOf(raw);
        if (type == int.class || type == Integer.class)         return toInt(raw);
        if (type == long.class || type == Long.class)           return toLong(raw);
        if (type == double.class || type == Double.class)       return toDouble(raw);
        if (type == float.class || type == Float.class)         return toFloat(raw);
        if (type == boolean.class || type == Boolean.class)     return raw;
        if (type.isArray() && raw instanceof List<?> list)      return convertToArray(list, type.getComponentType());
        if (List.class.isAssignableFrom(type) && raw instanceof List<?> list)
            return new ArrayList<>(list);
        if (raw instanceof Map<?, ?> map && !Map.class.isAssignableFrom(type))
            return fromMap((Map<String, Object>) map, type);
        return raw;
    }

    private Object convertToArray(List<?> list, Class<?> componentType) {
        Object arr = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(arr, i, map(list.get(i), componentType));
        }
        return arr;
    }

    private <T> T newInstance(Class<T> clazz) throws Exception {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new JsonException("Класс " + clazz.getName() + " должен иметь конструктор без параметров");
        }
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private Integer toInt(Object v) {
        if (v instanceof Integer i) return i;
        if (v instanceof Long l)    return l.intValue();
        if (v instanceof Double d)  return d.intValue();
        if (v instanceof String s)  return Integer.parseInt(s);
        throw new JsonException("Не удалось преобразовать в int: " + v);
    }

    private Long toLong(Object v) {
        if (v instanceof Long l)    return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Double d)  return d.longValue();
        if (v instanceof String s)  return Long.parseLong(s);
        throw new JsonException("Не удалось преобразовать в long: " + v);
    }

    private Double toDouble(Object v) {
        if (v instanceof Double d)  return d;
        if (v instanceof Integer i) return i.doubleValue();
        if (v instanceof Long l)    return l.doubleValue();
        if (v instanceof String s)  return Double.parseDouble(s);
        throw new JsonException("Не удалось преобразовать в double: " + v);
    }

    private Float toFloat(Object v) {
        return toDouble(v).floatValue();
    }
}
