package jsonlib;

import java.lang.reflect.*;
import java.util.*;

public class JsonMapper {

  
    public static <T> T toObject(String json, Class<T> clazz) {
        Object parsed = JsonParser.parse(json);

        if (!(parsed instanceof Map)) {
            throw new RuntimeException("JSON must be an object");
        }

        return map((Map<String, Object>) parsed, clazz);
    }

   
    private static <T> T map(Map<String, Object> map, Class<T> clazz) {
        try {
        	Constructor<T> constructor = clazz.getDeclaredConstructor();
        	constructor.setAccessible(true);
        	T obj = constructor.newInstance();
            
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                Object value = map.get(field.getName());
                if (value == null) continue;

                Class<?> type = field.getType();

                if (value instanceof Map && !isPrimitiveOrWrapper(type)) {
                    Object nestedObj = map((Map<String, Object>) value, type);
                    field.set(obj, nestedObj);
                }

               
                else {
                    field.set(obj, convertValue(value, type));
                }
            }

            return obj;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object convertValue(Object value, Class<?> type) {

        if (value == null) return null;

        if (type == int.class || type == Integer.class) {
            return ((Number) value).intValue();
        }
        if (type == long.class || type == Long.class) {
            return ((Number) value).longValue();
        }
        if (type == double.class || type == Double.class) {
            return ((Number) value).doubleValue();
        }
        if (type == float.class || type == Float.class) {
            return ((Number) value).floatValue();
        }
        if (type == boolean.class || type == Boolean.class) {
            return value;
        }

        return value;
    }

    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive()
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Boolean.class
                || type == String.class
                || Number.class.isAssignableFrom(type);
    }

  
    public static String toJson(Object obj) {
        return serialize(obj);
    }

    private static String serialize(Object obj) {

        if (obj == null) return "null";

        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }

        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        if (obj.getClass().isArray()) {
            return arrayToJson(obj);
        }

        if (obj instanceof Collection) {
            return collectionToJson((Collection<?>) obj);
        }

        return objectToJson(obj);
    }

    private static String objectToJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        Field[] fields = obj.getClass().getDeclaredFields();

        boolean first = true;

        for (Field f : fields) {
            f.setAccessible(true);

            try {
                Object value = f.get(obj);

                if (!first) sb.append(",");
                first = false;

                sb.append("\"").append(f.getName()).append("\":");
                sb.append(serialize(value));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        sb.append("}");
        return sb.toString();
    }

    private static String arrayToJson(Object array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        int length = Array.getLength(array);

        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(",");
            sb.append(serialize(Array.get(array, i)));
        }

        sb.append("]");
        return sb.toString();
    }

    private static String collectionToJson(Collection<?> col) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        boolean first = true;
        for (Object o : col) {
            if (!first) sb.append(",");
            first = false;
            sb.append(serialize(o));
        }

        sb.append("]");
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}