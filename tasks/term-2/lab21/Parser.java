import java.lang.reflect.Field;
import java.util.*;
public class Parser {
    
    private Splitter splitter;
    
    public Parser(Splitter splitter) {
        this.splitter = splitter;
    }

    public Map<String, Object> parseMap() {
        Map<String, Object> map = new HashMap<>();
        splitter.next();
        while (splitter.hasMore()) {
            String key = splitter.next();
            if ("}".equals(key)) {
                break;
            }
            splitter.next();
            Object value = parseValue();
            map.put(key, value);
            if (",".equals(splitter.peek())) {
                splitter.next();
            }
        }
        return map;
    }
    
    public <T> T parseObject(Class<T> clas) {
        try {
            T obj = clas.getDeclaredConstructor().newInstance();
            splitter.next();
            Field[] fields = clas.getDeclaredFields();
            
            while (splitter.hasMore()) {
                String key = splitter.next();
                if ("}".equals(key)) {
                    break;
                }
                splitter.next();
                for (Field field : fields) {
                    if (field.getName().equals(key)) {
                        field.setAccessible(true);
                        Object value = parseValueForType(field.getType());
                        field.set(obj, value);
                        break;
                    }
                }
                if (",".equals(splitter.peek())) {
                    splitter.next();
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
    
    private Object parseValueForType(Class<?> type) {
        String token = splitter.peek();
        if ("null".equals(token)) {
            splitter.next();
            return null;
        }
        if (type == String.class) {
            splitter.next();
            return token;
        }
        if (type == int.class || type == Integer.class) {
            splitter.next();
            return Integer.parseInt(token);
        }
        if (type == long.class || type == Long.class) {
            splitter.next();
            return Long.parseLong(token);
        }
        if (type == double.class || type == Double.class) {
            splitter.next();
            return Double.parseDouble(token);
        }
        if (type == boolean.class || type == Boolean.class) {
            splitter.next();
            return "true".equals(token);
        }
        if ("{".equals(token)) {
            return parseObject(type);
        }
        if ("[".equals(token)) {
            return parseArray(type);
        }
        splitter.next();
        return token;
    }
    
    private Object parseValue() {
        String token = splitter.peek();
        if (token == null) {
            return null;
        }
        if ("null".equals(token)) {
            splitter.next();
            return null;
        }
        if ("{".equals(token)) {
            return parseMap();
        }
        if ("[".equals(token)) {
            return parseArrayGeneric();
        }
        if ("true".equals(token)) {
            splitter.next();
            return true;
        }
        if ("false".equals(token)) {
            splitter.next();
            return false;
        }
        splitter.next();
        if (token.startsWith("\"") && token.endsWith("\"")) {
            return token.substring(1, token.length() - 1);
        }
        try {
            if (token.contains(".")) {
                return Double.parseDouble(token);
            } else {
                return Integer.parseInt(token);
            }
        } catch (NumberFormatException e) {
            return token;
        }
    }
    
    private Object parseArray(Class<?> type) {
        splitter.next();
        List<Object> list = new ArrayList<>();
        while (splitter.hasMore()) {
            String token = splitter.peek();
            if ("]".equals(token)) {
                splitter.next();
                break;
            }
            list.add(parseValue());
            if (",".equals(splitter.peek())) {
                splitter.next();
            }
        }
        return list;
    }
    private List<Object> parseArrayGeneric() {
        splitter.next();
        List<Object> list = new ArrayList<>();
        while (splitter.hasMore()) {
            String token = splitter.peek();
            if ("]".equals(token)) {
                splitter.next();
                break;
            }
            list.add(parseValue());
            if (",".equals(splitter.peek())) {
                splitter.next();
            }
        }
        return list;
    }
}