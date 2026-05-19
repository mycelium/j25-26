package ru.lab.json.parser;

import ru.lab.json.exception.JsonException;
import java.util.*;

public class JsonStringParser {
    private final String src;
    private int pos = 0;

    public JsonStringParser(String src) {
        if (src == null || src.isBlank()) {
            throw new JsonException("JSON string cannot be null or empty");
        }
        this.src = src;
    }

    public Object parse() {
        skipWhitespace();
        if (pos >= src.length()) {
            throw new JsonException("Unexpected end of JSON input");
        }
        
        char current = peek();
        switch (current) {
            case '{': return parseObject();
            case '[': return parseArray();
            case '"': return parseString();
            case 't':
            case 'f': return parseBoolean();
            case 'n': 
                consume("null"); 
                return null;
            default: return parseNumber();
        }
    }

    private Map<String, Object> parseObject() {
        var map = new LinkedHashMap<String, Object>();
        consume("{");
        skipWhitespace();
        if (peek() == '}') {
            consume("}");
            return map;
        }
        while (true) {
            skipWhitespace();
            var key = parseString();
            skipWhitespace();
            consume(":");
            map.put(key, parse());
            skipWhitespace();
            if (peek() == '}') {
                consume("}");
                break;
            }
            consume(",");
        }
        return map;
    }

    private List<Object> parseArray() {
        var list = new ArrayList<>();
        consume("[");
        skipWhitespace();
        if (peek() == ']') {
            consume("]");
            return list;
        }
        while (true) {
            list.add(parse());
            skipWhitespace();
            if (peek() == ']') {
                consume("]");
                break;
            }
            consume(",");
        }
        return list;
    }

    private String parseString() {
        consume("\"");
        var sb = new StringBuilder();
        while (pos < src.length()) {
            char c = next();
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (pos >= src.length()) throw new JsonException("Unterminated escape sequence");
                char escaped = next();
                switch (escaped) {
                    case '"':  sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'u': {
                        if (pos + 4 > src.length()) throw new JsonException("Invalid unicode escape");
                        String hex = src.substring(pos, pos + 4);
                        pos += 4;
                        sb.append((char) Integer.parseInt(hex, 16));
                        break;
                    }
                    default: throw new JsonException("Illegal escape character: \\" + escaped);
                }
            } else {
                sb.append(c);
            }
        }
        throw new JsonException("Unterminated string in JSON");
    }

    private Number parseNumber() {
        int start = pos;
        if (peek() == '-') next();
        
        char c = peek();
        if (c == '0') {
            next();
        } else if (c >= '0' && c <= '9') { 
            while (peek() >= '0' && peek() <= '9') next();
        } else {
            throw new JsonException("Expected digit at position " + pos);
        }

        boolean isDouble = false;
        if (peek() == '.') {
            isDouble = true;
            next();
            if (!(peek() >= '0' && peek() <= '9')) throw new JsonException("Expected fractional part");
            while (peek() >= '0' && peek() <= '9') next();
        }

        if (peek() == 'e' || peek() == 'E') {
            isDouble = true;
            next();
            char expSign = peek();
            if (expSign == '+' || expSign == '-') next();
            if (!(peek() >= '0' && peek() <= '9')) throw new JsonException("Expected exponent");
            while (peek() >= '0' && peek() <= '9') next();
        }

        String val = src.substring(start, pos);
        try {
            if (isDouble) return Double.parseDouble(val);
            long longVal = Long.parseLong(val);
            if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                return (int) longVal;
            }
            return longVal;
        } catch (NumberFormatException e) {
            throw new JsonException("Invalid number format: " + val, e);
        }
    }

    private Boolean parseBoolean() {
        if (peek() == 't') { consume("true"); return true; }
        consume("false"); return false;
    }

    private void skipWhitespace() {
        while (pos < src.length() && " \t\n\r".indexOf(src.charAt(pos)) != -1) pos++;
    }

    private char peek() { return pos < src.length() ? src.charAt(pos) : 0; }
    private char next() { return src.charAt(pos++); }

    private void consume(String s) {
        for (char c : s.toCharArray()) {
            if (pos >= src.length() || next() != c) {
                throw new JsonException("Expected '" + s + "' at position " + (pos - 1));
            }
        }
    }
}