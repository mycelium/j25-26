package jsontree;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class TreeConverter {
    private JsonNode root;
    public TreeConverter(JsonNode root){
        this.root = root;
    }

    public String convertToJson() {
        return serializeNode(root);
    }

    private String serializeNode(JsonNode node) {
        if (node.isPrimitive()) {
            return serializePrimitive((BaseNode) node);
        }
        if (node.isArray()) {
            return serializeArray((ArrayNode) node);
        }
        if (node.isObject()) {
            return serializeObject((ObjectNode) node);
        }
        return "null";
    }

    private String serializePrimitive(BaseNode node) {
        Object value = node.getValue();
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + escapeString((String) value) + "\"";
        }
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        return "\"" + value.toString() + "\"";
    }

    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String serializeArray(ArrayNode node) {
        StringBuilder sb = new StringBuilder("[");
        List<JsonNode> elements = node.getElements();

        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(serializeNode(elements.get(i)));
        }

        sb.append("]");
        return sb.toString();
    }

    private String serializeObject(ObjectNode node) {
        StringBuilder sb = new StringBuilder("{");
        Map<String, JsonNode> members = node.getMembers();
        boolean first = true;
        for (Map.Entry<String, JsonNode> entry : members.entrySet()) {
            if (!first) sb.append(",");
            first = false;

            sb.append("\"").append(escapeString(entry.getKey())).append("\":");
            sb.append(serializeNode(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    public Map<String, Object> convertToMap() {
        if (!root.isObject()) {
            throw new RuntimeException("Root is not an object, cannot convert to Map");
        }
        return convertNodeToMap(root);
    }

    private Map<String, Object> convertNodeToMap(JsonNode node) {
        if (!node.isObject()) {
            throw new RuntimeException("Node is not an object");
        }
        ObjectNode objNode = (ObjectNode) node;
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, JsonNode> entry : objNode.getMembers().entrySet()) {
            result.put(entry.getKey(), convertNodeToJavaObject(entry.getValue()));
        }

        return result;
    }

    private Object convertNodeToJavaObject(JsonNode node) {
        if (node.isPrimitive()) {
            return ((BaseNode) node).getValue();
        }

        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            List<Object> list = new ArrayList<>();
            for (JsonNode element : arrayNode.getElements()) {
                list.add(convertNodeToJavaObject(element));
            }
            return list;
        }

        if (node.isObject()) {
            return convertNodeToMap(node);
        }

        return null;
    }

    public <T> T convertToObject(Class<T> clazz) {
        if (!root.isObject()) {
            throw new RuntimeException("Root must be an object to convert to POJO");
        }
        return convertNodeToClass(root, clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertNodeToClass(JsonNode node, Class<T> clazz) {
        if (node.isPrimitive()) {
            Object value = ((BaseNode) node).getValue();
            return (T) convertPrimitiveToType(value, clazz);
        }

        if (!node.isObject()) {
            throw new RuntimeException("Expected object node, got: " + node.getClass().getSimpleName());
        }

        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            ObjectNode objNode = (ObjectNode) node;

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                JsonNode fieldValue = objNode.get(fieldName);

                if (fieldValue != null) {
                    Object value = convertNodeToFieldType(fieldValue, field.getType(), field.getGenericType());
                    field.set(instance, value);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to " + clazz.getName(), e);
        }
    }

    private Object convertPrimitiveToType(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == String.class) {
            return value.toString();
        }

        if (value instanceof Number) {
            Number num = (Number) value;
            if (targetType == int.class || targetType == Integer.class) return num.intValue();
            if (targetType == long.class || targetType == Long.class) return num.longValue();
            if (targetType == double.class || targetType == Double.class) return num.doubleValue();
            if (targetType == float.class || targetType == Float.class) return num.floatValue();
            if (targetType == byte.class || targetType == Byte.class) return num.byteValue();
            if (targetType == short.class || targetType == Short.class) return num.shortValue();
        }

        if (value instanceof Boolean && (targetType == boolean.class || targetType == Boolean.class)) {
            return value;
        }

        return value;
    }

    private Object convertNodeToFieldType(JsonNode node, Class<?> targetType, Type genericType) {
        if (node.isPrimitive()) {
            return convertPrimitiveToType(((BaseNode) node).getValue(), targetType);
        }

        if (node.isArray() && (targetType.isArray() || Collection.class.isAssignableFrom(targetType))) {
            ArrayNode arrayNode = (ArrayNode) node;

            if (targetType.isArray()) {
                Class<?> componentType = targetType.getComponentType();
                Object array = java.lang.reflect.Array.newInstance(componentType, arrayNode.size());
                for (int i = 0; i < arrayNode.size(); i++) {
                    Object element = convertNodeToFieldType(arrayNode.get(i), componentType, null);
                    java.lang.reflect.Array.set(array, i, element);
                }
                return array;
            }

            if (Collection.class.isAssignableFrom(targetType)) {
                Collection<Object> collection = createCollection(targetType);
                Class<?> elementType = getGenericType(genericType);

                for (JsonNode element : arrayNode.getElements()) {
                    Object value = convertNodeToFieldType(element, elementType != null ? elementType : Object.class, null);
                    collection.add(value);
                }
                return collection;
            }
        }

        if (node.isObject() && !targetType.isPrimitive() &&
                !isPrimitiveOrWrapper(targetType) && targetType != String.class) {
            return convertNodeToClass(node, targetType);
        }

        if (node.isObject() && Map.class.isAssignableFrom(targetType)) {
            return convertNodeToMap(node);
        }

        throw new RuntimeException("Cannot convert " + node.getClass().getSimpleName() + " to " + targetType);
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> createCollection(Class<?> type) {
        if (type == List.class || type == Collection.class) {
            return new ArrayList<>();
        }
        if (type == Set.class) {
            return new HashSet<>();
        }
        try {
            return (Collection<Object>) type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    private Class<?> getGenericType(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                return (Class<?>) typeArgs[0];
            }
        }
        return null;
    }
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class ||
                clazz == Byte.class ||
                clazz == Character.class ||
                clazz == Short.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Float.class ||
                clazz == Double.class;
    }
}
