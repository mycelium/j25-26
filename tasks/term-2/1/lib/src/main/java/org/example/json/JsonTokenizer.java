package org.example.json;

import java.util.ArrayList;
import java.util.List;

public class JsonTokenizer {

    public enum TokenType {
        STRING, NUMBER, BOOLEAN, NULL,
        OBJECT_START, OBJECT_END,
        ARRAY_START, ARRAY_END,
        COLON, COMMA, EOF
    }

    public record Token(TokenType type, String value) {}

    private final String input;
    private int pos = 0;
    private final List<Token> tokens;
    private int tokenPos = 0;

    public JsonTokenizer(String input) {
        this.input = input.trim();
        this.tokens = tokenize();
    }

    private List<Token> tokenize() {
        List<Token> list = new ArrayList<>();
        while (pos < input.length()) {
            skipWhitespace();
            if (pos >= input.length()) break;
            char c = input.charAt(pos);
            switch (c) {
                case '{' -> { list.add(new Token(TokenType.OBJECT_START, "{")); pos++; }
                case '}' -> { list.add(new Token(TokenType.OBJECT_END, "}")); pos++; }
                case '[' -> { list.add(new Token(TokenType.ARRAY_START, "[")); pos++; }
                case ']' -> { list.add(new Token(TokenType.ARRAY_END, "]")); pos++; }
                case ':' -> { list.add(new Token(TokenType.COLON, ":")); pos++; }
                case ',' -> { list.add(new Token(TokenType.COMMA, ",")); pos++; }
                case '"' -> list.add(readString());
                case 't', 'f' -> list.add(readBoolean());
                case 'n' -> list.add(readNull());
                default -> {
                    if (c == '-' || Character.isDigit(c)) {
                        list.add(readNumber());
                    } else {
                        throw new JsonException("Unexpected character: " + c + " at position " + pos);
                    }
                }
            }
        }
        list.add(new Token(TokenType.EOF, ""));
        return list;
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private Token readString() {
        pos++; // skip opening "
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '"') {
                pos++;
                return new Token(TokenType.STRING, sb.toString());
            } else if (c == '\\') {
                pos++;
                if (pos >= input.length()) break;
                char esc = input.charAt(pos);
                switch (esc) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        String hex = input.substring(pos + 1, Math.min(pos + 5, input.length()));
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                    }
                    default -> sb.append(esc);
                }
                pos++;
            } else {
                sb.append(c);
                pos++;
            }
        }
        throw new JsonException("Unterminated string");
    }

    private Token readBoolean() {
        if (input.startsWith("true", pos)) {
            pos += 4;
            return new Token(TokenType.BOOLEAN, "true");
        } else if (input.startsWith("false", pos)) {
            pos += 5;
            return new Token(TokenType.BOOLEAN, "false");
        }
        throw new JsonException("Invalid token at position " + pos);
    }

    private Token readNull() {
        if (input.startsWith("null", pos)) {
            pos += 4;
            return new Token(TokenType.NULL, "null");
        }
        throw new JsonException("Invalid token at position " + pos);
    }

    private Token readNumber() {
        int start = pos;
        if (input.charAt(pos) == '-') pos++;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
        if (pos < input.length() && input.charAt(pos) == '.') {
            pos++;
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
        }
        if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
            pos++;
            if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) pos++;
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
        }
        return new Token(TokenType.NUMBER, input.substring(start, pos));
    }

    public Token peek() {
        return tokens.get(tokenPos);
    }

    public Token next() {
        Token t = tokens.get(tokenPos);
        if (t.type() != TokenType.EOF) tokenPos++;
        return t;
    }

    public Token expect(TokenType type) {
        Token t = next();
        if (t.type() != type) {
            throw new JsonException("Expected " + type + " but got " + t.type() + " ('" + t.value() + "')");
        }
        return t;
    }
}
