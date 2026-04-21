package com.jsonparser.core;

import com.jsonparser.exception.JsonParseException;
import java.lang.reflect.*;
import java.util.*;

/**
 * Преобразование JSON токенов в Java объекты
 */
public class JsonDeserializer {
    private final List<JsonToken> tokens;
    private int position;

    public JsonDeserializer(String json) {
        JsonTokenizer tokenizer = new JsonTokenizer(json);
        this.tokens = tokenizer.tokenize();
        this.position = 0;
    }

    /**
     * Парсинг JSON в Map<String, Object>
     */
    public Map<String, Object> parseAsMap() {
        expect(JsonToken.TokenType.BEGIN_OBJECT);
        Map<String, Object> map = new LinkedHashMap<>();

        while (position < tokens.size()) {
            JsonToken token = tokens.get(position);

            if (token.getType() == JsonToken.TokenType.END_OBJECT) {
                position++;
                break;
            }

            // Парсим ключ
            expect(JsonToken.TokenType.STRING);
            String key = tokens.get(position - 1).getStringValue();

            expect(JsonToken.TokenType.COLON);

            // Парсим значение
            Object value = parseValue();
            map.put(key, value);

            // Проверяем запятую или конец объекта
            if (position < tokens.size() && tokens.get(position).getType() == JsonToken.TokenType.COMMA) {
                position++;
            }
        }

        return map;
    }

    /**
     * Парсинг JSON в объект указанного класса
     */
    public <T> T parseAsObject(Class<T> clazz) {
        expect(JsonToken.TokenType.BEGIN_OBJECT);

        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Map<String, Field> fieldMap = getFieldMap(clazz);

            while (position < tokens.size()) {
                JsonToken token = tokens.get(position);

                if (token.getType() == JsonToken.TokenType.END_OBJECT) {
                    position++;
                    break;
                }

                // Получаем имя поля из JSON
                expect(JsonToken.TokenType.STRING);
                String fieldName = tokens.get(position - 1).getStringValue();

                expect(JsonToken.TokenType.COLON);

                // Находим поле в классе
                Field field = fieldMap.get(fieldName);
                if (field == null) {
                    // Если поле не найдено, пропускаем значение
                    parseValue();
                } else {
                    // Парсим значение и устанавливаем в поле
                    Object value = parseValueForField(field);
                    field.setAccessible(true);
                    field.set(instance, value);
                }

                // Проверяем запятую
                if (position < tokens.size() && tokens.get(position).getType() == JsonToken.TokenType.COMMA) {
                    position++;
                }
            }

            return instance;
        } catch (Exception e) {
            throw new JsonParseException("Ошибка создания объекта: " + e.getMessage(), e);
        }
    }

    /**
     * Парсинг JSON значение (поддерживает все типы)
     */
    private Object parseValue() {
        if (position >= tokens.size()) {
            throw new JsonParseException("Неожиданный конец JSON");
        }

        JsonToken token = tokens.get(position);
        position++;

        switch (token.getType()) {
            case BEGIN_OBJECT:
                // Рекурсивно парсим вложенный объект
                return parseAsMap();
            case BEGIN_ARRAY:
                return parseArray();
            case STRING:
                return token.getStringValue();
            case NUMBER:
                String numStr = token.getNumberValue();
                if (numStr.contains(".")) {
                    return Double.parseDouble(numStr);
                } else {
                    // Пытаемся определить Integer или Long
                    long longVal = Long.parseLong(numStr);
                    if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                        return (int) longVal;
                    }
                    return longVal;
                }
            case TRUE:
                return true;
            case FALSE:
                return false;
            case NULL:
                return null;
            default:
                throw new JsonParseException("Неожиданный токен: " + token.getType());
        }
    }

    /**
     * Парсинг JSON массив в Java список
     */
    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();

        while (position < tokens.size()) {
            JsonToken token = tokens.get(position);

            if (token.getType() == JsonToken.TokenType.END_ARRAY) {
                position++;
                break;
            }

            // Парсим элемент массива
            list.add(parseValue());

            // Проверяем запятую
            if (position < tokens.size() && tokens.get(position).getType() == JsonToken.TokenType.COMMA) {
                position++;
            }
        }

        return list;
    }

    /**
     * Парсинг значения для конкретного поля с учетом его типа
     * Поддерживает: примитивы, обертки, строки, массивы, коллекции, вложенные объекты
     */
    private Object parseValueForField(Field field) {
        Class<?> fieldType = field.getType();

        // Проверяем, является ли поле массивом
        if (fieldType.isArray()) {
            expect(JsonToken.TokenType.BEGIN_ARRAY);
            List<Object> list = parseArray();
            Class<?> componentType = fieldType.getComponentType();
            Object array = Array.newInstance(componentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, convertValue(list.get(i), componentType));
            }
            return array;
        }

        // Проверяем, является ли поле коллекцией
        if (Collection.class.isAssignableFrom(fieldType)) {
            expect(JsonToken.TokenType.BEGIN_ARRAY);
            return parseArray();
        }

        // Для вложенных объектов
        if (position < tokens.size() && tokens.get(position).getType() == JsonToken.TokenType.BEGIN_OBJECT) {
            return parseAsObject(fieldType);
        }

        // Для простых типов
        Object rawValue = parseValue();
        return convertValue(rawValue, fieldType);
    }

    /**
     * Конвертация значения в нужный тип
     * Поддерживает примитивы и их обертки
     */
    private Object convertValue(Object rawValue, Class<?> targetType) {
        if (rawValue == null) {
            return null;
        }

        // Примитивные типы и их обертки
        if (targetType == int.class || targetType == Integer.class) {
            if (rawValue instanceof Number) {
                return ((Number) rawValue).intValue();
            }
            return Integer.parseInt(rawValue.toString());
        }

        if (targetType == long.class || targetType == Long.class) {
            if (rawValue instanceof Number) {
                return ((Number) rawValue).longValue();
            }
            return Long.parseLong(rawValue.toString());
        }

        if (targetType == double.class || targetType == Double.class) {
            if (rawValue instanceof Number) {
                return ((Number) rawValue).doubleValue();
            }
            return Double.parseDouble(rawValue.toString());
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            if (rawValue instanceof Boolean) {
                return rawValue;
            }
            return Boolean.parseBoolean(rawValue.toString());
        }

        if (targetType == String.class) {
            return rawValue.toString();
        }

        return rawValue;
    }

    /**
     * Возвращение мапы полей класса по имени
     */
    private Map<String, Field> getFieldMap(Class<?> clazz) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }

    /**
     * Ожидание определенного типа токена
     */
    private void expect(JsonToken.TokenType expectedType) {
        if (position >= tokens.size()) {
            throw new JsonParseException("Ожидался токен " + expectedType + ", но достигнут конец");
        }

        JsonToken token = tokens.get(position);
        if (token.getType() != expectedType) {
            throw new JsonParseException("Ожидался токен " + expectedType + ", получен " + token.getType());
        }

        position++;
    }
}