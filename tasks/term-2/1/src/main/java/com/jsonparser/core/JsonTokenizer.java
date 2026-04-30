package com.jsonparser.core;

import com.jsonparser.exception.JsonParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Разбиение JSON строки на токены
 */
public class JsonTokenizer {
    private final String json;
    private int position;
    private final List<JsonToken> tokens;

    public JsonTokenizer(String json) {
        this.json = json;
        this.position = 0;
        this.tokens = new ArrayList<>();
    }

    // Токенизация JSON строки
    // объекты, массивы, строки, числа, boolean, null
    public List<JsonToken> tokenize() {
        tokens.clear();
        position = 0;

        while (position < json.length()) {
            char current = json.charAt(position);

            // Пропуск пробельных символов
            if (Character.isWhitespace(current)) {
                position++;
                continue;
            }

            switch (current) {
                case '{':
                    tokens.add(new JsonToken(JsonToken.TokenType.BEGIN_OBJECT));
                    position++;
                    break;
                case '}':
                    tokens.add(new JsonToken(JsonToken.TokenType.END_OBJECT));
                    position++;
                    break;
                case '[':
                    tokens.add(new JsonToken(JsonToken.TokenType.BEGIN_ARRAY));
                    position++;
                    break;
                case ']':
                    tokens.add(new JsonToken(JsonToken.TokenType.END_ARRAY));
                    position++;
                    break;
                case ':':
                    tokens.add(new JsonToken(JsonToken.TokenType.COLON));
                    position++;
                    break;
                case ',':
                    tokens.add(new JsonToken(JsonToken.TokenType.COMMA));
                    position++;
                    break;
                case '"':
                    parseString();
                    break;
                default:
                    if (Character.isDigit(current) || current == '-') {
                        parseNumber();
                    } else if (Character.isLetter(current)) {
                        parseKeyword();
                    } else {
                        throw new JsonParseException("Неожиданный символ: " + current + " на позиции " + position);
                    }
                    break;
            }
        }

        return tokens;
    }

    // Парсинг строкового значения JSON
    private void parseString() {
        position++; // Пропускаем открывающую кавычку
        StringBuilder sb = new StringBuilder();

        while (position < json.length()) {
            char current = json.charAt(position);

            if (current == '"') {
                position++; // Пропускаем закрывающую кавычку
                tokens.add(new JsonToken(JsonToken.TokenType.STRING, sb.toString()));
                return;
            }

            // Обработка escape-последовательностей
            if (current == '\\' && position + 1 < json.length()) {
                char next = json.charAt(position + 1);
                switch (next) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append('\\').append(next);
                }
                position += 2;
            } else {
                sb.append(current);
                position++;
            }
        }

        throw new JsonParseException("Незакрытая строка");
    }

    // Парсинг числового значения JSON
    private void parseNumber() {
        int start = position;

        // Обработка отрицательных чисел
        if (json.charAt(position) == '-') {
            position++;
        }

        // Целая часть
        while (position < json.length() && Character.isDigit(json.charAt(position))) {
            position++;
        }

        // Дробная часть
        if (position < json.length() && json.charAt(position) == '.') {
            position++;
            while (position < json.length() && Character.isDigit(json.charAt(position))) {
                position++;
            }
        }

        // Экспоненциальная часть
        if (position < json.length() && (json.charAt(position) == 'e' || json.charAt(position) == 'E')) {
            position++;
            if (position < json.length() && (json.charAt(position) == '+' || json.charAt(position) == '-')) {
                position++;
            }
            while (position < json.length() && Character.isDigit(json.charAt(position))) {
                position++;
            }
        }

        String numberStr = json.substring(start, position);
        tokens.add(new JsonToken(JsonToken.TokenType.NUMBER, numberStr));
    }

    // Парсинг ключевых слов: true, false, null
    private void parseKeyword() {
        int start = position;

        while (position < json.length() && Character.isLetter(json.charAt(position))) {
            position++;
        }

        String keyword = json.substring(start, position);

        switch (keyword) {
            case "true":
                tokens.add(new JsonToken(JsonToken.TokenType.TRUE, true));
                break;
            case "false":
                tokens.add(new JsonToken(JsonToken.TokenType.FALSE, false));
                break;
            case "null":
                tokens.add(new JsonToken(JsonToken.TokenType.NULL, null));
                break;
            default:
                throw new JsonParseException("Неизвестный ключевой токен: " + keyword);
        }
    }
}