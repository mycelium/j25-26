package jsonlib.parser;

import jsonlib.JsonParseException;
import jsonlib.node.*;

public class JsonParser {
    private final JsonTokenizer tokenizer;
    private Token current;

    public JsonParser(JsonTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.current = tokenizer.nextToken();
    }

    private void consume(TokenType type) {
        if (current.type == type) {
            current = tokenizer.nextToken();
        } else {
            throw new JsonParseException("Expected " + type + " but found " + current.type + " (" + current.value + ")");
        }
    }

    public JsonNode parse() {
        JsonNode node = parseValue();
        if (current.type != TokenType.EOF) {
            throw new JsonParseException("Unexpected token after value: " + current.type);
        }
        return node;
    }

    private JsonNode parseValue() {
        switch (current.type) {
            case LBRACE: return parseObject();
            case LBRACKET: return parseArray();
            case STRING: return parseString();
            case NUMBER: return parseNumber();
            case TRUE: return parseTrue();
            case FALSE: return parseFalse();
            case NULL: return parseNull();
            default:
                throw new JsonParseException("Unexpected token: " + current.type);
        }
    }

    private JsonObject parseObject() {
        consume(TokenType.LBRACE);
        JsonObject obj = new JsonObject();
        if (current.type == TokenType.RBRACE) {
            consume(TokenType.RBRACE);
            return obj;
        }
        while (true) {
            if (current.type != TokenType.STRING) {
                throw new JsonParseException("Expected string key in object, got " + current.type);
            }
            String key = current.value;
            consume(TokenType.STRING);
            consume(TokenType.COLON);
            obj.put(key, parseValue());
            if (current.type == TokenType.COMMA) {
                consume(TokenType.COMMA);
            } else {
                break;
            }
        }
        consume(TokenType.RBRACE);
        return obj;
    }

    private JsonArray parseArray() {
        consume(TokenType.LBRACKET);
        JsonArray arr = new JsonArray();
        if (current.type == TokenType.RBRACKET) {
            consume(TokenType.RBRACKET);
            return arr;
        }
        while (true) {
            arr.add(parseValue());
            if (current.type == TokenType.COMMA) {
                consume(TokenType.COMMA);
            } else {
                break;
            }
        }
        consume(TokenType.RBRACKET);
        return arr;
    }

    private JsonString parseString() {
        String val = current.value;
        consume(TokenType.STRING);
        return new JsonString(val);
    }

    private JsonNumber parseNumber() {
        String val = current.value;
        consume(TokenType.NUMBER);
        return new JsonNumber(val);
    }

    private JsonBoolean parseTrue() {
        consume(TokenType.TRUE);
        return JsonBoolean.TRUE;
    }

    private JsonBoolean parseFalse() {
        consume(TokenType.FALSE);
        return JsonBoolean.FALSE;
    }

    private JsonNull parseNull() {
        consume(TokenType.NULL);
        return JsonNull.INSTANCE;
    }
}