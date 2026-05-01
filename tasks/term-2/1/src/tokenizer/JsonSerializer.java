package tokenizer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

class JsonSerializer {

    static String toJson(Object value) {
        StringBuilder sb = new StringBuilder();
        IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        writeValue(value, sb, visited);
        return sb.toString();
    }

    private static void writeValue(Object value, StringBuilder sb, IdentityHashMap<Object, Boolean> visited) {
        if (value == null) {
            sb.append("null");
            return;
        }
        if (value instanceof String) {
            String s = (String) value;
            writeString(s, sb);
            return;
        }
        if (value instanceof Character) {
            Character c = (Character) value;
            writeString(String.valueOf(c), sb);
            return;
        }
        if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
            return;
        }
        if (value.getClass().isEnum()) {
            writeString(((Enum<?>) value).name(), sb);
            return;
        }

        if (visited.containsKey(value)) {
            throw new IllegalArgumentException("Cyclic dependency detected");
        }
        visited.put(value, Boolean.TRUE);
        try {
            if (value instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) value;
                writeMap(map, sb, visited);
                return;
            }
            if (value instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) value;
                writeIterable(iterable, sb, visited);
                return;
            }
            if (value.getClass().isArray()) {
                writeArray(value, sb, visited);
                return;
            }
            writePojo(value, sb, visited);
        } finally {
            visited.remove(value);
        }
    }

    private static void writeMap(Map<?, ?> map, StringBuilder sb, IdentityHashMap<Object, Boolean> visited) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()){
            if (!first) {
                sb.append(',');
            }
            first = false;
            writeString(String.valueOf(entry.getKey()), sb);
            sb.append(':');
            writeValue(entry.getValue(), sb, visited);
        }
        sb.append('}');
    }

    private static void writeIterable(Iterable<?> iterable, StringBuilder sb, IdentityHashMap<Object, Boolean> visited) {
        sb.append('[');
        Iterator<?> it = iterable.iterator();
        boolean first = true;
        while (it.hasNext()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            writeValue(it.next(), sb, visited);
        }
        sb.append(']');
    }

    private static void writeArray(Object array, StringBuilder sb, IdentityHashMap<Object, Boolean> visited) {
        sb.append('[');
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            writeValue(Array.get(array, i), sb, visited);
        }
        sb.append(']');
    }

    private static void writePojo(Object object, StringBuilder sb, IdentityHashMap<Object, Boolean> visited) {
        sb.append('{');
        boolean first = true;
        Class<?> current = object.getClass();
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
                    continue;
                }
                field.setAccessible(true);
                Object fieldValue;
                try {
                    fieldValue = field.get(object);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Cannot read field: " + field.getName(), e);
                }
                if (!first) {
                    sb.append(',');
                }
                first = false;
                writeString(field.getName(), sb);
                sb.append(':');
                writeValue(fieldValue, sb, visited);
            }
            current = current.getSuperclass();
        }
        sb.append('}');
    }

    private static void writeString(String value, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        String hex = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int j = hex.length(); j < 4; j++) {
                            sb.append('0');
                        }
                        sb.append(hex);
                    } else {
                        sb.append(ch);
                    }
            }
        }
        sb.append('"');
    }
}
