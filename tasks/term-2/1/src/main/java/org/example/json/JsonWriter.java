package org.example.json;

import java.lang.reflect.*;
import java.util.*;

class JsonWriter {
    
    static String write(Object obj) {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        return convertToString(obj, visited);
    }
    
    private static String convertToString(Object obj, Set<Object> visited) {
        if (obj == null) return "null";
        
        if (visited.contains(obj)) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Character) {
            return "\"" + escape(obj.toString()) + "\"";
        }
        if (obj instanceof Number) {
            if (obj instanceof Double || obj instanceof Float) {
                double val = ((Number) obj).doubleValue();
                if (Double.isNaN(val) || Double.isInfinite(val)) {
                    return "null";
                }
            }
            return obj.toString();
        }
        if (obj instanceof Boolean) {
            return obj.toString();
        }
        
        visited.add(obj);
        String result;
        
        if (obj.getClass().isArray()) {
            result = arrayToJson(obj, visited);
        } else if (obj instanceof Collection) {
            result = collectionToJson((Collection<?>) obj, visited);
        } else if (obj instanceof Map) {
            result = mapToJson((Map<?, ?>) obj, visited);
        } else {
            result = objectToJson(obj, visited);
        }
        
        visited.remove(obj);
        return result;
    }
    
    private static String arrayToJson(Object array, Set<Object> visited) {
        int length = Array.getLength(array);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(",");
            sb.append(convertToString(Array.get(array, i), visited));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String collectionToJson(Collection<?> coll, Set<Object> visited) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : coll) {
            if (!first) sb.append(",");
            sb.append(convertToString(item, visited));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String mapToJson(Map<?, ?> map, Set<Object> visited) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
            sb.append(convertToString(entry.getValue(), visited));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static String objectToJson(Object obj, Set<Object> visited) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;
            
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (!first) sb.append(",");
                sb.append("\"").append(escape(field.getName())).append("\":");
                sb.append(convertToString(value, visited));
                first = false;
            } catch (IllegalAccessException e) {
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
