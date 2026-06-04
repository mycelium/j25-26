package jsonlib.converter;

import jsonlib.JsonParseException;
import jsonlib.node.*;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

public class JsonToClassMapper {

    @SuppressWarnings("unchecked")
    public <T> T map(JsonNode node, Class<T> clazz) {
        if (node.isNull()) return null;
        if (!node.isObject()) throw new JsonParseException("Cannot map non-object to class " + clazz.getName());
        return (T) mapObject((JsonObject) node, clazz);
    }

    private Object mapValue(JsonNode node, Type targetType) {
        if (node.isNull()) return null;
        if (targetType instanceof Class<?>) {
            Class<?> cls = (Class<?>) targetType;
            if (cls.isPrimitive()) return convertPrimitive(node, cls);
            if (cls == String.class) {
                if (!node.isString()) throw new JsonParseException("Expected string");
                return ((JsonString) node).getValue();
            }
            if (Number.class.isAssignableFrom(cls)) {
                if (!node.isNumber()) throw new JsonParseException("Expected number");
                return convertNumber(((JsonNumber) node).getValue(), cls);
            }
            if (cls == Boolean.class || cls == boolean.class) {
                if (!node.isBoolean()) throw new JsonParseException("Expected boolean");
                return ((JsonBoolean) node).getValue();
            }
            if (cls.isArray()) {
                if (!node.isArray()) throw new JsonParseException("Expected array");
                return mapArray((JsonArray) node, cls);
            }
            if (List.class.isAssignableFrom(cls) || Set.class.isAssignableFrom(cls)) {
                if (!node.isArray()) throw new JsonParseException("Expected array for collection");
                return mapCollection((JsonArray) node, cls, targetType);
            }
            if (Map.class.isAssignableFrom(cls)) {
                if (!node.isObject()) throw new JsonParseException("Expected object for Map");
                return mapMap((JsonObject) node, cls, targetType);
            }
            // пользовательский класс
            if (!node.isObject()) throw new JsonParseException("Expected object for class " + cls.getName());
            return mapObject((JsonObject) node, cls);
        } else if (targetType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) targetType;
            Class<?> rawType = (Class<?>) pt.getRawType();
            Type[] typeArgs = pt.getActualTypeArguments();
            if (List.class.isAssignableFrom(rawType) || Set.class.isAssignableFrom(rawType)) {
                if (!node.isArray()) throw new JsonParseException("Expected array");
                Type elementType = typeArgs[0];
                return mapParameterizedCollection((JsonArray) node, rawType, elementType);
            }
            if (Map.class.isAssignableFrom(rawType)) {
                if (!node.isObject()) throw new JsonParseException("Expected object");
                Type valueType = typeArgs[1];
                return mapParameterizedMap((JsonObject) node, rawType, valueType);
            }
            throw new JsonParseException("Unsupported parameterized type: " + targetType);
        }
        throw new JsonParseException("Unsupported type: " + targetType);
    }

    private Object mapObject(JsonObject jsonObject, Class<?> clazz) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            Object instance = ctor.newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) continue;
                JsonNode valueNode = jsonObject.getMembers().get(field.getName());
                if (valueNode == null) continue;
                field.setAccessible(true);
                Type fieldType = field.getGenericType();
                Object val = mapValue(valueNode, fieldType);
                field.set(instance, val);
            }
            return instance;
        } catch (Exception e) {
            throw new JsonParseException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    private Object mapArray(JsonArray jsonArray, Class<?> arrayClass) {
        Class<?> componentType = arrayClass.getComponentType();
        int len = jsonArray.size();
        Object arr = Array.newInstance(componentType, len);
        for (int i = 0; i < len; i++) {
            Object elem = mapValue(jsonArray.get(i), componentType);
            Array.set(arr, i, elem);
        }
        return arr;
    }

    private Object mapCollection(JsonArray jsonArray, Class<?> collClass, Type targetType) {
        Collection<Object> coll;
        if (collClass == List.class || collClass == ArrayList.class) coll = new ArrayList<>();
        else if (collClass == Set.class || collClass == HashSet.class) coll = new HashSet<>();
        else if (collClass == LinkedList.class) coll = new LinkedList<>();
        else if (collClass == TreeSet.class) coll = new TreeSet<>();
        else {
            try { coll = (Collection<Object>) collClass.getDeclaredConstructor().newInstance(); }
            catch (Exception e) { throw new JsonParseException("Cannot create collection of type " + collClass.getName(), e); }
        }
        Type elementType = Object.class;
        if (targetType instanceof ParameterizedType) {
            elementType = ((ParameterizedType) targetType).getActualTypeArguments()[0];
        }
        for (JsonNode node : jsonArray.getElements()) {
            coll.add(mapValue(node, elementType));
        }
        return coll;
    }

    private Object mapParameterizedCollection(JsonArray jsonArray, Class<?> rawType, Type elementType) {
        Collection<Object> coll;
        if (rawType == List.class || rawType == ArrayList.class) coll = new ArrayList<>();
        else if (rawType == Set.class || rawType == HashSet.class) coll = new HashSet<>();
        else if (rawType == LinkedList.class) coll = new LinkedList<>();
        else if (rawType == TreeSet.class) coll = new TreeSet<>();
        else {
            try { coll = (Collection<Object>) rawType.getDeclaredConstructor().newInstance(); }
            catch (Exception e) { throw new JsonParseException("Cannot create collection of type " + rawType.getName(), e); }
        }
        for (JsonNode node : jsonArray.getElements()) {
            coll.add(mapValue(node, elementType));
        }
        return coll;
    }

    private Object mapMap(JsonObject jsonObject, Class<?> mapClass, Type targetType) {
        Map<String, Object> map;
        if (mapClass == Map.class || mapClass == LinkedHashMap.class) map = new LinkedHashMap<>();
        else if (mapClass == HashMap.class) map = new HashMap<>();
        else if (mapClass == TreeMap.class) map = new TreeMap<>();
        else {
            try { map = (Map<String, Object>) mapClass.getDeclaredConstructor().newInstance(); }
            catch (Exception e) { throw new JsonParseException("Cannot create Map of type " + mapClass.getName(), e); }
        }
        Type valueType = Object.class;
        if (targetType instanceof ParameterizedType) {
            valueType = ((ParameterizedType) targetType).getActualTypeArguments()[1];
        }
        for (Map.Entry<String, JsonNode> e : jsonObject.getMembers().entrySet()) {
            map.put(e.getKey(), mapValue(e.getValue(), valueType));
        }
        return map;
    }

    private Object mapParameterizedMap(JsonObject jsonObject, Class<?> rawType, Type valueType) {
        Map<String, Object> map;
        if (rawType == Map.class || rawType == LinkedHashMap.class) map = new LinkedHashMap<>();
        else if (rawType == HashMap.class) map = new HashMap<>();
        else if (rawType == TreeMap.class) map = new TreeMap<>();
        else {
            try { map = (Map<String, Object>) rawType.getDeclaredConstructor().newInstance(); }
            catch (Exception e) { throw new JsonParseException("Cannot create Map of type " + rawType.getName(), e); }
        }
        for (Map.Entry<String, JsonNode> e : jsonObject.getMembers().entrySet()) {
            map.put(e.getKey(), mapValue(e.getValue(), valueType));
        }
        return map;
    }

    private Object convertPrimitive(JsonNode node, Class<?> target) {
        if (target == int.class) return ((JsonNumber) node).intValue();
        if (target == long.class) return ((JsonNumber) node).longValue();
        if (target == double.class) return ((JsonNumber) node).doubleValue();
        if (target == float.class) return ((JsonNumber) node).getValue().floatValue();
        if (target == short.class) return ((JsonNumber) node).getValue().shortValue();
        if (target == byte.class) return ((JsonNumber) node).getValue().byteValue();
        if (target == boolean.class) return ((JsonBoolean) node).getValue();
        throw new JsonParseException("Unsupported primitive: " + target);
    }

    private Object convertNumber(Number number, Class<?> target) {
        if (target == Integer.class || target == int.class) return number.intValue();
        if (target == Long.class || target == long.class) return number.longValue();
        if (target == Double.class || target == double.class) return number.doubleValue();
        if (target == Float.class || target == float.class) return number.floatValue();
        if (target == Short.class || target == short.class) return number.shortValue();
        if (target == Byte.class || target == byte.class) return number.byteValue();
        if (target == BigDecimal.class) return new BigDecimal(number.toString());
        if (target == Number.class) return number;
        try {
            return target.getDeclaredConstructor(String.class).newInstance(number.toString());
        } catch (Exception e) {
            throw new JsonParseException("Cannot convert number to " + target.getName());
        }
    }
}