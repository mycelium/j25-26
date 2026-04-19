package jsonparser;

class JsonTokenizer {
    private final String src;
    private int pos;

    private TokenType type;
    private String value;

    JsonTokenizer(String src) {
        this.src = src;
        this.pos = 0;
        advance();
    }

    TokenType type() { return type; }
    String value() { return value; }

    void advance() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;

        if (pos >= src.length()) {
            type = TokenType.EOF;
            value = null;
            return;
        }

        char c = src.charAt(pos);
        switch (c) {
            case '{' -> { pos++; type = TokenType.BEGIN_OBJECT; value = "{"; }
            case '}' -> { pos++; type = TokenType.END_OBJECT;   value = "}"; }
            case '[' -> { pos++; type = TokenType.BEGIN_ARRAY;  value = "["; }
            case ']' -> { pos++; type = TokenType.END_ARRAY;    value = "]"; }
            case ',' -> { pos++; type = TokenType.COMMA;        value = ","; }
            case ':' -> { pos++; type = TokenType.COLON;        value = ":"; }
            case '"' -> readString();
            case 't' -> readKeyword("true",  TokenType.BOOLEAN);
            case 'f' -> readKeyword("false", TokenType.BOOLEAN);
            case 'n' -> readKeyword("null",  TokenType.NULL);
            default  -> {
                if (c == '-' || Character.isDigit(c)) readNumber();
                else throw new JsonException("Unexpected character '" + c + "' at position " + pos);
            }
        }
    }

    private void readString() {
        pos++; // skip opening "
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') {
                type = TokenType.STRING;
                value = sb.toString();
                return;
            }
            if (c == '\\') {
                if (pos >= src.length()) throw new JsonException("Unexpected end in string escape");
                char esc = src.charAt(pos++);
                switch (esc) {
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/'  -> sb.append('/');
                    case 'b'  -> sb.append('\b');
                    case 'f'  -> sb.append('\f');
                    case 'n'  -> sb.append('\n');
                    case 'r'  -> sb.append('\r');
                    case 't'  -> sb.append('\t');
                    case 'u'  -> {
                        if (pos + 4 > src.length()) throw new JsonException("Invalid unicode escape");
                        sb.append((char) Integer.parseInt(src.substring(pos, pos + 4), 16));
                        pos += 4;
                    }
                    default -> throw new JsonException("Invalid escape character: \\" + esc);
                }
            } else {
                sb.append(c);
            }
        }
        throw new JsonException("Unterminated string literal");
    }

    private void readNumber() {
        int start = pos;
        if (src.charAt(pos) == '-') pos++;
        while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        if (pos < src.length() && src.charAt(pos) == '.') {
            pos++;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        }
        if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
            pos++;
            if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) pos++;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        }
        type  = TokenType.NUMBER;
        value = src.substring(start, pos);
    }

    private void readKeyword(String keyword, TokenType t) {
        if (src.startsWith(keyword, pos)) {
            pos += keyword.length();
            type  = t;
            value = keyword;
        } else {
            throw new JsonException("Unexpected token at position " + pos);
        }
    }
}
