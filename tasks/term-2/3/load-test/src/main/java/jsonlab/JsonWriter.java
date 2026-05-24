package json;

import java.lang.reflect.*;
import java.util.*;

class JsonWriter {
    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
            String.class, Integer.class, Long.class, Double.class,
            Float.class, Boolean.class, Byte.class, Short.class,
            Character.class, int.class, long.class, double.class,
            float.class, boolean.class, byte.class, short.class, char.class
    );

    static String write(Object obj) {
        StringBuilder out = new StringBuilder();
        append(obj, out, new IdentityHashMap<>(8));
        return out.toString();
    }

    private static void append(Object val, StringBuilder sb, IdentityHashMap<Object, Boolean> seen) {
        if (val == null) {
            sb.append("null");
            return;
        }
        if (seen.containsKey(val)) {
            sb.append("null"); // защита от циклов
            return;
        }
        if (!SIMPLE_TYPES.contains(val.getClass())) {
            seen.put(val, true);
        }

        if (val instanceof String s) {
            sb.append('"').append(escape(s)).append('"');
        } else if (val instanceof Number || val instanceof Boolean) {
            sb.append(val);
        } else if (val instanceof Map<?, ?> m) {
            writeMap(m, sb, seen);
        } else if (val instanceof Collection<?> c) {
            writeCollection(c, sb, seen);
        } else if (val.getClass().isArray()) {
            writeArray(val, sb, seen);
        } else {
            writeBean(val, sb, seen);
        }

        if (!SIMPLE_TYPES.contains(val.getClass())) {
            seen.remove(val);
        }
    }

    private static String escape(String s) {
        StringBuilder r = new StringBuilder(s.length() + 10);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> r.append("\\\"");
                case '\\' -> r.append("\\\\");
                case '\b' -> r.append("\\b");
                case '\f' -> r.append("\\f");
                case '\n' -> r.append("\\n");
                case '\r' -> r.append("\\r");
                case '\t' -> r.append("\\t");
                default -> {
                    if (c < 0x20) r.append(String.format("\\u%04x", (int) c));
                    else r.append(c);
                }
            }
        }
        return r.toString();
    }

    private static void writeMap(Map<?, ?> m, StringBuilder sb, IdentityHashMap<Object, Boolean> seen) {
        sb.append('{');
        boolean first = true;
        for (var e : m.entrySet()) {
            if (!first) sb.append(',');
            sb.append('"').append(escape(e.getKey().toString())).append("\":");
            append(e.getValue(), sb, seen);
            first = false;
        }
        sb.append('}');
    }

    private static void writeCollection(Collection<?> c, StringBuilder sb, IdentityHashMap<Object, Boolean> seen) {
        sb.append('[');
        boolean first = true;
        for (var item : c) {
            if (!first) sb.append(',');
            append(item, sb, seen);
            first = false;
        }
        sb.append(']');
    }

    private static void writeArray(Object arr, StringBuilder sb, IdentityHashMap<Object, Boolean> seen) {
        int len = Array.getLength(arr);
        sb.append('[');
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(',');
            append(Array.get(arr, i), sb, seen);
        }
        sb.append(']');
    }

    private static void writeBean(Object bean, StringBuilder sb, IdentityHashMap<Object, Boolean> seen) {
        sb.append('{');
        boolean first = true;
        for (Field f : bean.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) continue;
            f.setAccessible(true);
            try {
                Object v = f.get(bean);
                if (!first) sb.append(',');
                sb.append('"').append(escape(f.getName())).append("\":");
                append(v, sb, seen);
                first = false;
            } catch (IllegalAccessException ignored) {}
        }
        sb.append('}');
    }
}