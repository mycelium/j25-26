package json.serializer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class JsonSerializer {

    public String serialize(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof String s) {
            return "\"" + escape(s) + "\"";
        }

        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        if (obj instanceof Map<?,?> map) {
            StringJoiner sj = new StringJoiner(",", "{", "}");
            for (var entry : map.entrySet()) {
                sj.add("\"" + escape(entry.getKey().toString()) + "\":" + serialize(entry.getValue()));
            }
            return sj.toString();
        }

        if (obj.getClass().isArray()) {
            StringJoiner sj = new StringJoiner(",", "[", "]");
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                sj.add(serialize(Array.get(obj, i)));
            }
            return sj.toString();
        }

        if (obj instanceof Iterable<?> iter) {
            StringJoiner sj = new StringJoiner(",", "[", "]");
            for (Object element : iter) {
                sj.add(serialize(element));
            }
            return sj.toString();
        }

        try {
            StringJoiner sj = new StringJoiner(",", "{", "}");
            List<Field> fields = collectFields(obj.getClass());
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                field.setAccessible(true);
                Object value = field.get(obj);
                sj.add("\"" + escape(field.getName()) + "\":" + serialize(value));
            }
            return sj.toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to serialize object: " + obj.getClass().getName(), e);
        }
    }

    private String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> sb.append(c);
            }
        }
        return sb.toString();
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
