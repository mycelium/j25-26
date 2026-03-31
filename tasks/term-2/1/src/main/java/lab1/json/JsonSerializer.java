package lab1.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

class JsonSerializer {

    public String toJson(Object object) {
        return serialize(object);
    }

    private String serialize(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (obj instanceof String) {
            return "\"" + obj + "\"";
        }

        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(",");
                sb.append(serialize(Array.get(obj, i)));
            }
            sb.append("]");
            return sb.toString();
        }

        if (obj instanceof Collection<?> collection) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : collection) {
                if (!first) sb.append(",");
                first = false;
                sb.append(serialize(item));
            }
            sb.append("]");
            return sb.toString();
        }

        if (obj instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(entry.getKey()).append("\":");
                sb.append(serialize(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }

        // объект
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (!first) sb.append(",");
                first = false;

                sb.append("\"").append(field.getName()).append("\":");
                sb.append(serialize(field.get(obj)));

            } catch (Exception e) {
                throw new JsonException("Serialization error", e);
            }
        }

        sb.append("}");
        return sb.toString();
    }
}