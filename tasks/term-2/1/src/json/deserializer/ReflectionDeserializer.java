package json.deserializer;

import json.parser.JsonNode;
import json.parser.JsonPrimitive;
import json.parser.JsonObject;
import java.lang.reflect.Field;

public class ReflectionDeserializer {

    public <T> T toClass(JsonNode node, Class<T> clazz) {
        if (node == null) return null;
        try {
            if (node instanceof JsonPrimitive) {
                return (T) new JsonDeserializer().toObject(node);
            }

            if (node instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) node;

                T instance = clazz.getDeclaredConstructor().newInstance();

                for (Field field : clazz.getDeclaredFields()) {
                    String fieldName = field.getName();

                    if (jsonObject.getFields().containsKey(fieldName)) {
                        JsonNode valueNode = jsonObject.getFields().get(fieldName);

                        Object fieldValue = toClass(valueNode, field.getType());

                        // Handle numeric type casting (e.g., from Double to int)
                        if (fieldValue instanceof Double) {
                            if (field.getType() == int.class || field.getType() == Integer.class) {
                                fieldValue = ((Double) fieldValue).intValue();
                            } else if (field.getType() == long.class || field.getType() == Long.class) {
                                fieldValue = ((Double) fieldValue).longValue();
                            } else if (field.getType() == float.class || field.getType() == Float.class) {
                                fieldValue = ((Double) fieldValue).floatValue();
                            } else if (field.getType() == double.class || field.getType() == Double.class) {
                                fieldValue = ((Double) fieldValue).doubleValue();
                            } else if (field.getType() == byte.class || field.getType() == Byte.class) {
                                fieldValue = ((Double) fieldValue).byteValue();
                            } else if (field.getType() == short.class || field.getType() == Short.class) {
                                fieldValue = ((Double) fieldValue).shortValue();
                            }
                        }

                        field.setAccessible(true);
                        field.set(instance, fieldValue);
                    }
                }
                return instance;
            }

            throw new UnsupportedOperationException("Cannot deserialize node to " + clazz.getName());

        } catch (Exception e) {
            throw new RuntimeException("Error mapping JSON to class: " + clazz.getName(), e);
        }
    }
}
