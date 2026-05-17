package json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


class JsonParser {

    private final String input;
    private int pos;

    JsonParser(String input) {
        this.input = input;
        this.pos   = 0;
    }

    Object parse() {
        skipWhitespace();
        var value = parseValue();
        skipWhitespace();
        if (pos != input.length()) {
            throw new JsonException("Unexpected characters at position " + pos + ": '" + input.charAt(pos) + "'");
        }
        return value;
    }


    private Object parseValue() {
        skipWhitespace();
        if (pos >= input.length()) {
            throw new JsonException("Unexpected end of input");
        }
        return switch (input.charAt(pos)) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't' -> parseLiteral("true",  Boolean.TRUE);
            case 'f' -> parseLiteral("false", Boolean.FALSE);
            case 'n' -> parseLiteral("null",  null);
            default  -> {
                char c = input.charAt(pos);
                if (c == '-' || Character.isDigit(c)) {
                    yield parseNumber();
                }
                throw new JsonException("Unexpected character '" + c + "' at position " + pos);
            }
        };
    }

    private Map<String, Object> parseObject() {
        consume('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') { pos++; return map; }

        while (true) {
            skipWhitespace();
            var key = parseString();
            skipWhitespace();
            consume(':');
            skipWhitespace();
            var value = parseValue();
            map.put(key, value);
            skipWhitespace();
            char next = requireNext("object");
            if (next == '}') { pos++; break; }
            if (next != ',') throw new JsonException("Expected ',' or '}' at position " + pos);
            pos++;
        }
        return map;
    }

    private List<Object> parseArray() {
        consume('[');
        List<Object> list = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') { pos++; return list; }

        while (true) {
            skipWhitespace();
            list.add(parseValue());
            skipWhitespace();
            char next = requireNext("array");
            if (next == ']') { pos++; break; }
            if (next != ',') throw new JsonException("Expected ',' or ']' at position " + pos);
            pos++;
        }
        return list;
    }

    private String parseString() {
        consume('"');
        var sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos++);
            switch (c) {
                case '"'  -> { return sb.toString(); }
                case '\\' -> sb.append(parseEscape());
                default   -> {
                    if (c < 0x20) {
                        throw new JsonException("Unescaped control character U+" + String.format("%04X", (int) c));
                    }
                    sb.append(c);
                }
            }
        }
        throw new JsonException("Unterminated string literal");
    }

    private char parseEscape() {
        if (pos >= input.length()) throw new JsonException("Unexpected end of input after '\\'");
        char esc = input.charAt(pos++);
        return switch (esc) {
            case '"'  -> '"';
            case '\\' -> '\\';
            case '/'  -> '/';
            case 'b'  -> '\b';
            case 'f'  -> '\f';
            case 'n'  -> '\n';
            case 'r'  -> '\r';
            case 't'  -> '\t';
            case 'u'  -> parseUnicodeEscape();
            default   -> throw new JsonException("Invalid escape sequence: '\\" + esc + "'");
        };
    }

    private char parseUnicodeEscape() {
        if (pos + 4 > input.length()) {
            throw new JsonException("Incomplete \\uXXXX escape at position " + pos);
        }
        var hex = input.substring(pos, pos + 4);
        pos += 4;
        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            throw new JsonException("Invalid unicode escape: \\u" + hex);
        }
    }

    private Number parseNumber() {
        int start = pos;
        boolean isDecimal = false;

        if (peek() == '-') pos++;

        // inetger part
        if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) {
            throw new JsonException("Invalid number at position " + start);
        }
        // zero check
        if (input.charAt(pos) == '0') {
            pos++;
        } else {
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
        }

        // fraction
        if (pos < input.length() && input.charAt(pos) == '.') {
            isDecimal = true;
            pos++;
            if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) {
                throw new JsonException("Expected digit after '.' at position " + pos);
            }
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
        }

        // exp
        if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
            isDecimal = true;
            pos++;
            if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) pos++;
            if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) {
                throw new JsonException("Expected digit in exponent at position " + pos);
            }
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
        }

        var numStr = input.substring(start, pos);
        if (isDecimal) {
            return Double.parseDouble(numStr);
        }
        // Return if it in 32 bits
        var longVal = Long.parseLong(numStr);
        if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
            return (int) longVal;
        }
        return longVal;
    }

    private Object parseLiteral(String literal, Object value) {
        if (input.startsWith(literal, pos)) {
            pos += literal.length();
            return value;
        }
        throw new JsonException("Invalid token at position " + pos + " (expected '" + literal + "')");
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++;
    }

    private void consume(char expected) {
        if (pos >= input.length() || input.charAt(pos) != expected) {
            throw new JsonException("Expected '" + expected + "' at position " + pos
                    + (pos < input.length() ? " but found '" + input.charAt(pos) + "'" : " (end of input)"));
        }
        pos++;
    }

    private char peek() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    private char requireNext(String context) {
        if (pos >= input.length()) {
            throw new JsonException("Unexpected end of input inside " + context);
        }
        return input.charAt(pos);
    }
}
