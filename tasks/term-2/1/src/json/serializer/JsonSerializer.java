package json.serializer;

import java.lang.reflect.Field;
import java.util.Map;

public class JsonSerializer {

    public String serialize(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof String) {
            return "\"" + obj + "\"";
        }

        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":")
                        .append(serialize(entry.getValue()));
                first = false;
            }
            return sb.append("}").toString();
        }

        if (obj instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object element : iterable) {
                if (!first) sb.append(",");
                sb.append(serialize(element));
                first = false;
            }
            return sb.append("]").toString();
        }

        try {
            StringBuilder sb = new StringBuilder("{");
            Field[] fields = obj.getClass().getDeclaredFields();
            boolean first = true;
            for (Field field : fields) {
                if (!first) sb.append(",");
                field.setAccessible(true);
                Object value = field.get(obj);
                sb.append("\"").append(field.getName()).append("\":")
                        .append(serialize(value));
                first = false;
            }
            return sb.append("}").toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to serialize object: " + obj.getClass().getName(), e);
        }
    }
}
