package jsonlib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

class JsonSerializer {
    private static final ThreadLocal<Set<Object>> visitedObjects = ThreadLocal.withInitial(HashSet::new);

    public static String serialize(Object obj) {
        Set<Object> visited = visitedObjects.get();
        try {
            visited.clear();
            return serializeValue(obj, visited);
        } finally {
            visited.clear();
        }
    }

    private static String serializeValue(Object obj, Set<Object> visited) {
        if (obj == null) {
            return "null";
        }

        Class<?> clazz = obj.getClass();

        if (obj instanceof String) {
            return "\"" + escapeString((String) obj) + "\"";
        }
        
        if (obj instanceof Number || obj instanceof Boolean) {
            if (obj instanceof Double d) {
                if (Double.isNaN(d) || Double.isInfinite(d)) return "null";
            }
            if (obj instanceof Float f) {
                if (Float.isNaN(f) || Float.isInfinite(f)) return "null";
            }
            return obj.toString();
        }

        if (clazz.isArray()) {
            return serializeArray(obj, visited);
        }

        if (obj instanceof Collection) {
            return serializeCollection((Collection<?>) obj, visited);
        }

        if (obj instanceof Map) {
            return serializeMap((Map<?, ?>) obj, visited);
        }

        return serializeObject(obj, visited);
    }

    private static String serializeArray(Object array, Set<Object> visited) {
        int length = Array.getLength(array);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(serializeValue(Array.get(array, i), visited));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String serializeCollection(Collection<?> collection, Set<Object> visited) {
        StringBuilder sb = new StringBuilder("[");
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            sb.append(serializeValue(it.next(), visited));
            if (it.hasNext()) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String serializeMap(Map<?, ?> map, Set<Object> visited) {
        StringBuilder sb = new StringBuilder("{");
        Iterator<?> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();
            String keyStr = entry.getKey() == null ? "null" : "\"" + escapeString(entry.getKey().toString()) + "\"";
            sb.append(keyStr).append(": ").append(serializeValue(entry.getValue(), visited));
            if (it.hasNext()) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String serializeObject(Object obj, Set<Object> visited) {
        Class<?> clazz = obj.getClass();

        if (visited.contains(obj)) {
            return "null"; 
        }
        visited.add(obj);

        StringBuilder sb = new StringBuilder("{");
        Field[] fields = clazz.getDeclaredFields();
        boolean first = true;

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) || 
                Modifier.isTransient(field.getModifiers()) ||
                field.isSynthetic()) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                
                if (!first) {
                    sb.append(", ");
                }
                sb.append("\"").append(escapeString(field.getName())).append("\": ");
                sb.append(serializeValue(value, visited));
                first = false;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }
        
        visited.remove(obj); 
        sb.append("}");
        return sb.toString();
    }

    private static String escapeString(String str) {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
