package json.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String input;
    private int pos;

    public Lexer(String input) {
        this.input = input;
        this.pos = 0;
    }

    private char peek(){
        if(pos >= input.length()){
            return '\0';
        }
        return input.charAt(pos);
    }

    private char consume(){
        if(pos >= input.length()){
            return '\0';
        }
        char curr = input.charAt(pos);
        pos++;
        return curr;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        char curr;
        while(peek() != '\0') {
            if(peek() == ' ' || peek() == '\t' || peek() == '\n' || peek() == '\r') {
                consume();
                continue;
            }
            curr = consume();

            switch(curr){
                case '{' -> tokens.add(new Token(Token.Type.LBRACE,"{"));
                case '}' -> tokens.add(new Token(Token.Type.RBRACE,"}"));
                case '[' -> tokens.add(new Token(Token.Type.LBRACKET,"["));
                case ']' -> tokens.add(new Token(Token.Type.RBRACKET,"]"));
                case ',' -> tokens.add(new Token(Token.Type.COMMA,","));
                case ':' -> tokens.add(new Token(Token.Type.COLON,":"));

                case '"' -> {
                    StringBuilder sb = new StringBuilder();
                    while(peek() != '"' && peek() != '\0'){
                        if(peek() == '\\'){
                            consume();
                            char esc = consume();
                            switch(esc){
                                case '"' -> sb.append('"');
                                case '\\' -> sb.append('\\');
                                case '/' -> sb.append('/');
                                case 'n' -> sb.append('\n');
                                case 'r' -> sb.append('\r');
                                case 't' -> sb.append('\t');
                                case 'b' -> sb.append('\b');
                                case 'f' -> sb.append('\f');
                                default -> throw new RuntimeException("Unknown escape: \\" + esc);
                            }
                        } else {
                            sb.append(consume());
                        }
                    }
                    consume();
                    tokens.add(new Token(Token.Type.STRING, sb.toString()));
                }

                case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    StringBuilder sbn = new StringBuilder();
                    sbn.append(curr);
                    while(Character.isDigit(peek()) || peek() == '.' || peek() == 'e' || peek() == 'E' || peek() == '+' || peek() == '-'){
                        sbn.append(consume());
                    }
                    tokens.add(new Token(Token.Type.NUMBER, sbn.toString()));
                }

                case 't' -> {
                    readLiteral("rue");
                    tokens.add(new Token(Token.Type.BOOLEAN, "true"));
                }
                case 'f' -> {
                    readLiteral("alse");
                    tokens.add(new Token(Token.Type.BOOLEAN, "false"));
                }
                case 'n' -> {
                    readLiteral("ull");
                    tokens.add(new Token(Token.Type.NULL, "null"));
                }

                default -> throw new RuntimeException("Unexpected character:" + curr);
            }
        }

        tokens.add(new Token(Token.Type.EOF,""));

        return tokens;
    }

    private void readLiteral(String literal) {
        for (char c : literal.toCharArray()) {
            char next = consume();
            if (next != c) {
                throw new RuntimeException("Unexpected character while parsing literal: expected " + c + " but got " + next);
            }
        }
    }
}
