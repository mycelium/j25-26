package jsonlib.parser;

import jsonlib.JsonParseException;

public class JsonTokenizer {
    private final String input;
    private int pos;

    public JsonTokenizer(String input) {
        this.input = input;
        this.pos = 0;
    }

    private char peek() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    private char advance() {
        return input.charAt(pos++);
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(peek())) {
            pos++;
        }
    }

    public Token nextToken() {
        skipWhitespace();
        if (pos >= input.length()) return new Token(TokenType.EOF);

        char c = peek();
        switch (c) {
            case '{': advance(); return new Token(TokenType.LBRACE);
            case '}': advance(); return new Token(TokenType.RBRACE);
            case '[': advance(); return new Token(TokenType.LBRACKET);
            case ']': advance(); return new Token(TokenType.RBRACKET);
            case ':': advance(); return new Token(TokenType.COLON);
            case ',': advance(); return new Token(TokenType.COMMA);
            case '"': return readString();
            default:
                if (c == '-' || Character.isDigit(c)) return readNumber();
                if (Character.isLetter(c)) return readLiteral();
                throw new JsonParseException("Unexpected character '" + c + "' at position " + pos);
        }
    }

    private Token readString() {
        advance(); // начальная кавычка
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = advance();
            if (c == '"') return new Token(TokenType.STRING, sb.toString());
            if (c == '\\') {
                if (pos >= input.length()) throw new JsonParseException("Unterminated string");
                char esc = advance();
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 > input.length()) throw new JsonParseException("Invalid unicode escape");
                        String hex = input.substring(pos, pos + 4);
                        pos += 4;
                        sb.append((char) Integer.parseInt(hex, 16));
                        break;
                    default:
                        throw new JsonParseException("Illegal escape: \\" + esc);
                }
            } else {
                sb.append(c);
            }
        }
        throw new JsonParseException("Unterminated string");
    }

    private Token readNumber() {
        int start = pos;
        if (peek() == '-') advance();
        if (!Character.isDigit(peek())) throw new JsonParseException("Invalid number at " + pos);
        while (pos < input.length() && Character.isDigit(peek())) advance();
        if (pos < input.length() && peek() == '.') {
            advance();
            if (!Character.isDigit(peek())) throw new JsonParseException("Invalid fraction at " + pos);
            while (pos < input.length() && Character.isDigit(peek())) advance();
        }
        if (pos < input.length() && (peek() == 'e' || peek() == 'E')) {
            advance();
            if (pos < input.length() && (peek() == '+' || peek() == '-')) advance();
            if (!Character.isDigit(peek())) throw new JsonParseException("Invalid exponent at " + pos);
            while (pos < input.length() && Character.isDigit(peek())) advance();
        }
        return new Token(TokenType.NUMBER, input.substring(start, pos));
    }

    private Token readLiteral() {
        int start = pos;
        while (pos < input.length() && Character.isLetter(peek())) advance();
        String lit = input.substring(start, pos);
        switch (lit) {
            case "true": return new Token(TokenType.TRUE);
            case "false": return new Token(TokenType.FALSE);
            case "null": return new Token(TokenType.NULL);
            default: throw new JsonParseException("Unknown literal: " + lit + " at " + start);
        }
    }
}