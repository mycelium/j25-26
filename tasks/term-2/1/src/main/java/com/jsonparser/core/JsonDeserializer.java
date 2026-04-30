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

    // Парсинг JSON в Java объект
    public Object parse() {
        return parseValue();
    }

    // Парсинг JSON в Map<String, Object>
    public Map<String, Object> parseAsMap() {
        expect(JsonToken.TokenType.BEGIN_OBJECT);
        return parseObjectContents();
    }

    // Чтение содержимого JSON-объекта
    private Map<String, Object> parseObjectContents() {
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

    // Парсинг JSON в объект указанного класса
    public <T> T parseAsObject(Class<T> clazz) {
        expect(JsonToken.TokenType.BEGIN_OBJECT);

        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            T instance = ctor.newInstance();
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
        } catch (JsonParseException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonParseException("Ошибка создания объекта: " + e.getMessage(), e);
        }
    }

    // Парсинг JSON значения (поддерживает все типы)
    private Object parseValue() {
        if (position >= tokens.size()) {
            throw new JsonParseException("Неожиданный конец JSON");
        }

        JsonToken token = tokens.get(position);
        position++;

        switch (token.getType()) {
            case BEGIN_OBJECT:
                return parseObjectContents();
            case BEGIN_ARRAY:
                return parseArray();
            case STRING:
                return token.getStringValue();
            case NUMBER:
                return parseNumber(token.getNumberValue());
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

    // Преобразование строки числа в Integer / Long / Double
    private Object parseNumber(String numStr) {
        if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
            return Double.parseDouble(numStr);
        }
        try {
            long longVal = Long.parseLong(numStr);
            if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                return (int) longVal;
            }
            return longVal;
        } catch (NumberFormatException e) {
            return Double.parseDouble(numStr);
        }
    }

    // Парсинг JSON массива в Java список без типизации элементов
    private List<Object> parseArray() {
        return parseArrayWithType(null);
    }

    // Парсинг JSON массива с приведением элементов к указанному классу
    private List<Object> parseArrayWithType(Class<?> elementType) {
        List<Object> list = new ArrayList<>();

        while (position < tokens.size()) {
            JsonToken token = tokens.get(position);

            if (token.getType() == JsonToken.TokenType.END_ARRAY) {
                position++;
                break;
            }

            Object element;
            if (elementType != null
                    && token.getType() == JsonToken.TokenType.BEGIN_OBJECT
                    && !Map.class.isAssignableFrom(elementType)
                    && elementType != Object.class) {
                element = parseAsObject(elementType);
            } else {
                Object raw = parseValue();
                element = (elementType != null) ? convertValue(raw, elementType) : raw;
            }
            list.add(element);

            // Проверяем запятую
            if (position < tokens.size() && tokens.get(position).getType() == JsonToken.TokenType.COMMA) {
                position++;
            }
        }

        return list;
    }

    // Парсинг значения для конкретного поля с учетом его типа
    private Object parseValueForField(Field field) {
        Class<?> fieldType = field.getType();

        // Массив
        if (fieldType.isArray()) {
            expect(JsonToken.TokenType.BEGIN_ARRAY);
            Class<?> componentType = fieldType.getComponentType();
            List<Object> list = parseArrayWithType(componentType);
            Object array = Array.newInstance(componentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        }

        // Коллекция
        if (Collection.class.isAssignableFrom(fieldType)) {
            expect(JsonToken.TokenType.BEGIN_ARRAY);
            Class<?> elementType = getCollectionElementType(field);
            return parseArrayWithType(elementType);
        }

        // Вложенный объект
        if (position < tokens.size() && tokens.get(position).getType() == JsonToken.TokenType.BEGIN_OBJECT) {
            return parseAsObject(fieldType);
        }

        // Простые типы
        Object rawValue = parseValue();
        return convertValue(rawValue, fieldType);
    }

    // Извлекает класс элементов коллекции из generic-параметра поля
    private Class<?> getCollectionElementType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
            if (args.length > 0 && args[0] instanceof Class) {
                return (Class<?>) args[0];
            }
        }
        return null;
    }

    // Конвертация значения в нужный тип
    private Object convertValue(Object rawValue, Class<?> targetType) {
        if (rawValue == null || targetType == null) {
            return rawValue;
        }

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

        if (targetType == float.class || targetType == Float.class) {
            if (rawValue instanceof Number) {
                return ((Number) rawValue).floatValue();
            }
            return Float.parseFloat(rawValue.toString());
        }

        if (targetType == short.class || targetType == Short.class) {
            if (rawValue instanceof Number) {
                return ((Number) rawValue).shortValue();
            }
            return Short.parseShort(rawValue.toString());
        }

        if (targetType == byte.class || targetType == Byte.class) {
            if (rawValue instanceof Number) {
                return ((Number) rawValue).byteValue();
            }
            return Byte.parseByte(rawValue.toString());
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            if (rawValue instanceof Boolean) {
                return rawValue;
            }
            return Boolean.parseBoolean(rawValue.toString());
        }

        if (targetType == char.class || targetType == Character.class) {
            String s = rawValue.toString();
            return s.isEmpty() ? '\0' : s.charAt(0);
        }

        if (targetType == String.class) {
            return rawValue.toString();
        }

        return rawValue;
    }

    // Возвращение мапы полей класса по имени
    private Map<String, Field> getFieldMap(Class<?> clazz) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }

    // Ожидание определенного типа токена
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