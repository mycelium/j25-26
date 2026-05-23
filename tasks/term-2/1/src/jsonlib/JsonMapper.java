package jsonlib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

class JsonMapper {

    @SuppressWarnings("unchecked")
    public static <T> T map(Object data, Class<T> clazz) {
        if (data == null) {
            return null;
        }

        if (clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || clazz == Boolean.class || clazz == Character.class) {
            return convertPrimitive(data, clazz);
        }
        
        if (clazz == String.class) {
            return (T) data.toString();
        }

        if (clazz.isArray()) {
            if (!(data instanceof List)) {
                throw new IllegalArgumentException("Expected a JSON array for array type " + clazz.getName());
            }
            return convertArray((List<?>) data, clazz.getComponentType());
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            if (!(data instanceof List)) {
                throw new IllegalArgumentException("Expected a JSON array for Collection type " + clazz.getName());
            }
            
            Collection<Object> collection;
            if (clazz.isInterface() || clazz == List.class) {
                collection = new ArrayList<>();
            } else if (clazz == Set.class || clazz == HashSet.class) {
                collection = new HashSet<>();
            } else {
                try {
                    collection = (Collection<Object>) clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Cannot instantiate collection", e);
                }
            }
            List<?> sourceList = (List<?>) data;
            for (Object item : sourceList) {
                collection.add(item); 
            }
            return (T) collection;
        }

        if (Map.class.isAssignableFrom(clazz)) {
            if (!(data instanceof Map)) {
                throw new IllegalArgumentException("Expected a JSON object for Map type");
            }
            return (T) new LinkedHashMap<>((Map<?, ?>) data);
        }

        if (data instanceof Map) {
            return convertToPojo((Map<String, Object>) data, clazz);
        }

        throw new IllegalArgumentException("Cannot map " + data.getClass() + " to " + clazz);
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertPrimitive(Object data, Class<T> clazz) {
        if (data == null) return null;
        
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(((Number) data).intValue());
        }
        if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(((Number) data).longValue());
        }
        if (clazz == double.class || clazz == Double.class) {
            return (T) Double.valueOf(((Number) data).doubleValue());
        }
        if (clazz == float.class || clazz == Float.class) {
            return (T) Float.valueOf(((Number) data).floatValue());
        }
        if (clazz == boolean.class || clazz == Boolean.class) {
            return (T) data;
        }
        if (clazz == byte.class || clazz == Byte.class) {
            return (T) Byte.valueOf(((Number) data).byteValue());
        }
        if (clazz == short.class || clazz == Short.class) {
            return (T) Short.valueOf(((Number) data).shortValue());
        }
        
        return (T) data;
    }

    private static <T> T convertArray(List<?> list, Class<?> componentType) {
        Object array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, map(list.get(i), componentType));
        }
        return (T) array;
    }

    private static <T> T convertToPojo(Map<String, Object> jsonMap, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                
                String fieldName = field.getName();
                if (jsonMap.containsKey(fieldName)) {
                    Object jsonValue = jsonMap.get(fieldName);
                    Object javaValue = map(jsonValue, field.getType());
                    
                    field.setAccessible(true);
                    field.set(instance, javaValue);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map JSON to class " + clazz.getName(), e);
        }
    }
}
