package com.jsonparser.core;

import com.jsonparser.exception.JsonException;
import java.lang.reflect.*;
import java.util.*;

public class Reader {
    private final List<Token> tokens;
    private int idx;

    public Reader(String json) {
        Lexer lexer = new Lexer(json);
        this.tokens = lexer.scan();
        this.idx = 0;
    }

    public Object read() {
        return readValue();
    }

    public Map<String, Object> readMap() {
        expect(Token.Type.OBJECT_START);
        return readObject();
    }

    public <T> T readObject(Class<T> clazz) {
        expect(Token.Type.OBJECT_START);

        try {
            Constructor<T> cons = clazz.getDeclaredConstructor();
            cons.setAccessible(true);
            T instance = cons.newInstance();
            Map<String, Field> fieldIndex = buildFieldIndex(clazz);

            while (idx < tokens.size()) {
                Token t = tokens.get(idx);
                if (t.kind() == Token.Type.OBJECT_END) {
                    idx++;
                    break;
                }

                expect(Token.Type.TEXT);
                String fieldName = tokens.get(idx - 1).asText();
                expect(Token.Type.COLON);

                Field f = fieldIndex.get(fieldName);
                if (f == null) {
                    readValue();
                } else {
                    Object val = readValueForField(f);
                    f.setAccessible(true);
                    f.set(instance, val);
                }

                if (idx < tokens.size() && tokens.get(idx).kind() == Token.Type.COMMA) {
                    idx++;
                }
            }
            return instance;
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException("Failed to create object: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> readObject() {
        Map<String, Object> result = new LinkedHashMap<>();

        while (idx < tokens.size()) {
            Token t = tokens.get(idx);
            if (t.kind() == Token.Type.OBJECT_END) {
                idx++;
                break;
            }

            expect(Token.Type.TEXT);
            String key = tokens.get(idx - 1).asText();
            expect(Token.Type.COLON);

            Object val = readValue();
            result.put(key, val);

            if (idx < tokens.size() && tokens.get(idx).kind() == Token.Type.COMMA) {
                idx++;
            }
        }
        return result;
    }

    private Object readValue() {
        if (idx >= tokens.size()) {
            throw new JsonException("Unexpected end");
        }

        Token t = tokens.get(idx);
        idx++;

        return switch (t.kind()) {
            case OBJECT_START -> readObject();
            case ARRAY_START -> readArray();
            case TEXT -> t.asText();
            case NUM -> parseNumber(t.asNumber());
            case BOOL_TRUE -> true;
            case BOOL_FALSE -> false;
            case EMPTY -> null;
            default -> throw new JsonException("Unexpected token: " + t.kind());
        };
    }

    private Object parseNumber(String raw) {
        if (raw.contains(".") || raw.contains("e") || raw.contains("E")) {
            return Double.parseDouble(raw);
        }
        try {
            long val = Long.parseLong(raw);
            if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                return (int) val;
            }
            return val;
        } catch (NumberFormatException e) {
            return Double.parseDouble(raw);
        }
    }

    private List<Object> readArray() {
        return readArrayWithType(null);
    }

    private List<Object> readArrayWithType(Class<?> elemType) {
        List<Object> list = new ArrayList<>();

        while (idx < tokens.size()) {
            Token t = tokens.get(idx);
            if (t.kind() == Token.Type.ARRAY_END) {
                idx++;
                break;
            }

            Object element;
            if (elemType != null && t.kind() == Token.Type.OBJECT_START && !Map.class.isAssignableFrom(elemType)) {
                element = readObject(elemType);
            } else {
                Object raw = readValue();
                element = (elemType != null) ? convert(raw, elemType) : raw;
            }
            list.add(element);

            if (idx < tokens.size() && tokens.get(idx).kind() == Token.Type.COMMA) {
                idx++;
            }
        }
        return list;
    }

    private Object readValueForField(Field f) {
        Class<?> type = f.getType();

        if (type.isArray()) {
            expect(Token.Type.ARRAY_START);
            Class<?> comp = type.getComponentType();
            List<Object> items = readArrayWithType(comp);
            Object arr = Array.newInstance(comp, items.size());
            for (int i = 0; i < items.size(); i++) {
                Array.set(arr, i, items.get(i));
            }
            return arr;
        }

        if (Collection.class.isAssignableFrom(type)) {
            expect(Token.Type.ARRAY_START);
            Class<?> elemType = extractElementType(f);
            List<Object> items = readArrayWithType(elemType);

            Collection<Object> coll;
            if (type.isInterface()) {
                coll = List.class.isAssignableFrom(type) ? new ArrayList<>() : new LinkedHashSet<>();
            } else {
                try {
                    coll = (Collection<Object>) type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    coll = new ArrayList<>();
                }
            }
            coll.addAll(items);
            return coll;
        }

        if (idx < tokens.size() && tokens.get(idx).kind() == Token.Type.OBJECT_START) {
            return readObject(type);
        }

        return convert(readValue(), type);
    }

    private Class<?> extractElementType(Field f) {
        Type generic = f.getGenericType();
        if (generic instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length > 0 && args[0] instanceof Class<?> c) {
                return c;
            }
        }
        return null;
    }

    private Object convert(Object raw, Class<?> target) {
        if (raw == null || target == null || target.isInstance(raw)) {
            return raw;
        }

        return switch (target.getName()) {
            case "int", "java.lang.Integer" -> raw instanceof Number n ? n.intValue() : Integer.parseInt(raw.toString());
            case "long", "java.lang.Long" -> raw instanceof Number n ? n.longValue() : Long.parseLong(raw.toString());
            case "double", "java.lang.Double" -> raw instanceof Number n ? n.doubleValue() : Double.parseDouble(raw.toString());
            case "float", "java.lang.Float" -> raw instanceof Number n ? n.floatValue() : Float.parseFloat(raw.toString());
            case "short", "java.lang.Short" -> raw instanceof Number n ? n.shortValue() : Short.parseShort(raw.toString());
            case "byte", "java.lang.Byte" -> raw instanceof Number n ? n.byteValue() : Byte.parseByte(raw.toString());
            case "boolean", "java.lang.Boolean" -> raw instanceof Boolean b ? b : Boolean.parseBoolean(raw.toString());
            case "char", "java.lang.Character" -> {
                String s = raw.toString();
                yield s.isEmpty() ? '\0' : s.charAt(0);
            }
            case "java.lang.String" -> raw.toString();
            default -> raw;
        };
    }

    private Map<String, Field> buildFieldIndex(Class<?> clazz) {
        Map<String, Field> map = new HashMap<>();
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && !f.isSynthetic()) {
                map.put(f.getName(), f);
            }
        }
        return map;
    }

    private void expect(Token.Type expected) {
        if (idx >= tokens.size()) {
            throw new JsonException("Expected " + expected + " but got end");
        }
        if (tokens.get(idx).kind() != expected) {
            throw new JsonException("Expected " + expected + " but got " + tokens.get(idx).kind());
        }
        idx++;
    }
}