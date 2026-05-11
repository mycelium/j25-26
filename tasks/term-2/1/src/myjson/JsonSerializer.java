package myjson;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

final class JsonSerializer {

    private JsonSerializer() {} 

    static StringBuilder serialize(Object obj) {
        if (obj == null) {
            return new StringBuilder("null");
        }
        if (obj instanceof String) {
            return new StringBuilder("\"").append(escape((String) obj)).append("\"");
        }
        if (obj instanceof Boolean || obj instanceof Number) {
            return new StringBuilder(obj.toString());
        }
        if (obj instanceof Collection) {
            return serializeCollection((Collection<?>) obj);
        }
        if (obj.getClass().isArray()) {
            return serializeArray(obj);
        }
        if (obj instanceof Map) {
            return serializeMap((Map<?, ?>) obj);
        }
        return serializeObject(obj);
    }

    private static StringBuilder serializeCollection(Collection<?> col) {
        var sb = new StringBuilder("[");
        var first = true;
        for (var item : col) {
            if (!first) sb.append(",");
            sb.append(serialize(item));
            first = false;
        }
        sb.append("]");
        return sb;
    }

    private static StringBuilder serializeArray(Object arr) {
        var sb = new StringBuilder("[");
        var len = Array.getLength(arr);
        for (var i = 0; i < len; i++) {
            if (i > 0) sb.append(",");
            sb.append(serialize(Array.get(arr, i)));
        }
        sb.append("]");
        return sb;
    }

    private static StringBuilder serializeMap(Map<?, ?> map) {
        var sb = new StringBuilder("{");
        var first = true;
        for (var entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey().toString()).append("\":");
            sb.append(serialize(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb;
    }

    private static StringBuilder serializeObject(Object obj) {
        var sb = new StringBuilder("{");
        var fields = obj.getClass().getDeclaredFields();
        var first = true;
        for (var f : fields) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            try {
                var value = f.get(obj);
                if (!first) sb.append(",");
                sb.append("\"").append(f.getName()).append("\":");
                sb.append(serialize(value));
                first = false;
            } catch (IllegalAccessException ignored) {}
        }
        sb.append("}");
        return sb;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}