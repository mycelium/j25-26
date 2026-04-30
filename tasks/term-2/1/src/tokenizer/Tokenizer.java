package tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tokenizer {
    private final String input;
    private int pos;

    public Tokenizer(String input){
        this.input = removeSpaces(input);
        this.pos = 0;
    }

    public String getJSON(){
        return input;
    }


    public Token nextToken(){
        if (pos >= input.length())
            return new Token(TokenType.EOF, null);

        char c = input.charAt(pos);

        switch (c){
            case '[':
                pos ++;
                return new Token(TokenType.LEFT_SQR, "[");
            case ']':
                pos ++;
                return new Token(TokenType.RIGHT_SQR, "]");
            case '{':
                pos ++;
                return new Token(TokenType.LEFT_FIG, "{");
            case '}':
                pos ++;
                return new Token(TokenType.RIGHT_FGR, "}");
            case ':':
                pos ++;
                return new Token(TokenType.COLON, ":");
            case ',':
                pos ++;
                return new Token(TokenType.COMMA, ",");
            case '\"':
                return getStringToken(input);
            default:
                return getValueToken(input);

        }
    }

    private Token getStringToken(String input){
//        String word = "\"";
        String word = "";
        pos ++; //skip "
        while (input.charAt(pos)!='\"'){
            word += input.charAt(pos);
            pos ++;
        }
//        word += "\"";
        pos ++;

        return new Token(TokenType.STRING, word);
    }

    private Token getValueToken(String input){
        String value = "";
        while (input.charAt(pos)!='\"'
                && input.charAt(pos) != '{'
                && input.charAt(pos) != '}'
                && input.charAt(pos) != '['
                && input.charAt(pos) != ']'
                && input.charAt(pos) != ','
                && input.charAt(pos) != ':'){
            value += input.charAt(pos);
            pos ++;
        }
        if (value.compareTo("false") == 0)
            return new Token(TokenType.FALSE, "false");
        if (value.compareTo("true") == 0)
            return new Token(TokenType.TRUE, "true");
        if (value.compareTo("null") == 0)
            return new Token(TokenType.NULL, "null");

        return new Token(TokenType.VALUE, value);
    }

    private String removeSpaces(String input){
        String result = "";
        for (int i=0; i< input.length(); i++){
            char c = input.charAt(i);
            if (c == '\"'){
                result += c;

                for (int j=i+1; j< input.length(); j++){
                    char cv = input.charAt(j);
                    if (cv != '\"'){
                        result += cv;
                    }
                    else {
                        result +=cv;
                        i = j;
                        break;
                    }
                }
            }
            else {
                if (c != ' ' && c != '\n'){
                    result += c;
                }
            }
        }
        return result;
    }

    private Object parseValue() {
        Token token = nextToken();
        return parseValue(token);
    }

    private Object parseValue(Token token){

        switch (token.getType()){
            case TokenType.LEFT_FIG:
                return parseObject();
            case TokenType.LEFT_SQR:
                return parseArray();
            case TokenType.STRING:
                return token.getValue();
            case TokenType.TRUE:
                return true;
            case TokenType.FALSE:
                return false;
            case TokenType.NULL:
                return null;
            case TokenType.VALUE:
                return parseDigValue(token.getValue());
            default:
                throw new RuntimeException("Unexpected token: " + token);
        }
    }

    private Object parseDigValue(String dig) {
        if (dig.contains(".")){
            return new Double(dig);
        }
        else {
            return new Integer(dig);
        }
    }
//
//    private Token getStringValue(String input){
//        String word = "";
//        pos++;
//        while (input.charAt(pos) != '\"'){
//            word += input.charAt(pos);
//            pos++;
//        }
//
//        pos++;
//        return new Token(TokenType.STRING, word);
//    }

    private Object parseArray() {
        List<Object> array = new ArrayList<>();

        Token token = nextToken();

        if (token.getType() == TokenType.RIGHT_SQR){
            return array;
        }

        while(true){
            Object value = parseValue(token);

            array.add(value);

            token = nextToken();

            if (token.getType() == TokenType.RIGHT_SQR) {
                break;
            }

            if (token.getType() != TokenType.COMMA) {
                throw new RuntimeException("miss ','");
            }

            token = nextToken();
        }
        return array;
    }

    private Map<String, Object> parseObject(){
        Map<String, Object> map = new HashMap<>();

        Token token = nextToken();

        if (token.getType() == TokenType.RIGHT_FGR){
            return map;
        }

        while(true){
            if (token.getType() != TokenType.STRING){
                throw new RuntimeException("incorrect type of key");
            }

            String key = token.getValue();

            token = nextToken();
            if (TokenType.COLON != token.getType()){
                throw new RuntimeException("miss ':'");
            }

            Object value = parseValue();
            map.put(key, value);

            token = nextToken();
            if (token.getType() == TokenType.RIGHT_FGR) {
                break;
            }

            if (token.getType() != TokenType.COMMA) {
                throw new RuntimeException("miss ','");
            }

            token = nextToken();
        }
        return map;
    }

    public Object parseJSON(){
        return parseValue();
    }

    public Map<String, Object> parseJSONToMap() {
        Object result = parseValue();

        if (!(result instanceof Map)) {
            throw new RuntimeException("json is not a map");
        }

        return (Map<String, Object>) result;
    }
}
