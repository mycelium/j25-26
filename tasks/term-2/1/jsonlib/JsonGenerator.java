package jsonlib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

class JsonGenerator {
    static String generate(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return quote((String) obj);
        if (obj instanceof Number) return obj.toString();
        if (obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map) return mapToJson((Map<?, ?>) obj);
        if (obj instanceof Collection) return collectionToJson((Collection<?>) obj);
        if (obj.getClass().isArray()) return arrayToJson(obj);
        return objectToJson(obj);
    }

    private static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append(quote(entry.getKey().toString())).append(":");
            sb.append(generate(entry.getValue()));
            sb.append(",");
        }
        if (sb.length() > 1) sb.setLength(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    private static String collectionToJson(Collection<?> coll) {
        StringBuilder sb = new StringBuilder("[");
        for (Object item : coll) {
            sb.append(generate(item));
            sb.append(",");
        }
        if (sb.length() > 1) sb.setLength(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    private static String arrayToJson(Object arr) {
        int len = Array.getLength(arr);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < len; i++) {
            sb.append(generate(Array.get(arr, i)));
            if (i < len - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String objectToJson(Object obj) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (first) first = false; else sb.append(",");
                sb.append(quote(field.getName())).append(":");
                sb.append(generate(value));
            } catch (IllegalAccessException e) {
                
            }
        }
        sb.append("}");
        return sb.toString();
    }
}