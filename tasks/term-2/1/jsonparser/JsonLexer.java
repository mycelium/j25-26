package jsonparser;

// Лексер: разбивает JSON-строку на токены
class JsonLexer {

    private final String src;
    private int pos;

    private TokenType currentType;
    private String currentValue;

    JsonLexer(String src) {
        this.src = src;
        this.pos = 0;
        next(); // читаем первый токен
    }

    TokenType type() { return currentType; }
    String value()   { return currentValue; }

    // Переходим к следующему токену
    void next() {
        skipWhitespace();

        if (pos >= src.length()) {
            currentType  = TokenType.EOF;
            currentValue = null;
            return;
        }

        char c = src.charAt(pos);
        switch (c) {
            case '{' -> { pos++; currentType = TokenType.LBRACE;   currentValue = "{"; }
            case '}' -> { pos++; currentType = TokenType.RBRACE;   currentValue = "}"; }
            case '[' -> { pos++; currentType = TokenType.LBRACKET; currentValue = "["; }
            case ']' -> { pos++; currentType = TokenType.RBRACKET; currentValue = "]"; }
            case ',' -> { pos++; currentType = TokenType.COMMA;    currentValue = ","; }
            case ':' -> { pos++; currentType = TokenType.COLON;    currentValue = ":"; }
            case '"' -> readString();
            case 't' -> readKeyword("true",  TokenType.BOOLEAN);
            case 'f' -> readKeyword("false", TokenType.BOOLEAN);
            case 'n' -> readKeyword("null",  TokenType.NULL);
            default  -> {
                if (c == '-' || Character.isDigit(c)) readNumber();
                else throw new JsonException("Неожиданный символ '" + c + "' на позиции " + pos);
            }
        }
    }

    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }

    private void readString() {
        pos++; // пропускаем открывающую кавычку
        StringBuilder sb = new StringBuilder();

        while (pos < src.length()) {
            char c = src.charAt(pos++);

            if (c == '"') {
                currentType  = TokenType.STRING;
                currentValue = sb.toString();
                return;
            }

            if (c == '\\') {
                if (pos >= src.length()) throw new JsonException("Неожиданный конец строки в escape-последовательности");
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
                        if (pos + 4 > src.length()) throw new JsonException("Неверная unicode escape-последовательность");
                        String hex = src.substring(pos, pos + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                    }
                    default -> throw new JsonException("Неизвестный escape-символ: \\" + esc);
                }
            } else {
                sb.append(c);
            }
        }
        throw new JsonException("Строка не закрыта");
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
        currentType  = TokenType.NUMBER;
        currentValue = src.substring(start, pos);
    }

    private void readKeyword(String keyword, TokenType type) {
        if (src.startsWith(keyword, pos)) {
            pos += keyword.length();
            currentType  = type;
            currentValue = keyword;
        } else {
            throw new JsonException("Неожиданный токен на позиции " + pos);
        }
    }
}
