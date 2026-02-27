package jsonparser;

import java.util.ArrayList;
import java.util.List;

class JsonTokenizer {
    private final String input;
    private int position = 0;
    
    public JsonTokenizer(String input) {
        this.input = input.trim();
    }
    
    public List<JsonToken> tokenize() {
        List<JsonToken> tokens = new ArrayList<>();
        while (position < input.length()) {
            char current = input.charAt(position);
            
            if (Character.isWhitespace(current)) {
                position++;
                continue;
            }
            
            switch (current) {
                case '{':
                    tokens.add(new JsonToken(JsonToken.Type.LEFT_BRACE, "{"));
                    position++;
                    break;
                case '}':
                    tokens.add(new JsonToken(JsonToken.Type.RIGHT_BRACE, "}"));
                    position++;
                    break;
                case '[':
                    tokens.add(new JsonToken(JsonToken.Type.LEFT_BRACKET, "["));
                    position++;
                    break;
                case ']':
                    tokens.add(new JsonToken(JsonToken.Type.RIGHT_BRACKET, "]"));
                    position++;
                    break;
                case ':':
                    tokens.add(new JsonToken(JsonToken.Type.COLON, ":"));
                    position++;
                    break;
                case ',':
                    tokens.add(new JsonToken(JsonToken.Type.COMMA, ","));
                    position++;
                    break;
                case '"':
                    tokens.add(new JsonToken(JsonToken.Type.STRING, parseString()));
                    break;
                case 't':
                case 'f':
                    tokens.add(new JsonToken(JsonToken.Type.BOOLEAN, parseBoolean()));
                    break;
                case 'n':
                    tokens.add(new JsonToken(JsonToken.Type.NULL, parseNull()));
                    break;
                default:
                    if (current == '-' || Character.isDigit(current)) {
                        tokens.add(new JsonToken(JsonToken.Type.NUMBER, parseNumber()));
                    } else {
                        throw new JsonException("Unexpected character: " + current + " at position " + position);
                    }
                    break;
            }
        }
        return tokens;
    }
    
    private String parseString() {
        position++;
        StringBuilder sb = new StringBuilder();
        
        while (position < input.length()) {
            char c = input.charAt(position);
            if (c == '"') {
                position++;
                return sb.toString();
            }
            if (c == '\\') {
                position++;
                if (position < input.length()) {
                    char next = input.charAt(position);
                    switch (next) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        default: sb.append(next);
                    }
                }
            } else {
                sb.append(c);
            }
            position++;
        }
        throw new JsonException("Unterminated string");
    }
    
    private String parseNumber() {
        int start = position;
        if (input.charAt(position) == '-') {
            position++;
        }
        
        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            position++;
        }
        
        if (position < input.length() && input.charAt(position) == '.') {
            position++;
            while (position < input.length() && Character.isDigit(input.charAt(position))) {
                position++;
            }
        }
        
        if (position < input.length() && (input.charAt(position) == 'e' || input.charAt(position) == 'E')) {
            position++;
            if (position < input.length() && (input.charAt(position) == '+' || input.charAt(position) == '-')) {
                position++;
            }
            while (position < input.length() && Character.isDigit(input.charAt(position))) {
                position++;
            }
        }
        
        return input.substring(start, position);
    }
    
    private String parseBoolean() {
        if (input.startsWith("true", position)) {
            position += 4;
            return "true";
        } else if (input.startsWith("false", position)) {
            position += 5;
            return "false";
        }
        throw new JsonException("Invalid boolean at position " + position);
    }
    
    private String parseNull() {
        if (input.startsWith("null", position)) {
            position += 4;
            return "null";
        }
        throw new JsonException("Invalid null at position " + position);
    }
}
