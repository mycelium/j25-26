package papkaJSON;
import java.util.*;
import java.lang.reflect.*;


public class ConverterJSON {
    private ConverterJSON(){}
    ///////////////////////////////////////////////////////////////////////////////////////
    public static String toJSON(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Number) {
            return obj.toString();
        }
        if (obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj.getClass().isArray()) {
            return arrayToJSON(obj);
        }
        if (obj instanceof Collection) {
            return collectionToJSON((Collection<?>) obj);
        }
        if (obj instanceof Map) {
            return mapToJSON((Map) obj);
        }
        return objectToJSON(obj);
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    private static String arrayToJSON(Object array) {
        int length = Array.getLength(array);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(",");
            Object element = Array.get(array, i);
            sb.append(toJSON(element));
        }
        sb.append("]");
        return sb.toString();
    }
    private static String collectionToJSON(Collection<?> collection) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int i = 0;
        for (Object item : collection) {
            if (i++ > 0) sb.append(",");
            sb.append(toJSON(item));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String mapToJSON(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(toJSON(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static String objectToJSON(Object obj) {
        Map<String, Object> map = new LinkedHashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                map.put(field.getName(), value);
            } catch (IllegalAccessException e) {
            }
        }

        return mapToJSON(map);
    }
    
    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t").replace("\b", "\\b")
                .replace("\f", "\\f");
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////

    public static Map<String, Object> toMap(String str_JSON) {
        Object obj = new ParserJSON().parse(str_JSON);
        if(obj instanceof Map<?,?>) {
            return (Map<String, Object>) obj;
        }
        else {
            throw new RuntimeException("Error");
        }
    }

    public static <T> T toClass(String str_JSON, Class<T> clas) {
        Object obj = new ParserJSON().parse(str_JSON);
        return convertToClass(obj, clas);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private static <T> T convertToClass(Object obj, Class<T> clas) {

        if (obj == null) {
            return null;
        }
        if (clas.isInstance(obj)) {
            return (T) (obj);
        }
        if (clas == String.class) {
            return (T) obj.toString();
        }
        if (clas == Boolean.class || clas == boolean.class) {
            return (T) obj;
        }
        if (clas.isPrimitive() || Number.class.isAssignableFrom(clas)) {
            Number num = (Number) obj;
            if (clas == int.class || clas == Integer.class) {
                return (T) (Integer) num.intValue();
            }
            if (clas == long.class || clas == Long.class) {
                return (T) (Long) num.longValue();
            }
            if (clas == double.class || clas == Double.class) {
                return (T) (Double) num.doubleValue();
            }
            if (clas == float.class || clas == Float.class) {
                return (T) (Float) num.floatValue();
            }
            if (clas == short.class || clas == Short.class) {
                return (T) (Short) num.shortValue();
            }
            if (clas == byte.class || clas == Byte.class) {
                return (T) (Byte) num.byteValue();
            }
            throw new RuntimeException("Error");
        }

        if (clas.isArray()) {
            List<?> list = (List<?>) obj;
            Class<?> type = clas.getComponentType();
            Object array = Array.newInstance(type, list.size());
            for (int i = 0; i < list.size(); i++) {
                Object converted = convertToClass(list.get(i), type);
                Array.set(array, i, converted);
            }
            return clas.cast(array);
        }

        if (clas == List.class || clas == ArrayList.class) {
            if (obj instanceof List) {
                List<?> list = (List<?>) obj;
                List<Object> result = new ArrayList<>();
                for (Object item : list) {
                    result.add(convertToClass(item, Object.class));
                }
                return clas.cast(result);
            }
        }

        if (clas == Set.class || clas == HashSet.class) {
            if (obj instanceof List) {
                List<?> list = (List<?>) obj;
                Set<Object> result = new HashSet<>();
                for (Object item : list) {
                    result.add(convertToClass(item, Object.class));
                }
                return clas.cast(result);
            }
        }

        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            try {
                T instance = clas.getDeclaredConstructor().newInstance();

                for (Field field : clas.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = map.get(field.getName());
                    if (value != null) {
                        Object converted = convertToClass(value, field.getType());
                        field.set(instance, converted);
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Error");
            }
        }
        throw new RuntimeException("Error");
    }
}
