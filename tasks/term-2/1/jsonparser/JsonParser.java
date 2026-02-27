package jsonparser;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

class JsonParser {
    private final List<JsonToken> tokens;
    private int position = 0;
    
    public JsonParser(List<JsonToken> tokens) {
        this.tokens = tokens;
    }
    
    public Object parse() {
        if (tokens.isEmpty()) {
            throw new JsonException("Empty JSON");
        }
        return parseValue();
    }
    
    private Object parseValue() {
        if (position >= tokens.size()) {
            throw new JsonException("Unexpected end of input");
        }
        
        JsonToken token = tokens.get(position);
        
        switch (token.getType()) {
            case LEFT_BRACE:
                return parseObject();
            case LEFT_BRACKET:
                return parseArray();
            case STRING:
                position++;
                return token.getValue();
            case NUMBER:
                position++;
                return parseNumber(token.getValue());
            case BOOLEAN:
                position++;
                return Boolean.parseBoolean(token.getValue());
            case NULL:
                position++;
                return null;
            default:
                throw new JsonException("Unexpected token: " + token);
        }
    }
    
    private Number parseNumber(String value) {
        if (value.contains(".") || value.contains("e") || value.contains("E")) {
            return Double.parseDouble(value);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return Long.parseLong(value);
        }
    }
    
    private Map<String, Object> parseObject() {
        Map<String, Object> result = new LinkedHashMap<>();
        position++; 
        
        if (position < tokens.size() && tokens.get(position).getType() == JsonToken.Type.RIGHT_BRACE) {
            position++;
            return result;
        }
        
        while (position < tokens.size()) {
            if (tokens.get(position).getType() != JsonToken.Type.STRING) {
                throw new JsonException("Expected string key");
            }
            String key = tokens.get(position).getValue();
            position++;
            
            if (tokens.get(position).getType() != JsonToken.Type.COLON) {
                throw new JsonException("Expected colon");
            }
            position++;
            
            Object value = parseValue();
            result.put(key, value);
            
            if (position < tokens.size()) {
                JsonToken token = tokens.get(position);
                if (token.getType() == JsonToken.Type.COMMA) {
                    position++;
                    continue;
                } else if (token.getType() == JsonToken.Type.RIGHT_BRACE) {
                    position++;
                    break;
                } else {
                    throw new JsonException("Expected comma or closing brace");
                }
            }
        }
        
        return result;
    }
    
    private List<Object> parseArray() {
        List<Object> result = new ArrayList<>();
        position++; 
        
        if (position < tokens.size() && tokens.get(position).getType() == JsonToken.Type.RIGHT_BRACKET) {
            position++;
            return result;
        }
        
        while (position < tokens.size()) {
            result.add(parseValue());
            
            if (position < tokens.size()) {
                JsonToken token = tokens.get(position);
                if (token.getType() == JsonToken.Type.COMMA) {
                    position++;
                    continue;
                } else if (token.getType() == JsonToken.Type.RIGHT_BRACKET) {
                    position++;
                    break;
                } else {
                    throw new JsonException("Expected comma or closing bracket");
                }
            }
        }
        
        return result;
    }
}