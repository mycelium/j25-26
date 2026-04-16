package org.example.json;

import java.lang.reflect.*;
import java.util.*;

class ObjectMapper {
    
    @SuppressWarnings("unchecked")
    static <T> T convert(Object data, Class<T> targetType) {
        return (T) convertValue(data, targetType);
    }
    
    private static Object convertValue(Object data, Type targetType) {
        if (data == null) return null;
        
        if (targetType instanceof Class) {
            Class<?> clazz = (Class<?>) targetType;
            
            if (clazz == String.class) return data.toString();
            if (clazz == Integer.class || clazz == int.class) {
                return ((Number) data).intValue();
            }
            if (clazz == Long.class || clazz == long.class) {
                return ((Number) data).longValue();
            }
            if (clazz == Double.class || clazz == double.class) {
                return ((Number) data).doubleValue();
            }
            if (clazz == Float.class || clazz == float.class) {
                return ((Number) data).floatValue();
            }
            if (clazz == Boolean.class || clazz == boolean.class) {
                return data;
            }
            if (clazz == Byte.class || clazz == byte.class) {
                return ((Number) data).byteValue();
            }
            if (clazz == Short.class || clazz == short.class) {
                return ((Number) data).shortValue();
            }
            
            if (clazz.isArray()) {
                return buildArray(data, clazz.getComponentType());
            }
            
            if (Collection.class.isAssignableFrom(clazz)) {
                return buildCollection(data, clazz, null);
            }
            
            if (Map.class.isAssignableFrom(clazz)) {
                return buildMap(data, clazz, null, null);
            }
            
            return buildObject(data, clazz);
        }
        
        if (targetType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) targetType;
            Class<?> rawType = (Class<?>) paramType.getRawType();
            Type[] arguments = paramType.getActualTypeArguments();
            
            if (Collection.class.isAssignableFrom(rawType)) {
                Type elemType = arguments.length > 0 ? arguments[0] : Object.class;
                return buildCollection(data, rawType, elemType);
            }
            
            if (Map.class.isAssignableFrom(rawType)) {
                Type keyType = arguments.length > 0 ? arguments[0] : Object.class;
                Type valType = arguments.length > 1 ? arguments[1] : Object.class;
                return buildMap(data, rawType, keyType, valType);
            }
        }
        
        return data;
    }
    
    private static Object buildArray(Object source, Class<?> componentType) {
        List<?> list = (List<?>) source;
        Object array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, convertValue(list.get(i), componentType));
        }
        return array;
    }
    
    private static Object buildCollection(Object source, Class<?> collectionType, Type elementType) {
        List<?> sourceList = (List<?>) source;
        Collection<Object> target;
        
        if (collectionType.isInterface()) {
            if (collectionType == List.class) {
                target = new ArrayList<>();
            } else if (collectionType == Set.class) {
                target = new LinkedHashSet<>();
            } else {
                target = new ArrayList<>();
            }
        } else {
            try {
                target = (Collection<Object>) collectionType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                target = new ArrayList<>();
            }
        }
        
        for (Object item : sourceList) {
            target.add(elementType != null ? convertValue(item, elementType) : item);
        }
        return target;
    }
    
    private static Object buildMap(Object source, Class<?> mapType, Type keyType, Type valueType) {
        Map<?, ?> sourceMap = (Map<?, ?>) source;
        Map<Object, Object> target;
        
        if (mapType.isInterface()) {
            target = new LinkedHashMap<>();
        } else {
            try {
                target = (Map<Object, Object>) mapType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                target = new LinkedHashMap<>();
            }
        }
        
        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            
            if (keyType != null && keyType != String.class && keyType != Object.class) {
                key = convertValue(key, keyType);
            }
            target.put(key, valueType != null ? convertValue(value, valueType) : value);
        }
        return target;
    }
    
    private static Object buildObject(Object source, Class<?> targetClass) {
        Map<String, Object> fieldMap = (Map<String, Object>) source;
        
        try {
            Object instance = targetClass.getDeclaredConstructor().newInstance();
            
            for (Field field : targetClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                
                field.setAccessible(true);
                Object value = fieldMap.get(field.getName());
                
                if (value != null) {
                    field.set(instance, convertValue(value, field.getGenericType()));
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании объекта класса " + targetClass.getName(), e);
        }
    }
}
