package jsonparser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// парсер строит дерево объектов из токенов (рекурсивный спуск)
class JsonParser {

    private final JsonLexer lexer;

    JsonParser(String json) {
        this.lexer = new JsonLexer(json);
    }

    Object parse() {
        Object result = parseValue();
        if (lexer.type() != TokenType.EOF) {
            throw new JsonException("Лишние данные после конца JSON");
        }
        return result;
    }

    private Object parseValue() {
        return switch (lexer.type()) {
            case LBRACE   -> parseObject();
            case LBRACKET -> parseArray();
            case STRING   -> { String v = lexer.value(); lexer.next(); yield v; }
            case NUMBER   -> { Object n = parseNumber(lexer.value()); lexer.next(); yield n; }
            case BOOLEAN  -> { boolean b = "true".equals(lexer.value()); lexer.next(); yield b; }
            case NULL     -> { lexer.next(); yield null; }
            default -> throw new JsonException("Неожиданный токен: " + lexer.type());
        };
    }

    private Map<String, Object> parseObject() {
        lexer.next();
        Map<String, Object> map = new LinkedHashMap<>();

        if (lexer.type() == TokenType.RBRACE) {
            lexer.next();
            return map;
        }

        while (true) {
            if (lexer.type() != TokenType.STRING) {
                throw new JsonException("Ожидался ключ (строка), получен: " + lexer.type());
            }
            String key = lexer.value();
            lexer.next();

            if (lexer.type() != TokenType.COLON) {
                throw new JsonException("Ожидалось ':', получен: " + lexer.type());
            }
            lexer.next();

            map.put(key, parseValue());

            if (lexer.type() == TokenType.RBRACE) { lexer.next(); return map; }
            if (lexer.type() != TokenType.COMMA) {
                throw new JsonException("Ожидалась ',' или '}', получен: " + lexer.type());
            }
            lexer.next();
        }
    }

    private List<Object> parseArray() {
        lexer.next();
        List<Object> list = new ArrayList<>();

        if (lexer.type() == TokenType.RBRACKET) {
            lexer.next();
            return list;
        }

        while (true) {
            list.add(parseValue());

            if (lexer.type() == TokenType.RBRACKET) { lexer.next(); return list; }
            if (lexer.type() != TokenType.COMMA) {
                throw new JsonException("Ожидалась ',' или ']', получен: " + lexer.type());
            }
            lexer.next();
        }
    }

    private Object parseNumber(String s) {
        if (s.contains(".") || s.contains("e") || s.contains("E")) {
            return Double.parseDouble(s);
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return Long.parseLong(s);
        }
    }
}
