package jsonengine;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

class ValueSerializer {
    static String stringify(Object entity) {
        if (entity == null) return "null";
        if (entity instanceof String) return wrapString((String) entity);
        if (entity instanceof Number || entity instanceof Boolean) return entity.toString();
        if (entity instanceof Map) return serializeMap((Map<?, ?>) entity);
        if (entity instanceof Collection) return serializeIterable((Collection<?>) entity);
        if (entity.getClass().isArray()) return serializeArray(entity);
        
        return serializePojo(entity);
    }

    private static String wrapString(String val) {
        StringBuilder out = new StringBuilder("\"");
        for (char ch : val.toCharArray()) {
            switch (ch) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (ch < 32) out.append(String.format("\\u%04x", (int) ch));
                    else out.append(ch);
                }
            }
        }
        return out.append("\"").toString();
    }

    private static String serializeMap(Map<?, ?> data) {
        StringJoiner sj = new StringJoiner(",", "{", "}");
        data.forEach((k, v) -> sj.add(wrapString(String.valueOf(k)) + ":" + stringify(v)));
        return sj.toString();
    }

    private static String serializeIterable(Collection<?> items) {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        for (Object item : items) sj.add(stringify(item));
        return sj.toString();
    }

    private static String serializeArray(Object array) {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        int size = Array.getLength(array);
        for (int i = 0; i < size; i++) {
            sj.add(stringify(Array.get(array, i)));
        }
        return sj.toString();
    }

    private static String serializePojo(Object bean) {
        StringJoiner sj = new StringJoiner(",", "{", "}");
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            try {
                sj.add(wrapString(f.getName()) + ":" + stringify(f.get(bean)));
            } catch (IllegalAccessException ignored) {}
        }
        return sj.toString();
    }
}