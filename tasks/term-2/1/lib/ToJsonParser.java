import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ToJsonParser {

    private ToJsonParser(){};

    public static String parseToJson(Object obj) {
        try {
            if      (obj == null)                                     return "null";
            else if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
            else if (obj instanceof Character ch)                     return escapeString(ch.toString());
            else if (obj instanceof Collection cl)
                return "[" + cl.stream()
                                 .map(ToJsonParser::parseToJson)
                                 .reduce((str1, str2) -> str1 + ", " + str2)
                                 .orElse("") + "]";
            else if (obj.getClass().isArray()){
                int len = Array.getLength(obj);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < len; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(parseToJson(Array.get(obj, i)));
                }
                return sb.append("]").toString();
            }
            else if (obj instanceof String str) return escapeString(str);
            else if (obj instanceof Map mp) {
                Set<Map.Entry> entries = mp.entrySet();
                StringBuilder sb = new StringBuilder("{");
                for (var entr : entries) {
                    if (!(entr.getKey() instanceof String))
                        throw new RuntimeException("Unsupported key type for Json " + entr.getKey().getClass());
                    sb.append(parseToJson(entr.getKey()));
                    sb.append(" : ");
                    sb.append(parseToJson(entr.getValue()));
                    sb.append(", ");
                }
                if (sb.length() > 1) sb.delete(sb.length() - 2, sb.length());
                sb.append('}');
                return sb.toString();
            } else {
                StringBuilder sb = new StringBuilder("{");
                for (Class<?> cls = obj.getClass(); cls != null && cls != Object.class; cls = cls.getSuperclass()) {
                    Field[] flds = cls.getDeclaredFields();
                    for (var fld : flds) {
                        int md = fld.getModifiers();
                        if (Modifier.isStatic(md) || Modifier.isTransient(md)) continue;
                        fld.setAccessible(true);
                        sb.append('"').append(fld.getName()).append('"');
                        sb.append(" : ");
                        sb.append(parseToJson(fld.get(obj)));
                        sb.append(", ");
                    }
                }
                if (sb.length() > 1) sb.delete(sb.length() - 2, sb.length());
                sb.append('}');
                return sb.toString();
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private static String escapeString(String str) {
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
