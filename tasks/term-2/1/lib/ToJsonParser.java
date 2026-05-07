import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ToJsonParser {

    public static void main(String[] args){
        Object obj = new int[]{1,5,68};
        System.out.println(parseToJson(obj));
    }

    private ToJsonParser(){};

    public static String parseToJson(Object obj) {
        try {
            if      (obj == null)                                     return "null";
            else if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
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
            else if (obj instanceof String str) return '\"' + str + '\"';
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
                        fld.setAccessible(true);
                        sb.append("4\"" + fld.getName() + "\"");
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
}

