package json.deserializer;

import json.parser.JsonArray;
import json.parser.JsonNode;
import json.parser.JsonPrimitive;
import json.parser.JsonObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class ReflectionDeserializer {

    @SuppressWarnings("unchecked")
    public <T> T toClass(JsonNode node, Class<T> clazz) {
        if (node == null) return null;

        if (node instanceof JsonPrimitive p) {
            return (T) p.getValue();
        }

        if (node instanceof JsonObject jsonObject) {
            try {
                T instance = clazz.getDeclaredConstructor().newInstance();

                List<Field> fields = collectFields(clazz);
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    if (Modifier.isTransient(field.getModifiers())) continue;

                    String fieldName = field.getName();
                    if (!jsonObject.getFields().containsKey(fieldName)) continue;

                    JsonNode valueNode = jsonObject.getFields().get(fieldName);
                    Object fieldValue = resolveFieldValue(valueNode, field);

                    field.setAccessible(true);
                    field.set(instance, fieldValue);
                }
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Error mapping JSON to class: " + clazz.getName(), e);
            }
        }

        throw new RuntimeException("Cannot deserialize node to " + clazz.getName());
    }

    private Object resolveFieldValue(JsonNode valueNode, Field field) {
        Class<?> fieldType = field.getType();

        if (fieldType.isArray() && valueNode instanceof JsonArray arr) {
            Class<?> componentType = fieldType.getComponentType();
            List<JsonNode> elements = arr.getElements();
            Object array = Array.newInstance(componentType, elements.size());
            for (int i = 0; i < elements.size(); i++) {
                Object elem = toClass(elements.get(i), componentType);
                elem = convertNumber(elem, componentType);
                Array.set(array, i, elem);
            }
            return array;
        }

        if (List.class.isAssignableFrom(fieldType) && valueNode instanceof JsonArray arr) {
            Class<?> elementType = Object.class;
            if (field.getGenericType() instanceof ParameterizedType pt) {
                if (pt.getActualTypeArguments()[0] instanceof Class<?> c) {
                    elementType = c;
                }
            }
            List<Object> list = new ArrayList<>();
            for (JsonNode elem : arr.getElements()) {
                Object val = toClass(elem, elementType);
                val = convertNumber(val, elementType);
                list.add(val);
            }
            return list;
        }

        Object val = toClass(valueNode, fieldType);
        return convertNumber(val, fieldType);
    }

    private Object convertNumber(Object value, Class<?> targetType) {
        if (!(value instanceof Number num)) return value;

        if (targetType == int.class || targetType == Integer.class) return num.intValue();
        if (targetType == long.class || targetType == Long.class) return num.longValue();
        if (targetType == float.class || targetType == Float.class) return num.floatValue();
        if (targetType == double.class || targetType == Double.class) return num.doubleValue();
        if (targetType == byte.class || targetType == Byte.class) return num.byteValue();
        if (targetType == short.class || targetType == Short.class) return num.shortValue();

        return value;
    }

    private List<Field> collectFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                result.add(f);
            }
            current = current.getSuperclass();
        }
        return result;
    }
}
