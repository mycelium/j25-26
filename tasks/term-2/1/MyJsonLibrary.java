import java.util.*;
import java.lang.reflect.*;

/*
### 1. JSON parser
- Do not use external libraries
- Read JSON string
1) parse(String json) -> Object
2) parseToMap(String json) -> Map<String, Object>
3) parse(String json, Class<T> clas) -> T
- Convert Java object to JSON string
4) toJson (Object obj) -> String
- Library should support
  - Classes with fields (primitives, boxing types, null, arrays, classes)
  - Arrays
  - Collections
- Limitations (you may skip implementation)
  - Cyclic dependencies
  - non-representable in JSON types
- It should be a library, so all interactions and configurations should be made through public API
 */

public class MyJsonLibrary {

    public static Object parse(String json){ //json to Object
        if (json == null || json.isEmpty()) {
            return null;
        }
        json = json.trim();
        if (json.startsWith("{")) {
            return parseObject(json);
        } else if (json.startsWith("[")) {
            return parseArray(json);
        } else if (json.startsWith("\"")) {
            return parseString(json);
        } else if (json.equals("true") || json.equals("false")) {
            return Boolean.parseBoolean(json); //стандартный метод
        } else if (json.equals("null")) {
            return null;
        } else {
            return Double.parseDouble(json); //стандартный метод
        }
    }

    private static Map<String, Object> parseObject(String json) {
        Map<String, Object> res = new HashMap<>();
        json = json.trim();
        json = delBrackets(json, '{', '}');
        if (json.isEmpty()){
            return res;
        }
        List<String> pairs = splitTopLevel(json, ',');
        for (String pair : pairs) {
            List<String> kv = splitTopLevel(pair, ':');
            String key = parseString(kv.get(0).trim());
            String valuePart = kv.get(1).trim();
            Object value = parse(valuePart);
            res.put(key, value);
        }
        return res;
    }

    private static List<Object> parseArray(String json) {
        List<Object> res = new ArrayList<>();
        json = delBrackets(json, '[', ']');
        if (json.isEmpty()){
            return res;
        }
        List<String> elements = splitTopLevel(json, ',');
        for (String element : elements) {
            res.add(parse(element.trim()));
        }
        return res;
    }

private static String parseString(String json) {
    String str = delBrackets(json, '"', '"');
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
        char c = str.charAt(i);
        if (c == '\\') {
            char next = str.charAt(++i);
            switch (next) {
                case 'b':
                    result.append('\b');
                    break;
                case 'n':
                    result.append('\n');
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 't':
                    result.append('\t');
                    break;
                default:
                    throw new RuntimeException("Invalid escape sequence: \\" + next);
            }
        } else {
            result.append(c);
        }
    }

    return result.toString();
}

    private static String delBrackets(String json, char br, char closeBr){
        json = json.trim();
        if (json.startsWith(String.valueOf(br)) && json.endsWith(String.valueOf(closeBr))) {
            return json.substring(1, json.length() - 1).trim();
        }
        return json;
    }

    private static List<String> splitTopLevel(String text, char delimiter) {
        List<String> parts = new ArrayList<>();
        int objectLevel = 0;
        int arrayLevel = 0;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') objectLevel++;
            if (c == '}') objectLevel--;
            if (c == '[') arrayLevel++;
            if (c == ']') arrayLevel--;
            if (c == delimiter && objectLevel == 0 && arrayLevel == 0) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        parts.add(current.toString());
        return parts;
    }

    public static Map<String, Object> parseToMap(String json){
        Object obj = parse(json);
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        } else {
            throw new RuntimeException("JSON is not an object");
        }
    }

    public static <T> T parse(String json, Class<T> clas){
        try{
            Map<String, Object> map = parseToMap(json);
            T instance = clas.getDeclaredConstructor().newInstance();
            Field[] fields = clas.getDeclaredFields();
            for(Field field : fields){
                field.setAccessible(true);
                String name = field.getName();
                Object value = map.get(name);
                if(value == null) continue;
                Class<?> type = field.getType();
                if(type == int.class && value instanceof Number){
                    field.set(instance, ((Number)value).intValue());
                }
                else if(type == double.class && value instanceof Number){
                    field.set(instance, ((Number)value).doubleValue());
                }
                else if(type == boolean.class && value instanceof Boolean){
                    field.set(instance, value);
                }
                else if(type == String.class){
                    field.set(instance, value);
                }
                else if(List.class.isAssignableFrom(type)){
                    field.set(instance, value);
                }
                else{
                    field.set(instance,value);
                }
            }
            return instance;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

public static String toJson(Object obj) {
    if (obj == null) {
        return "null";
    }
    if (obj instanceof String) {
        return "\"" + obj + "\"";
    }
    if (obj instanceof Boolean) {
        return obj.toString();
    }
    if (obj instanceof Number) {
        Number num = (Number) obj;
        if (num instanceof Double || num instanceof Float) {
            double d = num.doubleValue();
            if (d == (long) d) {
                return String.valueOf((long) d);
            }
        }
        return num.toString();
    }

    if (obj instanceof Map) {
        Map<?, ?> map = (Map<?, ?>) obj;
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\"");
            sb.append(":");
            sb.append(toJson(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    if (obj instanceof Collection) {
        Collection<?> col = (Collection<?>) obj;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Object item : col) {
            if (!first) {
                sb.append(",");
            }
            sb.append(toJson(item));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    if (obj.getClass().isArray()) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int length = java.lang.reflect.Array.getLength(obj);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            Object value = java.lang.reflect.Array.get(obj, i);
            sb.append(toJson(value));
        }
        sb.append("]");
        return sb.toString();
    }

    try {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean first = true;
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(field.getName()).append("\"");
            sb.append(":");
            sb.append(toJson(value));
            first = false;
        }
        sb.append("}");
        return sb.toString();

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
}