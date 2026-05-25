package jsonlib;

import java.util.*;

class JsonParser {

    // ── Token types ──────────────────────────────────────────────────────────

    private enum TokenType {
        LBRACE, RBRACE, LBRACKET, RBRACKET,
        COLON, COMMA,
        STRING, NUMBER, BOOLEAN, NULL,
        EOF
    }

    private static final class Token {
        final TokenType type;
        final Object    value;

        Token(TokenType type, Object value) {
            this.type  = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return type + (value != null ? "(" + value + ")" : "");
        }
    }

    // ── Lexer ─────────────────────────────────────────────────────────────────

    private static final class Lexer {
        private final String src;
        private int          pos;

        Lexer(String src) {
            this.src = src;
            this.pos = 0;
        }

        List<Token> tokenize() {
            List<Token> tokens = new ArrayList<>();
            while (true) {
                Token t = nextToken();
                tokens.add(t);
                if (t.type == TokenType.EOF) break;
            }
            return tokens;
        }

        private void skipWs() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
        }

        private char peek() {
            return pos < src.length() ? src.charAt(pos) : '\0';
        }

        private char advance() {
            if (pos >= src.length()) throw new JsonParseException("Unexpected end of input");
            return src.charAt(pos++);
        }

        private Token nextToken() {
            skipWs();
            if (pos >= src.length()) return new Token(TokenType.EOF, null);

            char c = peek();
            switch (c) {
                case '{': pos++; return new Token(TokenType.LBRACE,   null);
                case '}': pos++; return new Token(TokenType.RBRACE,   null);
                case '[': pos++; return new Token(TokenType.LBRACKET, null);
                case ']': pos++; return new Token(TokenType.RBRACKET, null);
                case ':': pos++; return new Token(TokenType.COLON,    null);
                case ',': pos++; return new Token(TokenType.COMMA,    null);
                case '"':        return new Token(TokenType.STRING,   readString());
                case 't': case 'f': return new Token(TokenType.BOOLEAN, readBoolean());
                case 'n':        return new Token(TokenType.NULL,     readNull());
                default:
                    if (c == '-' || Character.isDigit(c))
                        return new Token(TokenType.NUMBER, readNumber());
                    throw new JsonParseException("Unexpected char '" + c + "' at pos " + pos);
            }
        }

        private String readString() {
            advance(); // opening "
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = advance();
                if (c == '"') break;
                if (c != '\\') { sb.append(c); continue; }
                char esc = advance();
                switch (esc) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/');  break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'u':  sb.append(readUnicode()); break;
                    default:   throw new JsonParseException("Bad escape: \\" + esc);
                }
            }
            return sb.toString();
        }

        private char readUnicode() {
            if (pos + 4 > src.length())
                throw new JsonParseException("Incomplete \\u escape at pos " + pos);
            String hex = src.substring(pos, pos + 4);
            pos += 4;
            try { return (char) Integer.parseInt(hex, 16); }
            catch (NumberFormatException e) { throw new JsonParseException("Bad \\u: " + hex); }
        }

        private Boolean readBoolean() {
            if (src.startsWith("true",  pos)) { pos += 4; return Boolean.TRUE;  }
            if (src.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
            throw new JsonParseException("Invalid boolean at pos " + pos);
        }

        private Object readNull() {
            if (src.startsWith("null", pos)) { pos += 4; return null; }
            throw new JsonParseException("Invalid null at pos " + pos);
        }

        private Number readNumber() {
            int start = pos;
            if (peek() == '-') pos++;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;

            boolean decimal = false;
            if (pos < src.length() && src.charAt(pos) == '.') {
                decimal = true; pos++;
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            }
            if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
                decimal = true; pos++;
                if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) pos++;
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            }

            String raw = src.substring(start, pos);
            try {
                if (decimal) return Double.parseDouble(raw);
                long v = Long.parseLong(raw);
                return (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE) ? (int) v : v;
            } catch (NumberFormatException e) {
                throw new JsonParseException("Bad number: " + raw, e);
            }
        }
    }

    // ── Recursive-descent parser over token list ──────────────────────────────

    private final List<Token> tokens;
    private int               cursor;

    private JsonParser(List<Token> tokens) {
        this.tokens = tokens;
        this.cursor = 0;
    }

    static Object parse(String rawJson) {
        List<Token> tokens = new Lexer(rawJson).tokenize();
        JsonParser  p      = new JsonParser(tokens);
        Object      result = p.readValue();
        if (p.peek().type != TokenType.EOF)
            throw new JsonParseException("Trailing tokens after root value");
        return result;
    }

    private Token peek() { return tokens.get(cursor); }

    private Token consume() { return tokens.get(cursor++); }

    private Token expect(TokenType type) {
        Token t = consume();
        if (t.type != type)
            throw new JsonParseException("Expected " + type + " but got " + t);
        return t;
    }

    private Object readValue() {
        Token t = peek();
        switch (t.type) {
            case LBRACE:   return readObject();
            case LBRACKET: return readArray();
            case STRING:   consume(); return (String)  t.value;
            case NUMBER:   consume(); return (Number)  t.value;
            case BOOLEAN:  consume(); return (Boolean) t.value;
            case NULL:     consume(); return null;
            default: throw new JsonParseException("Unexpected token: " + t);
        }
    }

    private Map<String, Object> readObject() {
        expect(TokenType.LBRACE);
        Map<String, Object> map = new LinkedHashMap<>();
        if (peek().type == TokenType.RBRACE) { consume(); return map; }

        while (true) {
            String key = (String) expect(TokenType.STRING).value;
            expect(TokenType.COLON);
            map.put(key, readValue());
            if (peek().type == TokenType.RBRACE) { consume(); break; }
            expect(TokenType.COMMA);
        }
        return map;
    }

    private List<Object> readArray() {
        expect(TokenType.LBRACKET);
        List<Object> list = new ArrayList<>();
        if (peek().type == TokenType.RBRACKET) { consume(); return list; }

        while (true) {
            list.add(readValue());
            if (peek().type == TokenType.RBRACKET) { consume(); break; }
            expect(TokenType.COMMA);
        }
        return list;
    }
}