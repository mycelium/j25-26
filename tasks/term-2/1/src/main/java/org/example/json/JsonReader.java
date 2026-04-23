package org.example.json;

import java.util.*;

class JsonReader {
    private String source;
    private int position;
    private int length;
    
    static Object process(String json) {
        JsonReader reader = new JsonReader(json);
        return reader.readValue();
    }
    
    private JsonReader(String input) {
        this.source = input;
        this.position = 0;
        this.length = input.length();
    }
    
    private void skipSpaces() {
        while (position < length && Character.isWhitespace(source.charAt(position))) {
            position++;
        }
    }
    
    private char currentChar() {
        if (position >= length) return 0;
        return source.charAt(position);
    }
    
    private char nextChar() {
        if (position >= length) {
            throw new RuntimeException("Неожиданный конец строки");
        }
        return source.charAt(position++);
    }
    
    private void expectChar(char expected) {
        char actual = nextChar();
        if (actual != expected) {
            throw new RuntimeException("Ожидался символ '" + expected + "', получен '" + actual + "'");
        }
    }
    
    private Object readValue() {
        skipSpaces();
        char c = currentChar();
        
        if (c == '{') return readObject();
        if (c == '[') return readArray();
        if (c == '"') return readString();
        if (c == 't' || c == 'f') return readBoolean();
        if (c == 'n') return readNull();
        if (c == '-' || (c >= '0' && c <= '9')) return readNumber();
        
        throw new RuntimeException("Неожиданный символ: " + c);
    }
    
    private Map<String, Object> readObject() {
        Map<String, Object> result = new LinkedHashMap<>();
        expectChar('{');
        skipSpaces();
        
        if (currentChar() == '}') {
            nextChar();
            return result;
        }
        
        while (true) {
            skipSpaces();
            String key = readString();
            skipSpaces();
            expectChar(':');
            Object value = readValue();
            result.put(key, value);
            
            skipSpaces();
            char c = currentChar();
            if (c == '}') {
                nextChar();
                break;
            } else if (c == ',') {
                nextChar();
                continue;
            } else {
                throw new RuntimeException("Ожидалась ',' или '}'");
            }
        }
        return result;
    }
    
    private List<Object> readArray() {
        List<Object> result = new ArrayList<>();
        expectChar('[');
        skipSpaces();
        
        if (currentChar() == ']') {
            nextChar();
            return result;
        }
        
        while (true) {
            result.add(readValue());
            skipSpaces();
            char c = currentChar();
            if (c == ']') {
                nextChar();
                break;
            } else if (c == ',') {
                nextChar();
                continue;
            } else {
                throw new RuntimeException("Ожидалась ',' или ']'");
            }
        }
        return result;
    }
    
    private String readString() {
        expectChar('"');
        StringBuilder sb = new StringBuilder();
        
        while (true) {
            char c = nextChar();
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                char esc = nextChar();
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    private Boolean readBoolean() {
        if (source.startsWith("true", position)) {
            position += 4;
            return true;
        }
        if (source.startsWith("false", position)) {
            position += 5;
            return false;
        }
        throw new RuntimeException("Ожидалось true или false");
    }
    
    private Object readNull() {
        if (source.startsWith("null", position)) {
            position += 4;
            return null;
        }
        throw new RuntimeException("Ожидалось null");
    }
    
    private Number readNumber() {
        int start = position;
        
        if (currentChar() == '-') {
            nextChar();
        }
        
        while (position < length && Character.isDigit(currentChar())) {
            nextChar();
        }
        
        if (currentChar() == '.') {
            nextChar();
            while (position < length && Character.isDigit(currentChar())) {
                nextChar();
            }
        }
        
        if (currentChar() == 'e' || currentChar() == 'E') {
            nextChar();
            if (currentChar() == '+' || currentChar() == '-') {
                nextChar();
            }
            while (position < length && Character.isDigit(currentChar())) {
                nextChar();
            }
        }
        
        String numStr = source.substring(start, position);
        if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
            return Double.parseDouble(numStr);
        } else {
            long longVal = Long.parseLong(numStr);
            if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                return (int) longVal;
            }
            return longVal;
        }
    }
}
