import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class ToJsonParser {

    private static final ConcurrentHashMap<Class<?>, List<Field>> fieldsCache = new ConcurrentHashMap<>();

    private static List<Field> cachedFields(Class<?> cls) {
        return fieldsCache.computeIfAbsent(cls, c -> {
            List<Field> result = new ArrayList<>();
            for (Class<?> cur = c; cur != null && cur != Object.class; cur = cur.getSuperclass()) {
                for (Field fld : cur.getDeclaredFields()) {
                    int md = fld.getModifiers();
                    if (Modifier.isStatic(md) || Modifier.isTransient(md)) continue;
                    fld.setAccessible(true);
                    result.add(fld);
                }
            }
            return Collections.unmodifiableList(result);
        });
    }

    private ToJsonParser() {}

    static String parseToJson(Object obj) {
        return parseToJson(obj, JsonConfig.defaultConfig());
    }

    static String parseToJson(Object obj, JsonConfig config) {
        try {
            return doSerialize(obj, config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String doSerialize(Object obj, JsonConfig config) throws Exception {
        if (obj == null)                                     return "null";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Character ch)                     return escapeString(ch.toString());
        if (obj instanceof String str)                       return escapeString(str);

        if (obj instanceof Collection cl) {
            return "[" + cl.stream()
                            .map(el -> {
                                try { return doSerialize(el, config); }
                                catch (Exception e) { throw new RuntimeException(e); }
                            })
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("") + "]";
        }

        if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < len; i++) {
                if (i > 0) sb.append(", ");
                sb.append(doSerialize(Array.get(obj, i), config));
            }
            return sb.append("]").toString();
        }

        if (obj instanceof Map mp) {
            Set<Map.Entry> entries = mp.entrySet();
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (var entr : entries) {
                if (!(entr.getKey() instanceof String))
                    throw new RuntimeException("Unsupported key type for Json " + entr.getKey().getClass());
                Object val = entr.getValue();
                if (val == null && !config.serializeNulls) continue;
                if (!first) sb.append(", ");
                sb.append(escapeString((String) entr.getKey()));
                sb.append(" : ");
                sb.append(doSerialize(val, config));
                first = false;
            }
            sb.append('}');
            return sb.toString();
        }

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Field fld : cachedFields(obj.getClass())) {
            Object val = fld.get(obj);
            if (val == null && !config.serializeNulls) continue;
            if (!first) sb.append(", ");
            sb.append('"').append(fld.getName()).append('"');
            sb.append(" : ");
            sb.append(doSerialize(val, config));
            first = false;
        }
        sb.append('}');
        return sb.toString();
    }

    static String escapeString(String str) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default   -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else          sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
