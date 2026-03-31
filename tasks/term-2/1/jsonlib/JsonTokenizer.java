package jsonlib;

class JsonTokenizer {
    private final String input;
    private int pos;
    private String currentToken;

    JsonTokenizer(String input) {
        this.input = input;
        this.pos = 0;
        nextToken();
    }

    String peek() {
        return currentToken;
    }

    String next() {
        String token = currentToken;
        nextToken();
        return token;
    }

    private void nextToken() {
        skipWhitespace();
        if (pos >= input.length()) {
            currentToken = null;
            return;
        }

        char c = input.charAt(pos);
        if (c == '{' || c == '}' || c == '[' || c == ']' || c == ':' || c == ',') {
            currentToken = String.valueOf(c);
            pos++;
            return;
        }
        
        if (c == '"') {
            int start = pos;
            pos++;
            while (pos < input.length()) {
                c = input.charAt(pos);
                if (c == '\\') {
                    pos += 2; 
                } else if (c == '"') {
                    pos++;
                    break;
                } else {
                    pos++;
                }
            }
            currentToken = input.substring(start, pos);
            return;
        }
        
        int start = pos;
        while (pos < input.length()) {
            c = input.charAt(pos);
            if (Character.isWhitespace(c) || c == ',' || c == '}' || c == ']' || c == ':') {
                break;
            }
            pos++;
        }
        currentToken = input.substring(start, pos);
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }
}