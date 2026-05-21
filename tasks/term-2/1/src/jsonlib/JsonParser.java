package jsonlib;



import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



class JsonParser {

    private final String json;
    private int pos;

    private JsonParser(String json) {
        this.json = json;
        this.pos = 0;
    }

    public static Object parse(String json) {
        return new JsonParser(json).parseValue();
    }

    private void skipWhitespace() {
        while (pos < json.length()) {
            char c = json.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                pos++;
            } else {
                break;
            }
        }
    }

    private char currentChar() {
        skipWhitespace();
        if (pos >= json.length()) {
            throw new RuntimeException("Unexpected end of JSON input");
        }
        return json.charAt(pos);
    }

    private Object parseValue() {
        char c = currentChar();
        switch (c) {
            case '{': return parseObject();
            case '[': return parseArray();
            case '"': return parseString();
            case 't': 
            case 'f': return parseBoolean();
            case 'n': return parseNull();
            default:  return parseNumber();
        }
    }

    private Map<String, Object> parseObject() {
        pos++; // skip '{'
        Map<String, Object> map = new LinkedHashMap<>();
        
        if (currentChar() == '}') {
            pos++;
            return map;
        }

        while (true) {
            skipWhitespace();
            if (json.charAt(pos) != '"') {
                throw new RuntimeException("Expected string key at pos " + pos);
            }
            String key = parseString(); // parseString сам двигает pos
            
            skipWhitespace();
            if (json.charAt(pos) != ':') {
                throw new RuntimeException("Expected ':' at pos " + pos);
            }
            pos++; // skip ':'

            Object value = parseValue();
            map.put(key, value);

            skipWhitespace();
            char next = json.charAt(pos);
            if (next == '}') {
                pos++;
                break;
            } else if (next == ',') {
                pos++;
            } else {
                throw new RuntimeException("Expected ',' or '}' at pos " + pos);
            }
        }
        return map;
    }

    private List<Object> parseArray() {
        pos++; // skip '['
        List<Object> list = new ArrayList<>();

        if (currentChar() == ']') {
            pos++;
            return list;
        }

        while (true) {
            list.add(parseValue());
            skipWhitespace();
            char next = json.charAt(pos);
            if (next == ']') {
                pos++;
                break;
            } else if (next == ',') {
                pos++;
            } else {
                throw new RuntimeException("Expected ',' or ']' at pos " + pos);
            }
        }
        return list;
    }

    private String parseString() {
        pos++; 
        StringBuilder sb = new StringBuilder();
        
        while (pos < json.length()) {
            char c = json.charAt(pos);
            if (c == '"') {
                pos++;
                return sb.toString();
            }
            
            if (c == '\\') {
                pos++;
                if (pos >= json.length()) throw new RuntimeException("Unterminated string");
                char escape = json.charAt(pos);
                switch (escape) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u': 
                        
                        if (pos + 4 >= json.length()) throw new RuntimeException("Invalid unicode escape");
                        String hex = json.substring(pos + 1, pos + 5);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                        break;
                    default: throw new RuntimeException("Invalid escape character: " + escape);
                }
            } else {
                sb.append(c);
            }
            pos++;
        }
        throw new RuntimeException("Unterminated string");
    }

    private Number parseNumber() {
        int start = pos;
        // Читаем все символы, относящиеся к числу
        while (pos < json.length()) {
            char c = json.charAt(pos);
            if (Character.isDigit(c) || c == '-' || c == '.' || c == 'e' || c == 'E' || c == '+') {
                pos++;
            } else {
                break;
            }
        }
        
        String numStr = json.substring(start, pos);
        try {
           
            if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                return Double.parseDouble(numStr);
            } else {
                
                long l = Long.parseLong(numStr);
                
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                    return (int) l;
                }
                return l;
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format: " + numStr);
        }
    }

    private Boolean parseBoolean() {
        if (json.startsWith("true", pos)) {
            pos += 4;
            return true;
        } else if (json.startsWith("false", pos)) {
            pos += 5;
            return false;
        }
        throw new RuntimeException("Invalid boolean value at pos " + pos);
    }

    private Object parseNull() {
        if (json.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw new RuntimeException("Invalid null value at pos " + pos);
    }
}