package jsonlib.serializer;

import jsonlib.JsonSerializationException;
import jsonlib.node.JsonString;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class JsonSerializer {

    public String serialize(Object obj, boolean pretty) {
        StringBuilder sb = new StringBuilder();
        serializeValue(obj, sb, pretty, 0);
        return sb.toString();
    }

    private void serializeValue(Object obj, StringBuilder sb, boolean pretty, int indent) {
        if (obj == null) {
            sb.append("null");
            return;
        }
        Class<?> cls = obj.getClass();
        if (obj instanceof String) {
            sb.append(escape((String) obj));
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj.toString());
        } else if (obj instanceof Character) {
            sb.append(escape(obj.toString()));
        } else if (cls.isArray()) {
            serializeArray(obj, sb, pretty, indent);
        } else if (obj instanceof Collection) {
            serializeCollection((Collection<?>) obj, sb, pretty, indent);
        } else if (obj instanceof Map) {
            serializeMap((Map<?, ?>) obj, sb, pretty, indent);
        } else {
            serializeObject(obj, sb, pretty, indent);
        }
    }

    private void serializeArray(Object arr, StringBuilder sb, boolean pretty, int indent) {
        sb.append("[");
        int len = Array.getLength(arr);
        if (len > 0) {
            if (pretty) indent++;
            for (int i = 0; i < len; i++) {
                if (i > 0) sb.append(",");
                if (pretty) { sb.append("\n"); indent(sb, indent); }
                serializeValue(Array.get(arr, i), sb, pretty, indent);
            }
            if (pretty) { sb.append("\n"); indent(sb, indent - 1); }
        }
        sb.append("]");
    }

    private void serializeCollection(Collection<?> coll, StringBuilder sb, boolean pretty, int indent) {
        sb.append("[");
        if (!coll.isEmpty()) {
            if (pretty) indent++;
            boolean first = true;
            for (Object item : coll) {
                if (!first) sb.append(",");
                first = false;
                if (pretty) { sb.append("\n"); indent(sb, indent); }
                serializeValue(item, sb, pretty, indent);
            }
            if (pretty) { sb.append("\n"); indent(sb, indent - 1); }
        }
        sb.append("]");
    }

    private void serializeMap(Map<?, ?> map, StringBuilder sb, boolean pretty, int indent) {
        sb.append("{");
        if (!map.isEmpty()) {
            if (pretty) indent++;
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                if (pretty) { sb.append("\n"); indent(sb, indent); }
                if (!(e.getKey() instanceof String)) {
                    throw new JsonSerializationException("Map key must be a String, got " + e.getKey().getClass());
                }
                sb.append(escape((String) e.getKey())).append(":");
                if (pretty) sb.append(" ");
                serializeValue(e.getValue(), sb, pretty, indent);
            }
            if (pretty) { sb.append("\n"); indent(sb, indent - 1); }
        }
        sb.append("}");
    }

    private void serializeObject(Object obj, StringBuilder sb, boolean pretty, int indent) {
        sb.append("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        List<Field> toSerialize = new ArrayList<>();
        for (Field f : fields) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) continue;
            f.setAccessible(true);
            toSerialize.add(f);
        }
        if (!toSerialize.isEmpty()) {
            if (pretty) indent++;
            boolean first = true;
            for (Field f : toSerialize) {
                if (!first) sb.append(",");
                first = false;
                if (pretty) { sb.append("\n"); indent(sb, indent); }
                sb.append(escape(f.getName())).append(":");
                if (pretty) sb.append(" ");
                try {
                    serializeValue(f.get(obj), sb, pretty, indent);
                } catch (IllegalAccessException e) {
                    throw new JsonSerializationException("Cannot access field " + f.getName(), e);
                }
            }
            if (pretty) { sb.append("\n"); indent(sb, indent - 1); }
        }
        sb.append("}");
    }

    private String escape(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private void indent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) sb.append("  ");
    }
}