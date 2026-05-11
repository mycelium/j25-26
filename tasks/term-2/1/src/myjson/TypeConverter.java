package myjson;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

final class TypeConverter {

    private TypeConverter() {}

    static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        try {
            var obj = clazz.getDeclaredConstructor().newInstance();
            for (var field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                var fieldName = field.getName();
                if (!map.containsKey(fieldName)) continue;

                var value = map.get(fieldName);
                field.setAccessible(true);
                if (value == null) {
                    field.set(obj, null);
                    continue;
                }
                setFieldValue(field, obj, value);
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private static void setFieldValue(Field field, Object target, Object value) throws IllegalAccessException {
        var type = field.getType();

       
        if (type == int.class || type == Integer.class) {
            field.set(target, ((Number) value).intValue());
            return;
        }
        if (type == long.class || type == Long.class) {
            field.set(target, ((Number) value).longValue());
            return;
        }
        if (type == double.class || type == Double.class) {
            field.set(target, ((Number) value).doubleValue());
            return;
        }
        if (type == float.class || type == Float.class) {
            field.set(target, ((Number) value).floatValue());
            return;
        }
        if (type == boolean.class || type == Boolean.class) {
            field.set(target, value);
            return;
        }
        if (type == byte.class || type == Byte.class) {
            field.set(target, ((Number) value).byteValue());
            return;
        }
        if (type == short.class || type == Short.class) {
            field.set(target, ((Number) value).shortValue());
            return;
        }
        if (type == char.class || type == Character.class) {
            var s = value.toString();
            if (s.length() > 0) field.set(target, s.charAt(0));
            return;
        }
        if (type == String.class) {
            field.set(target, value.toString());
            return;
        }

      
        if (type.isArray()) {
            if (value instanceof List<?> list) {
                var compType = type.getComponentType();
                var array = Array.newInstance(compType, list.size());
                for (var i = 0; i < list.size(); i++) {
                    var elem = list.get(i);
                    if (elem instanceof Map && !Map.class.isAssignableFrom(compType) && compType != Object.class) {
                        elem = mapToObject((Map<String, Object>) elem, compType);
                    }
                    if (compType.isPrimitive()) {
                        setPrimitiveArrayElement(array, i, elem, compType);
                    } else {
                        Array.set(array, i, elem);
                    }
                }
                field.set(target, array);
            } else {
                field.set(target, value);
            }
            return;
        }

       
        if (Collection.class.isAssignableFrom(type)) {
            if (value instanceof List<?> list) {
                var coll = createCollection(type);
                coll.addAll(list);
                field.set(target, coll);
            } else {
                var coll = createCollection(type);
                coll.add(value);
                field.set(target, coll);
            }
            return;
        }

        if (value instanceof Map && !Map.class.isAssignableFrom(type)) {
            var nested = mapToObject((Map<String, Object>) value, type);
            field.set(target, nested);
            return;
        }

       
        if (Map.class.isAssignableFrom(type)) {
            field.set(target, value);
            return;
        }

        field.set(target, value);
    }

    static Collection<Object> createCollection(Class<?> type) {
        if (List.class.isAssignableFrom(type)) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(type)) {
            return new LinkedHashSet<>();
        } else {
            try {
                return (Collection<Object>) type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
    }

    private static void setPrimitiveArrayElement(Object array, int index, Object value, Class<?> compType) {
        switch (compType.getSimpleName()) {
            case "int" -> Array.setInt(array, index, ((Number) value).intValue());
            case "long" -> Array.setLong(array, index, ((Number) value).longValue());
            case "double" -> Array.setDouble(array, index, ((Number) value).doubleValue());
            case "float" -> Array.setFloat(array, index, ((Number) value).floatValue());
            case "boolean" -> Array.setBoolean(array, index, (Boolean) value);
            case "byte" -> Array.setByte(array, index, ((Number) value).byteValue());
            case "char" -> Array.setChar(array, index, (Character) value);
            case "short" -> Array.setShort(array, index, ((Number) value).shortValue());
            default -> throw new IllegalArgumentException("Unsupported primitive type: " + compType);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T listToArray(List<?> list, Class<T> arrayClass) {
        var compType = arrayClass.getComponentType();
        var array = Array.newInstance(compType, list.size());
        for (var i = 0; i < list.size(); i++) {
            var elem = list.get(i);
            if (elem instanceof Map && !Map.class.isAssignableFrom(compType) && compType != Object.class) {
                elem = mapToObject((Map<String, Object>) elem, compType);
            }
            if (compType.isPrimitive()) {
                setPrimitiveArrayElement(array, i, elem, compType);
            } else {
                Array.set(array, i, elem);
            }
        }
        return (T) array;
    }
}