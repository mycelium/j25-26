package jsonlab;

import java.util.*;

final class JsonReader {
    private final char[] buffer;
    private int ptr = 0;
    private int line = 1, col = 1;

    JsonReader(String input) {
        this.buffer = (input != null ? input : "").toCharArray();
    }

    Object readValue() {
        skipWS();
        if (ptr >= buffer.length) throw new JsonException("Empty input", ptr, line, col);
        return parseAny();
    }

    private Object parseAny() {
        return switch (peekChar()) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> readString();
            case 't', 'f' -> readBool();
            case 'n' -> readNull();
            default -> {
                if (isDigitOrSign()) yield readNumber();
                throw unexpected("Expected JSON value");
            }
        };
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> res = new LinkedHashMap<>();
        consume('{');
        skipWS();
        if (peekChar() == '}') { consume('}'); return res; }
        while (true) {
            String key = readString();
            skipWS(); consume(':');
            res.put(key, parseAny());
            skipWS();
            char next = peekChar();
            if (next == ',') { consume(','); skipWS(); }
            else if (next == '}') { consume('}'); break; }
            else throw unexpected("Expected ',' or '}'");
        }
        return res;
    }

    private List<Object> parseArray() {
        List<Object> res = new ArrayList<>();
        consume('[');
        skipWS();
        if (peekChar() == ']') { consume(']'); return res; }
        while (true) {
            res.add(parseAny());
            skipWS();
            char next = peekChar();
            if (next == ',') { consume(','); skipWS(); }
            else if (next == ']') { consume(']'); break; }
            else throw unexpected("Expected ',' or ']'");
        }
        return res;
    }

    private String readString() {
        consume('"');
        StringBuilder sb = new StringBuilder();
        while (ptr < buffer.length) {
            char ch = buffer[ptr];
            if (ch == '"') { advance(); return sb.toString(); }
            if (ch == '\\') {
                advance();
                char esc = peekChar();
                char decoded = switch (esc) {
                    case '"' -> '"'; case '\\' -> '\\'; case '/' -> '/';
                    case 'b' -> '\b'; case 'f' -> '\f'; case 'n' -> '\n';
                    case 'r' -> '\r'; case 't' -> '\t';
                    case 'u' -> readUnicode();
                    default -> throw new JsonException("Invalid escape '\\" + esc + "'", ptr, line, col);
                };
                sb.append(decoded); advance();
            } else if (ch < 0x20) {
                throw new JsonException("Unescaped control char", ptr, line, col);
            } else {
                sb.append(ch); advance();
            }
        }
        throw new JsonException("Unterminated string", ptr, line, col);
    }

    private char readUnicode() {
        int start = ptr + 1;
        if (ptr + 4 >= buffer.length) throw new JsonException("Incomplete \\uXXXX", ptr, line, col);
        int code = 0;
        for (int i = 0; i < 4; i++) {
            char h = buffer[ptr + 1 + i];
            int val = hexVal(h);
            if (val < 0) throw new JsonException("Bad hex digit '" + h + "'", ptr + 1 + i, line, col);
            code = (code << 4) | val;
        }
        ptr += 5; col += 5;
        return (char) code;
    }

    private int hexVal(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        return -1;
    }

    private Number readNumber() {
        int start = ptr;
        while (ptr < buffer.length && isNumChar(buffer[ptr])) ptr++;
        String num = new String(buffer, start, ptr - start);
        col += ptr - start;

        if (num.contains(".") || num.contains("e") || num.contains("E")) {
            return Double.parseDouble(num);
        }
        try { return Integer.parseInt(num); }
        catch (NumberFormatException e) { return Long.parseLong(num); }
    }

    private boolean readBool() {
        if (match("true")) return true;
        if (match("false")) return false;
        throw new JsonException("Invalid boolean", ptr, line, col);
    }

    private Object readNull() {
        if (!match("null")) throw new JsonException("Invalid null", ptr, line, col);
        return null;
    }

    private void skipWS() {
        while (ptr < buffer.length) {
            char c = buffer[ptr];
            switch (c) {
                case ' ', '\t' -> advance();
                case '\n' -> { advance(); line++; col = 1; }
                case '\r' -> { advance(); if (ptr < buffer.length && buffer[ptr] == '\n') advance(); line++; col = 1; }
                default -> { return; }
            }
        }
    }

    private char peekChar() { return ptr < buffer.length ? buffer[ptr] : '\0'; }
    private void consume(char expected) {
        if (ptr >= buffer.length || buffer[ptr] != expected)
            throw unexpected("Expected '" + expected + "'");
        advance();
    }
    private void advance() { if (ptr < buffer.length) { ptr++; col++; } }
    private boolean match(String lit) {
        if (ptr + lit.length() > buffer.length) return false;
        for (int i = 0; i < lit.length(); i++) if (buffer[ptr + i] != lit.charAt(i)) return false;
        ptr += lit.length(); col += lit.length(); return true;
    }
    private boolean isDigitOrSign() {
        char c = peekChar();
        return (c >= '0' && c <= '9') || c == '-' || c == '+';
    }
    private boolean isNumChar(char c) {
        return Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '-' || c == '+';
    }
    private JsonException unexpected(String msg) {
        return new JsonException(msg + " (found '" + peekChar() + "')", ptr, line, col);
    }
}