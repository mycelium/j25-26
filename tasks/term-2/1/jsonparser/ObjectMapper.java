package jsonparser;

import java.lang.reflect.*;
import java.util.*;

class ObjectMapper {
    
    public <T> T toObject(Map<String, Object> map, Class<T> targetClass) {
        if (map == null) {
            return null;
        }
        
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();
            
            Field[] fields = targetClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                
                if (map.containsKey(fieldName)) {
                    Object value = map.get(fieldName);
                    if (value != null) {
                        value = convertValue(value, field.getType());
                    }
                    field.set(instance, value);
                }
            }
            
            return instance;
        } catch (Exception e) {
            throw new JsonException("Failed to convert to object: " + targetClass.getName(), e);
        }
    }
    
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        
        if (targetType.isPrimitive() || isWrapperType(targetType)) {
            return convertPrimitive(value, targetType);
        }
        
        if (targetType.isArray()) {
            return convertToArray(value, targetType.getComponentType());
        }
        
        if (Collection.class.isAssignableFrom(targetType)) {
            return convertToCollection(value, targetType);
        }
        
        if (Map.class.isAssignableFrom(targetType)) {
            return convertToMap(value, targetType);
        }
        
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return toObject(map, targetType);
        }
        
        return value;
    }
    
    private Object convertPrimitive(Object value, Class<?> targetType) {
        if (targetType == String.class) {
            return String.valueOf(value);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return value instanceof Boolean ? value : Boolean.parseBoolean(String.valueOf(value));
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return value instanceof Number ? ((Number) value).byteValue() : Byte.parseByte(String.valueOf(value));
        }
        if (targetType == short.class || targetType == Short.class) {
            return value instanceof Number ? ((Number) value).shortValue() : Short.parseShort(String.valueOf(value));
        }
        if (targetType == int.class || targetType == Integer.class) {
            return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(String.valueOf(value));
        }
        if (targetType == long.class || targetType == Long.class) {
            return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(String.valueOf(value));
        }
        if (targetType == float.class || targetType == Float.class) {
            return value instanceof Number ? ((Number) value).floatValue() : Float.parseFloat(String.valueOf(value));
        }
        if (targetType == double.class || targetType == Double.class) {
            return value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(String.valueOf(value));
        }
        return value;
    }
    
    private Object convertToArray(Object value, Class<?> componentType) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            Object array = Array.newInstance(componentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, convertValue(list.get(i), componentType));
            }
            return array;
        }
        return value;
    }
    
    @SuppressWarnings("unchecked")
    private Object convertToCollection(Object value, Class<?> collectionType) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            try {
                Collection<Object> collection;
                if (collectionType.isInterface()) {
                    if (List.class.isAssignableFrom(collectionType)) {
                        collection = new ArrayList<>();
                    } else if (Set.class.isAssignableFrom(collectionType)) {
                        collection = new LinkedHashSet<>();
                    } else {
                        collection = new ArrayList<>();
                    }
                } else {
                    collection = (Collection<Object>) collectionType.getDeclaredConstructor().newInstance();
                }
                
                collection.addAll(list);
                return collection;
            } catch (Exception e) {
                throw new JsonException("Failed to create collection", e);
            }
        }
        return value;
    }
    
    @SuppressWarnings("unchecked")
    private Object convertToMap(Object value, Class<?> mapType) {
        if (value instanceof Map) {
            try {
                Map<Object, Object> map;
                if (mapType.isInterface()) {
                    map = new LinkedHashMap<>();
                } else {
                    map = (Map<Object, Object>) mapType.getDeclaredConstructor().newInstance();
                }
                map.putAll((Map<?, ?>) value);
                return map;
            } catch (Exception e) {
                throw new JsonException("Failed to create map", e);
            }
        }
        return value;
    }
    
    private boolean isWrapperType(Class<?> type) {
        return type == Boolean.class || type == Byte.class || type == Short.class ||
               type == Integer.class || type == Long.class || type == Float.class ||
               type == Double.class || type == Character.class || type == String.class;
    }
}