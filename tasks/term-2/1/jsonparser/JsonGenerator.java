package jsonparser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class JsonGenerator {

    String generate(Object obj) {
        StringBuilder sb = new StringBuilder();
        appendValue(sb, obj);
        return sb.toString();
    }

    private void appendValue(StringBuilder sb, Object value) {
        if (value == null)                    { sb.append("null"); return; }
        if (value instanceof String s)        { appendString(sb, s); return; }
        if (value instanceof Boolean)         { sb.append(value); return; }
        if (value instanceof Number)          { sb.append(value); return; }
        if (value instanceof Enum<?>)         { appendString(sb, ((Enum<?>) value).name()); return; }
        if (value instanceof boolean[] arr)   { appendBooleanArray(sb, arr); return; }
        if (value instanceof int[] arr)       { appendIntArray(sb, arr); return; }
        if (value instanceof long[] arr)      { appendLongArray(sb, arr); return; }
        if (value instanceof double[] arr)    { appendDoubleArray(sb, arr); return; }
        if (value instanceof float[] arr)     { appendFloatArray(sb, arr); return; }
        if (value instanceof char[] arr)      { appendString(sb, new String(arr)); return; }
        if (value instanceof Object[] arr)    { appendObjectArray(sb, arr); return; }
        if (value instanceof Collection<?> c) { appendCollection(sb, c); return; }
        if (value instanceof Map<?, ?> m)     { appendMap(sb, m); return; }
        appendObject(sb, value);
    }

    private void appendString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else          sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    private void appendObjectArray(StringBuilder sb, Object[] arr) {
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            appendValue(sb, arr[i]);
        }
        sb.append(']');
    }

    private void appendIntArray(StringBuilder sb, int[] arr) {
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        sb.append(']');
    }

    private void appendLongArray(StringBuilder sb, long[] arr) {
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        sb.append(']');
    }

    private void appendDoubleArray(StringBuilder sb, double[] arr) {
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        sb.append(']');
    }

    private void appendFloatArray(StringBuilder sb, float[] arr) {
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        sb.append(']');
    }

    private void appendBooleanArray(StringBuilder sb, boolean[] arr) {
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        sb.append(']');
    }

    private void appendCollection(StringBuilder sb, Collection<?> col) {
        sb.append('[');
        boolean first = true;
        for (Object item : col) {
            if (!first) sb.append(',');
            first = false;
            appendValue(sb, item);
        }
        sb.append(']');
    }

    private void appendMap(StringBuilder sb, Map<?, ?> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            appendString(sb, String.valueOf(entry.getKey()));
            sb.append(':');
            appendValue(sb, entry.getValue());
        }
        sb.append('}');
    }

    private void appendObject(StringBuilder sb, Object obj) {
        sb.append('{');
        boolean first = true;
        for (Field field : collectFields(obj.getClass())) {
            field.setAccessible(true);
            try {
                if (!first) sb.append(',');
                first = false;
                appendString(sb, field.getName());
                sb.append(':');
                appendValue(sb, field.get(obj));
            } catch (IllegalAccessException e) {
                throw new JsonException("Cannot access field: " + field.getName(), e);
            }
        }
        sb.append('}');
    }

    private List<Field> collectFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod))
                    fields.add(f);
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
