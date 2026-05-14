package jsonlab;

import java.lang.reflect.*;
import java.util.*;

final class JsonSerializer {
    private JsonSerializer() {}

    static String write(Object obj) {
        if (obj == null) return "null";
        StringBuilder sb = new StringBuilder();
        appendValue(sb, obj);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void appendValue(StringBuilder out, Object val) {
        if (val == null) {
            out.append("null");
        } else if (val instanceof String s) {
            out.append('"').append(escape(s)).append('"');
        } else if (val instanceof Number || val instanceof Boolean) {
            out.append(val);
        } else if (val instanceof Character c) {
            out.append('"').append(escape(c.toString())).append('"');
        } else if (val instanceof Enum<?> e) {
            out.append('"').append(e.name()).append('"');
        } else if (val instanceof Map<?, ?> m) {
            appendObject(out, (Map<String, Object>) m);
        } else if (val instanceof Collection<?> c) {
            appendArray(out, c);
        } else if (val.getClass().isArray()) {
            appendArray(out, Arrays.asList((Object[]) val));
        } else {
            appendObject(out, toMap(val));
        }
    }

    private static void appendObject(StringBuilder out, Map<String, Object> map) {
        out.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) out.append(',');
            first = false;
            out.append('"').append(escape(e.getKey())).append("\":");
            appendValue(out, e.getValue());
        }
        out.append('}');
    }

    private static void appendArray(StringBuilder out, Collection<?> c) {
        out.append('[');
        boolean first = true;
        for (Object v : c) {
            if (!first) out.append(',');
            first = false;
            appendValue(out, v);
        }
        out.append(']');
    }

    private static Map<String, Object> toMap(Object obj) {
        Map<String, Object> res = new LinkedHashMap<>();
        for (Class<?> c = obj.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (isSkippable(f)) continue;
                try {
                    f.setAccessible(true);
                    res.put(f.getName(), f.get(obj));
                } catch (IllegalAccessException e) {
                    throw new JsonException("Cannot read field '" + f.getName() + "' from " + obj.getClass(), e);
                }
            }
        }
        return res;
    }

    private static boolean isSkippable(Field f) {
        int m = f.getModifiers();
        return Modifier.isStatic(m) || Modifier.isTransient(m);
    }

    private static String escape(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '/'  -> sb.append("\\/");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}