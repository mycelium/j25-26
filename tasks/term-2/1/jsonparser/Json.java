package jsonparser;

import java.util.List;
import java.util.Map;

/**
 * Main JSON parser library class
 */
public class Json {
    
    /**
     * Parse JSON string to Java Object (Map or List structure)
     */
    public static Object parse(String json) throws JsonException{
        if (json == null || json.trim().isEmpty()) {
            throw new JsonException("Empty JSON string");
        }
        
        JsonTokenizer tokenizer = new JsonTokenizer(json);
        List<JsonToken> tokens = tokenizer.tokenize();
        
        JsonParser parser = new JsonParser(tokens);

        return parser.parse();
    }
    
    /**
     * Parse JSON string to Map<String, Object>
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(String json) {
        Object result = parse(json);
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }
        throw new JsonException("JSON is not an object");
    }
    
    /**
     * Parse JSON string to specified class
     */
    public static <T> T parseToObject(String json, Class<T> targetClass) throws JsonException {
        Map<String, Object> map = parseToMap(json);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.toObject(map, targetClass);
    }
    
    /**
     * Convert Java object to JSON string
     */
    public static String stringify(Object obj) {
        JsonGenerator generator = new JsonGenerator();
        return generator.generate(obj);
    }
    
    /**
     * Convert Map to Java object
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> targetClass) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.toObject(map, targetClass);
    }
    
    /**
     * Pretty print JSON with indentation
     */
    public static String prettyPrint(String json) {
        Object parsed = parse(json);
        return prettyPrintObject(parsed, 0);
    }
    
    private static String prettyPrintObject(Object obj, int indent) {
        if (obj == null) return "null";
        
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            return prettyPrintMap(map, indent);
        } else if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) obj;
            return prettyPrintList(list, indent);
        } else if (obj instanceof String) {
            return "\"" + obj + "\"";
        } else {
            return String.valueOf(obj);
        }
    }
    
    private static String prettyPrintMap(Map<String, Object> map, int indent) {
        if (map.isEmpty()) return "{}";
        
        String indentStr = getIndent(indent);
        String childIndent = getIndent(indent + 2);
        
        StringBuilder sb = new StringBuilder("{\n");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",\n");
            sb.append(childIndent).append("\"").append(entry.getKey()).append("\": ");
            sb.append(prettyPrintObject(entry.getValue(), indent + 2));
            first = false;
        }
        sb.append("\n").append(indentStr).append("}");
        return sb.toString();
    }
    
    private static String prettyPrintList(List<Object> list, int indent) {
        if (list.isEmpty()) return "[]";
        
        String indentStr = getIndent(indent);
        String childIndent = getIndent(indent + 2);
        
        StringBuilder sb = new StringBuilder("[\n");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",\n");
            sb.append(childIndent).append(prettyPrintObject(item, indent + 2));
            first = false;
        }
        sb.append("\n").append(indentStr).append("]");
        return sb.toString();
    }
    
    private static String getIndent(int spaces) {
        return " ".repeat(spaces);
    }
}