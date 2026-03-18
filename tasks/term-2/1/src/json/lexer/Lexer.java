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
                case '{' :
                    tokens.add(new Token(Token.Type.LBRACE,"{"));
                    break;
                case '}' :
                    tokens.add(new Token(Token.Type.RBRACE,"}"));
                    break;
                case '[' :
                    tokens.add(new Token(Token.Type.LBRACKET,"["));
                    break;
                case ']' :
                    tokens.add(new Token(Token.Type.RBRACKET,"]"));
                    break;
                case ',' :
                    tokens.add(new Token(Token.Type.COMMA,","));
                    break;
                case ':' :
                    tokens.add(new Token(Token.Type.COLON,":"));
                    break;

                case '"' :
                    StringBuilder sb = new StringBuilder();
                    while(peek() != '"' && peek() != '\0'){
                        sb.append(peek());
                        consume();
                    }
                    consume();
                    String literal = sb.toString();
                    tokens.add(new Token(Token.Type.STRING,literal));
                    break;
                case '-':
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':

                    StringBuilder sbn = new StringBuilder();
                    sbn.append(curr);
                    while(Character.isDigit(peek()) || peek() == '.' || peek() == 'e' || peek() == 'E' || peek() == '+' || peek() == '-'){
                        sbn.append(peek());
                        consume();

                    }
                    tokens.add(new Token(Token.Type.NUMBER,sbn.toString()));
                    break;

                case 't':
                    readLiteral("rue");
                    tokens.add(new Token(Token.Type.BOOLEAN, "true"));
                    break;
                case 'f':
                    readLiteral("alse");
                    tokens.add(new Token(Token.Type.BOOLEAN, "false"));
                    break;
                case 'n':
                    readLiteral("ull");
                    tokens.add(new Token(Token.Type.NULL, "null"));
                    break;

                default:
                    throw new RuntimeException("Unexpected character:" + curr);
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
