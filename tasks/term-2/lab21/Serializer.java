import java.lang.reflect.Field;
import java.util.*;


public class Serializer {   
    public static String serialize(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + escapeString((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map) {
            return serializeMap((Map<?, ?>) obj);
        }
        if (obj.getClass().isArray()) {
            return serializeArray(obj);
        }
        return serializeObject(obj);
    }
    
    private static String serializeMap(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(escapeString(entry.getKey().toString())).append("\":");
            sb.append(serialize(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static String serializeArray(Object array) {
        StringBuilder sb = new StringBuilder("[");
        int length = java.lang.reflect.Array.getLength(array);
        boolean first = true;
        for (int i = 0; i < length; i++) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(serialize(java.lang.reflect.Array.get(array, i)));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String serializeObject(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value == null) {
                    continue;
                }
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append("\"").append(escapeString(field.getName())).append("\":");
                sb.append(serialize(value));
            } catch (Exception e) {
                
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static String escapeString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\t", "\\t")
                  .replace("\r", "\\r");
    }
}