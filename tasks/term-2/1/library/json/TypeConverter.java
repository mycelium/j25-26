package json;

import json.exceptions.JsonMappingException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

final class TypeConverter {
    private final JsonConfig config;

    public TypeConverter(JsonConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    public <T> T convert(Object value, Type targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        @SuppressWarnings("unchecked")
        T converted = (T) convertInternal(value, targetType);
        return converted;
    }

    private Object convertInternal(Object value, Type targetType) {
        if (targetType instanceof Class<?> targetClass) {
            return convertToClass(value, targetClass, targetType);
        }

        if (targetType instanceof ParameterizedType parameterizedType) {
            return convertToParameterized(value, parameterizedType);
        }

        if (targetType instanceof GenericArrayType genericArrayType) {
            return convertToGenericArray(value, genericArrayType);
        }

        if (targetType instanceof WildcardType wildcardType) {
            Type[] upperBounds = wildcardType.getUpperBounds();
            Type bound = upperBounds.length == 0 ? Object.class : upperBounds[0];
            return convertInternal(value, bound);
        }

        if (targetType instanceof TypeVariable<?> typeVariable) {
            Type[] bounds = typeVariable.getBounds();
            Type bound = bounds.length == 0 ? Object.class : bounds[0];
            return convertInternal(value, bound);
        }

        throw new JsonMappingException("Unsupported target type: " + targetType.getTypeName());
    }

    private Object convertToClass(Object value, Class<?> targetClass, Type targetType) {
        if (targetClass == Object.class) {
            return value;
        }

        Class<?> effectiveTargetClass = wrapPrimitive(targetClass);

        if (value == null) {
            if (targetClass.isPrimitive()) {
                throw mappingError("Cannot assign null to primitive " + targetClass.getName(), targetType);
            }
            return null;
        }

        if (effectiveTargetClass.isInstance(value)) {
            return value;
        }

        if (effectiveTargetClass == String.class) {
            return convertToString(value, targetType);
        }
        if (effectiveTargetClass == Character.class) {
            return convertToCharacter(value, targetType);
        }
        if (effectiveTargetClass == Boolean.class) {
            return convertToBoolean(value, targetType);
        }
        if (Number.class.isAssignableFrom(effectiveTargetClass)) {
            return convertToNumber(value, effectiveTargetClass, targetType);
        }
        if (effectiveTargetClass.isEnum()) {
            return convertToEnum(value, effectiveTargetClass, targetType);
        }
        if (effectiveTargetClass.isArray()) {
            return convertToArray(value, effectiveTargetClass.getComponentType());
        }
        if (Collection.class.isAssignableFrom(effectiveTargetClass)) {
            return convertToCollection(value, effectiveTargetClass, Object.class);
        }
        if (Map.class.isAssignableFrom(effectiveTargetClass)) {
            return convertToMap(value, effectiveTargetClass, String.class, Object.class);
        }
        if (value instanceof Map<?, ?> mapValue) {
            return convertToPojo(mapValue, effectiveTargetClass);
        }

        throw mappingError("Cannot convert value to " + targetClass.getName(), targetType);
    }

    private Object convertToParameterized(Object value, ParameterizedType targetType) {
        Type rawType = targetType.getRawType();
        if (!(rawType instanceof Class<?> rawClass)) {
            throw new JsonMappingException("Unsupported parameterized raw type: " + rawType.getTypeName());
        }

        if (Collection.class.isAssignableFrom(rawClass)) {
            Type[] typeArguments = targetType.getActualTypeArguments();
            Type elementType = typeArguments.length == 0 ? Object.class : typeArguments[0];
            return convertToCollection(value, rawClass, elementType);
        }

        if (Map.class.isAssignableFrom(rawClass)) {
            Type[] typeArguments = targetType.getActualTypeArguments();
            Type keyType = typeArguments.length > 0 ? typeArguments[0] : String.class;
            Type valueType = typeArguments.length > 1 ? typeArguments[1] : Object.class;
            return convertToMap(value, rawClass, keyType, valueType);
        }

        return convertToClass(value, rawClass, targetType);
    }

    private Object convertToGenericArray(Object value, GenericArrayType targetType) {
        Type componentType = targetType.getGenericComponentType();
        List<?> sourceValues = asList(value, targetType);
        Class<?> componentClass = resolveRawClass(componentType);
        Object array = Array.newInstance(componentClass, sourceValues.size());
        for (int i = 0; i < sourceValues.size(); i++) {
            Object convertedItem = convertInternal(sourceValues.get(i), componentType);
            Array.set(array, i, convertedItem);
        }
        return array;
    }

    private Object convertToArray(Object value, Type componentType) {
        List<?> sourceValues = asList(value, componentType);
        Class<?> componentClass = resolveArrayComponentClass(componentType);
        Object array = Array.newInstance(componentClass, sourceValues.size());
        for (int i = 0; i < sourceValues.size(); i++) {
            Object convertedItem = convertInternal(sourceValues.get(i), componentType);
            Array.set(array, i, convertedItem);
        }
        return array;
    }

    private Collection<?> convertToCollection(Object value, Class<?> collectionClass, Type elementType) {
        List<?> sourceValues = asList(value, elementType);
        Collection<Object> target = createCollection(collectionClass);
        for (Object sourceValue : sourceValues) {
            target.add(convertInternal(sourceValue, elementType));
        }
        return target;
    }

    private Map<?, ?> convertToMap(Object value, Class<?> mapClass, Type keyType, Type valueType) {
        if (!(value instanceof Map<?, ?> sourceMap)) {
            throw mappingError("Expected JSON object for map conversion", mapClass);
        }

        Map<Object, Object> target = createMap(mapClass);
        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            Object key = convertInternal(entry.getKey(), keyType);
            Object convertedValue = convertInternal(entry.getValue(), valueType);
            target.put(key, convertedValue);
        }
        return target;
    }

    private Object convertToPojo(Map<?, ?> sourceMap, Class<?> targetClass) {
        Object instance = ReflectionUtils.newInstance(targetClass);
        Map<String, Field> fields = ReflectionUtils.getMappableFields(targetClass);

        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw mappingError("JSON object keys must be strings", targetClass);
            }

            Field field = fields.get(key);
            if (field == null) {
                if (config.isFailOnUnknownProperties()) {
                    throw mappingError("Unknown property '" + key + "' for class " + targetClass.getName(), targetClass);
                }
                continue;
            }

            Object convertedValue = convertInternal(entry.getValue(), field.getGenericType());
            try {
                field.set(instance, convertedValue);
            } catch (IllegalAccessException ex) {
                throw new JsonMappingException("Cannot set field '" + field.getName() + "' in " + targetClass.getName(), ex);
            }
        }

        return instance;
    }

    private String convertToString(Object value, Type targetType) {
        if (value instanceof String stringValue) {
            return stringValue;
        }
        if (value instanceof Character || value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            return String.valueOf(value);
        }
        throw mappingError("Cannot convert value to String", targetType);
    }

    private Character convertToCharacter(Object value, Type targetType) {
        if (value instanceof Character characterValue) {
            return characterValue;
        }
        if (value instanceof String stringValue && stringValue.length() == 1) {
            return stringValue.charAt(0);
        }
        throw mappingError("Cannot convert value to Character", targetType);
    }

    private Boolean convertToBoolean(Object value, Type targetType) {
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        if (value instanceof String stringValue) {
            if ("true".equalsIgnoreCase(stringValue)) {
                return true;
            }
            if ("false".equalsIgnoreCase(stringValue)) {
                return false;
            }
        }
        throw mappingError("Cannot convert value to Boolean", targetType);
    }

    private Object convertToEnum(Object value, Class<?> enumClass, Type targetType) {
        if (!(value instanceof String enumName)) {
            throw mappingError("Enum value must be a string", targetType);
        }
        try {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object enumValue = Enum.valueOf((Class<? extends Enum>) enumClass, enumName);
            return enumValue;
        } catch (IllegalArgumentException ex) {
            throw mappingError("Unknown enum value '" + enumName + "' for " + enumClass.getName(), targetType);
        }
    }

    private Object convertToNumber(Object value, Class<?> targetClass, Type targetType) {
        BigDecimal decimalValue;
        if (value instanceof Number numberValue) {
            decimalValue = toBigDecimal(numberValue);
        } else if (value instanceof String stringValue) {
            try {
                decimalValue = new BigDecimal(stringValue);
            } catch (NumberFormatException ex) {
                throw mappingError("Cannot parse number from string '" + stringValue + "'", targetType);
            }
        } else {
            throw mappingError("Cannot convert value to number", targetType);
        }

        try {
            if (targetClass == Byte.class) {
                return decimalValue.byteValueExact();
            }
            if (targetClass == Short.class) {
                return decimalValue.shortValueExact();
            }
            if (targetClass == Integer.class) {
                return decimalValue.intValueExact();
            }
            if (targetClass == Long.class) {
                return decimalValue.longValueExact();
            }
            if (targetClass == Float.class) {
                return decimalValue.floatValue();
            }
            if (targetClass == Double.class) {
                return decimalValue.doubleValue();
            }
            if (targetClass == BigInteger.class) {
                return decimalValue.toBigIntegerExact();
            }
            if (targetClass == BigDecimal.class) {
                return decimalValue;
            }
        } catch (ArithmeticException ex) {
            throw mappingError("Numeric value out of range for " + targetClass.getSimpleName(), targetType);
        }

        throw mappingError("Unsupported numeric target type: " + targetClass.getName(), targetType);
    }

    private BigDecimal toBigDecimal(Number value) {
        if (value instanceof BigDecimal decimalValue) {
            return decimalValue;
        }
        if (value instanceof BigInteger integerValue) {
            return new BigDecimal(integerValue);
        }
        return new BigDecimal(value.toString());
    }

    private List<?> asList(Object value, Type targetType) {
        if (value instanceof List<?> listValue) {
            return listValue;
        }
        if (value instanceof Collection<?> collectionValue) {
            return new ArrayList<>(collectionValue);
        }
        if (value != null && value.getClass().isArray()) {
            int size = Array.getLength(value);
            List<Object> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(Array.get(value, i));
            }
            return list;
        }
        throw mappingError("Expected JSON array", targetType);
    }

    private Collection<Object> createCollection(Class<?> collectionClass) {
        if (collectionClass.isInterface()) {
            if (Set.class.isAssignableFrom(collectionClass)) {
                return new LinkedHashSet<>();
            }
            if (Deque.class.isAssignableFrom(collectionClass)) {
                return new ArrayDeque<>();
            }
            if (Queue.class.isAssignableFrom(collectionClass)) {
                return new ArrayDeque<>();
            }
            return new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        Collection<Object> instance = (Collection<Object>) ReflectionUtils.newInstance(collectionClass);
        return instance;
    }

    private Map<Object, Object> createMap(Class<?> mapClass) {
        if (mapClass.isInterface()) {
            if (SortedMap.class.isAssignableFrom(mapClass) || NavigableMap.class.isAssignableFrom(mapClass)) {
                return new TreeMap<>();
            }
            return new LinkedHashMap<>();
        }

        @SuppressWarnings("unchecked")
        Map<Object, Object> instance = (Map<Object, Object>) ReflectionUtils.newInstance(mapClass);
        return instance;
    }

    private Class<?> resolveRawClass(Type type) {
        if (type instanceof Class<?> targetClass) {
            return wrapPrimitive(targetClass);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> rawClass) {
                return rawClass;
            }
        }
        if (type instanceof GenericArrayType genericArrayType) {
            Class<?> componentType = resolveRawClass(genericArrayType.getGenericComponentType());
            return Array.newInstance(componentType, 0).getClass();
        }
        if (type instanceof WildcardType wildcardType) {
            Type[] upperBounds = wildcardType.getUpperBounds();
            return upperBounds.length == 0 ? Object.class : resolveRawClass(upperBounds[0]);
        }
        if (type instanceof TypeVariable<?> typeVariable) {
            Type[] bounds = typeVariable.getBounds();
            return bounds.length == 0 ? Object.class : resolveRawClass(bounds[0]);
        }
        return Object.class;
    }

    private Class<?> resolveArrayComponentClass(Type componentType) {
        if (componentType instanceof Class<?> componentClass) {
            return componentClass;
        }
        return resolveRawClass(componentType);
    }

    private Class<?> wrapPrimitive(Class<?> targetClass) {
        if (!targetClass.isPrimitive()) {
            return targetClass;
        }
        if (targetClass == byte.class) {
            return Byte.class;
        }
        if (targetClass == short.class) {
            return Short.class;
        }
        if (targetClass == int.class) {
            return Integer.class;
        }
        if (targetClass == long.class) {
            return Long.class;
        }
        if (targetClass == float.class) {
            return Float.class;
        }
        if (targetClass == double.class) {
            return Double.class;
        }
        if (targetClass == boolean.class) {
            return Boolean.class;
        }
        if (targetClass == char.class) {
            return Character.class;
        }
        return targetClass;
    }

    private JsonMappingException mappingError(String message, Type targetType) {
        return new JsonMappingException(message + " (target: " + targetType.getTypeName() + ")");
    }
}
