package jsontree;

import lexer.Lexeme;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;

public class JsonTree {
    private JsonNode root;
    private List<Lexeme> lexemes;
    private int pos;
    private int length;

    public JsonTree(List<Lexeme> lexemes) {
        this.lexemes = lexemes;
        this.pos = 0;
        length = lexemes.size();
        this.root = parseLexeme();
    }

    public JsonTree(Object object) {
        this.root = buildFromObject(object);
    }

    private JsonNode parseLexeme() {
        endCheck();
        Lexeme current = lexemes.get(pos);
        switch (current.getType()) {
            case STRING:
                pos++;
                return new BaseNode(current.getValue());
            case NUMBER:
                pos++;
                String numStr = current.getValue();
                if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                    return new BaseNode(Double.parseDouble(numStr));
                } else {
                    return new BaseNode(Long.parseLong(numStr));
                }
            case BOOL:
                pos++;
                return new BaseNode(Boolean.parseBoolean(current.getValue()));
            case NULL:
                pos++;
                return new BaseNode(null);
            case LeftBRACE:
                pos++;
                return parseObject();
            case LeftBRACKET:
                pos++;
                return parseArray();
            default:
                throw new RuntimeException("Unexpected token: " + current.getType());
        }
    }
    private ObjectNode parseObject() {
        endCheck();
        ObjectNode objectNode = new ObjectNode();
        if (lexemes.get(pos).getType() == Lexeme.LexemeType.RightBRACE) {
            pos++;
            return objectNode;
        }
        while (true) {
            endCheck();
            Lexeme keyLexeme = lexemes.get(pos);
            if (keyLexeme.getType() != Lexeme.LexemeType.STRING) {
                throw new RuntimeException("Invalid type of key");
            }
            pos++;
            String key = keyLexeme.getValue();
            endCheck();
            if (lexemes.get(pos).getType() != Lexeme.LexemeType.COLON) {
                throw new RuntimeException("Expected ':'");
            }
            pos++;
            JsonNode value = parseLexeme();
            objectNode.put(key, value);
            endCheck();
            Lexeme next = lexemes.get(pos);
            if (next.getType() == Lexeme.LexemeType.COMMA) {
                pos++;
            } else if (next.getType() == Lexeme.LexemeType.RightBRACE) {
                pos++;
                break;
            } else {
                throw new RuntimeException("Expected ',' or '}'");
            }
        }
        return objectNode;
    }
    private ArrayNode parseArray() {
        ArrayNode arrayNode = new ArrayNode();
        if (lexemes.get(pos).getType() == Lexeme.LexemeType.RightBRACKET) {
            pos++;
            return arrayNode;
        }
        while (true) {
            JsonNode element = parseLexeme();
            arrayNode.add(element);
            endCheck();
            Lexeme next = lexemes.get(pos);
            if (next.getType() == Lexeme.LexemeType.COMMA) {
                pos++;
            } else if (next.getType() == Lexeme.LexemeType.RightBRACKET) {
                pos++;
                break;
            } else {
                throw new RuntimeException("Expected ',' or ']'");
            }
        }

        return arrayNode;
    }

    private void endCheck() {
        if (pos >= length) {
            throw new RuntimeException("Unexpected end of input");
        }
    }

    private JsonNode buildFromObject(Object obj) {
        if (obj == null) {
            return new BaseNode(null);
        }
        Class<?> clazz = obj.getClass();
        if (isPrimitiveOrWrapper(clazz) || obj instanceof String) {
            return new BaseNode(obj);
        }
        if (clazz.isArray()) {
            return buildFromArray(obj);
        }
        if (obj instanceof Collection) {
            return buildFromCollection((Collection<?>) obj);
        }
        if (obj instanceof Map) {
            return buildFromMap((Map<?, ?>) obj);
        }
        return buildFromPojo(obj);
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class ||
                clazz == Byte.class ||
                clazz == Character.class ||
                clazz == Short.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Float.class ||
                clazz == Double.class;
    }
    private ArrayNode buildFromArray(Object array) {
        ArrayNode arrayNode = new ArrayNode();
        int length = java.lang.reflect.Array.getLength(array);

        for (int i = 0; i < length; i++) {
            Object element = java.lang.reflect.Array.get(array, i);
            arrayNode.add(buildFromObject(element));
        }
        return arrayNode;
    }
    private ArrayNode buildFromCollection(Collection<?> collection) {
        ArrayNode arrayNode = new ArrayNode();
        for (Object element : collection) {
            arrayNode.add(buildFromObject(element));
        }
        return arrayNode;
    }

    private ObjectNode buildFromMap(Map<?, ?> map) {
        ObjectNode objectNode = new ObjectNode();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            JsonNode value = buildFromObject(entry.getValue());
            objectNode.put(key, value);
        }
        return objectNode;
    }
    private ObjectNode buildFromPojo(Object obj) {
        ObjectNode objectNode = new ObjectNode();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                String fieldName = field.getName();
                JsonNode nodeValue = buildFromObject(value);
                objectNode.put(fieldName, nodeValue);
            } catch (IllegalAccessException e) {}
        }
        return objectNode;
    }
    public JsonNode getRoot() {
        return root;
    }
}
