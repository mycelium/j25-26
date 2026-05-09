package ru.example.json;

public class JsonTokenizer {

    private final String input;
    private int pos = 0;

    public JsonTokenizer(String input) {
        this.input = input.trim();
    }

    public char peek() {
        return input.charAt(pos);
    }

    public char next() {
        return input.charAt(pos++);
    }

    public boolean hasNext() {
        return pos < input.length();
    }

    public void skipWhitespace() {
        while (hasNext() && Character.isWhitespace(peek())) {
            pos++;
        }
    }

    public String readString() {
        StringBuilder sb = new StringBuilder();
        next(); // skip "
        while (peek() != '"') {
            sb.append(next());
        }
        next();
        return sb.toString();
    }

    public String readNumber() {
        StringBuilder sb = new StringBuilder();
        while (hasNext() && (Character.isDigit(peek()) || peek() == '.')) {
            sb.append(next());
        }
        return sb.toString();
    }

    public String readLiteral() {
        StringBuilder sb = new StringBuilder();
        while (hasNext() && Character.isLetter(peek())) {
            sb.append(next());
        }
        return sb.toString();
    }
}
