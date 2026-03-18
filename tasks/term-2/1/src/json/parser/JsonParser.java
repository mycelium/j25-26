package json.parser;

import json.lexer.Token;
import java.util.List;

public class JsonParser {

    private final List<Token> tokens;
    private int pos;

    public JsonParser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public JsonNode parse() {
        return parseValue();
    }

    private JsonNode parsePrimitive(){
        Token token = consume();
        String val = token.getValue();
        switch (token.getType()) {
            case STRING:
                return new JsonPrimitive(val);
            case NUMBER:
                return new JsonPrimitive(Double.parseDouble(val));
            case BOOLEAN:
                return new JsonPrimitive(Boolean.parseBoolean(val));
            case NULL:
                return new JsonPrimitive(null);

        }

        throw new RuntimeException("Unexpected character:" + token.getValue());

    }

    private JsonNode parseValue() {
        Token current = peek();
        switch (current.getType()) {
            case STRING:

            case NUMBER:
            case BOOLEAN:
            case NULL:
                return parsePrimitive();

            case LBRACE:
                return parseObject();
            case LBRACKET:
                return parseArray();
            default:
                throw new RuntimeException("Unexpected token: " + current.getType());
        }
    }

    private JsonObject parseObject() {
        JsonObject jsonObject = new JsonObject();
        expect(Token.Type.LBRACE);

        if (peek().getType() == Token.Type.RBRACE){
            expect(Token.Type.RBRACE);
            return jsonObject;
        }
        while(true){
            String key = expect(Token.Type.STRING).getValue();
            expect(Token.Type.COLON);
            jsonObject.put(key,parseValue());
            if (peek().getType() == Token.Type.COMMA) {
                consume();
            } else if (peek().getType() == Token.Type.RBRACE) {
                expect(Token.Type.RBRACE);
                break;
            } else {
                throw new RuntimeException("Expected , or } in object but got " + peek().getType());
            }
        }
        return jsonObject;
    }

    private JsonArray parseArray() {
        JsonArray jsonArray = new JsonArray();
        expect(Token.Type.LBRACKET);

        if (peek().getType() == Token.Type.RBRACKET){
            expect(Token.Type.RBRACKET);
            return jsonArray;
        }
        while(true){
            jsonArray.add(parseValue());
            if (peek().getType() == Token.Type.COMMA) {
                consume();
            } else if (peek().getType() == Token.Type.RBRACKET) {
                expect(Token.Type.RBRACKET);
                break;
            } else {
                throw new RuntimeException("Expected , or ] in array but got " + peek().getType());
            }
        }
        return jsonArray;

    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token consume() {
        return tokens.get(pos++);
    }

    private Token expect(Token.Type type) {
        Token t = consume();
        if (t.getType() != type) {
            throw new RuntimeException("Expected " + type + " but got " + t.getType());
        }
        return t;
    }
}
