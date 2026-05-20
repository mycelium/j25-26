package lab1;
import java.util.ArrayList;
import java.util.List;

public class Splitter {

    private String json;
    private int pos;
    private List<String> tokens;
    private int genPos;
    
    public Splitter(String json) {
        this.json = json;
        this.pos = 0;
        this.tokens = new ArrayList<>();
        this.genPos = 0;
        split();
    }
    public String next() {
        if (genPos < tokens.size()) {
            return tokens.get(genPos++);
        }
        return null;
    }
        public String peek() {
        if (genPos < tokens.size()) {
            return tokens.get(genPos);
        }
        return null;
    }
    public boolean hasMore() {
        return genPos < tokens.size();
    }
    public void back() {
        if (genPos > 0) {
            genPos--;
        }
    }

    private void split() {
        while (pos < json.length()) {
            skipSpace();
            if (pos >= json.length()) break;
            char c = json.charAt(pos);
            if (c == '{' || c == '}' || c == '[' || c == ']' || c == ':' || c == ',') {
                tokens.add(String.valueOf(c));
                pos++;
            }
            else if (c == '"') {
                tokens.add(readString());
            }
            else {
                tokens.add(readValue());
            }
        }
    }
    private void skipSpace() {
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
            pos++;
        }
    }
    private String readString() {
        pos++;
        StringBuilder sb = new StringBuilder();
        while (pos < json.length() && json.charAt(pos) != '"') {
            sb.append(json.charAt(pos));
            pos++;
        }
        pos++;
        return sb.toString();
    }
    private String readValue() {
        StringBuilder sb = new StringBuilder();
        while (pos < json.length()) {
            char c = json.charAt(pos);
            if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                break;
            }
            sb.append(c);
            pos++;
        }
        return sb.toString().trim();
    }
}
