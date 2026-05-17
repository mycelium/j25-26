package json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

class JsonSerializer {

    private JsonSerializer() {}

    static String serialize(Object obj) {
        var sb = new StringBuilder(64);
        write(obj, sb);
        return sb.toString();
    }

    // main etnry
    private static void write(Object obj, StringBuilder sb) {
        if (obj == null) { sb.append("null"); return; }

        switch (obj) {
            case Boolean b -> sb.append(b);
            case Number n -> writeNumber(n, sb);
            case String s -> writeString(s, sb);
            case Character c -> writeString(c.toString(), sb);
            case Enum<?> e -> writeString(e.name(), sb);
            default -> {
                var clazz = obj.getClass();
                if (clazz.isArray()) writeArray(obj, sb);
                else if (obj instanceof Collection<?> col) writeCollection(col, sb);
                else if (obj instanceof Map<?, ?> map) writeMap(map, sb);
                else writeObject(obj, sb);
            }
        }
    }

    // sup mehtods
    private static void writeNumber(Number n, StringBuilder sb) {
        switch (n) {
            case Double d  -> { if (Double.isNaN(d) || Double.isInfinite(d)) sb.append("null"); else sb.append(d); }
            case Float  f  -> { if (Float.isNaN(f) || Float.isInfinite(f)) sb.append("null"); else sb.append(f); }
            default        -> sb.append(n);
        }
    }

    private static void writeString(String s, StringBuilder sb) {
        sb.append('"');
        for (var c : s.toCharArray()) {
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private static void writeArray(Object arr, StringBuilder sb) {
        sb.append('[');
        int len = Array.getLength(arr);
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(',');
            write(Array.get(arr, i), sb);
        }
        sb.append(']');
    }

    private static void writeCollection(Collection<?> col, StringBuilder sb) {
        sb.append('[');
        var it = col.iterator();
        while (it.hasNext()) {
            write(it.next(), sb);
            if (it.hasNext()) sb.append(',');
        }
        sb.append(']');
    }

    private static void writeMap(Map<?, ?> map, StringBuilder sb) {
        sb.append('{');
        var it = map.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            writeString(String.valueOf(entry.getKey()), sb);
            sb.append(':');
            write(entry.getValue(), sb);
            if (it.hasNext()) sb.append(',');
        }
        sb.append('}');
    }

    private static void writeObject(Object obj, StringBuilder sb) {
        sb.append('{');
        var fields = collectFields(obj.getClass());
        boolean first = true;
        for (var field : fields) {
            int mods = field.getModifiers();
            if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) continue;

            field.setAccessible(true);
            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                throw new JsonException("Cannot access field '" + field.getName() + "'", e);
            }

            if (!first) sb.append(',');
            first = false;
            writeString(field.getName(), sb);
            sb.append(':');
            write(value, sb);
        }
        sb.append('}');
    }

    // accum
    private static List<Field> collectFields(Class<?> clazz) {
        var fields = new ArrayList<Field>();
        for (var c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }
}
