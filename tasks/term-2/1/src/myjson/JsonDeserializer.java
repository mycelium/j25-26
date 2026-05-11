package myjson;

import java.util.*;

final class JsonDeserializer {

    private JsonDeserializer() {}

    static Object parse(String json) {
        return new InternalParser(json.trim()).parse();
    }

    @SuppressWarnings("unchecked")
    static <T> T parseToObject(String json, Class<T> clazz) {
        Object parsed = parse(json);
        if (clazz == Map.class || clazz == Object.class) {
            return (T) parsed;
        }
        if (parsed instanceof Map) {
            return TypeConverter.mapToObject((Map<String, Object>) parsed, clazz);
        }
        if (parsed instanceof List) {
            List<?> list = (List<?>) parsed;
            if (clazz.isArray()) {
                return TypeConverter.listToArray(list, clazz);
            }
            if (Collection.class.isAssignableFrom(clazz)) {
                Collection<Object> collection = TypeConverter.createCollection(clazz);
                collection.addAll(list);
                return (T) collection;
            }
        }
        throw new RuntimeException("Cannot map JSON to " + clazz.getName());
    }


    private static class InternalParser {
        private final String input;
        private int pos;

        InternalParser(String input) {
            this.input = input;
            this.pos = 0;
        }

        Object parse() {
            skipSpaces();
            if (pos >= input.length()) return null;
            var c = input.charAt(pos);
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't', 'f' -> parseBoolean();
                case 'n' -> parseNull();
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            var map = new LinkedHashMap<String, Object>();
            pos++;
            skipSpaces();
            if (pos < input.length() && input.charAt(pos) == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipSpaces();
                if (input.charAt(pos) != '"')
                    throw new RuntimeException("Expected string key at " + pos);
                var key = parseString();
                skipSpaces();
                if (input.charAt(pos) != ':')
                    throw new RuntimeException("Expected ':' at " + pos);
                pos++;
                skipSpaces();
                var value = parse();
                map.put(key, value);
                skipSpaces();
                if (pos >= input.length()) break;
                var ch = input.charAt(pos);
                if (ch == '}') {
                    pos++;
                    break;
                }
                if (ch == ',') {
                    pos++;
                } else {
                    throw new RuntimeException("Unexpected character in object: " + ch);
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            var list = new ArrayList<>();
            pos++;
            skipSpaces();
            if (pos < input.length() && input.charAt(pos) == ']') {
                pos++;
                return list;
            }
            while (true) {
                skipSpaces();
                list.add(parse());
                skipSpaces();
                if (pos >= input.length()) break;
                var ch = input.charAt(pos);
                if (ch == ']') {
                    pos++;
                    break;
                }
                if (ch == ',') {
                    pos++;
                }
            }
            return list;
        }

        private String parseString() {
            pos++;
            var sb = new StringBuilder();
            while (pos < input.length()) {
                var c = input.charAt(pos);
                if (c == '"') {
                    pos++;
                    return sb.toString();
                }
                if (c == '\\') {
                    pos++;
                    if (pos >= input.length())
                        throw new RuntimeException("Unexpected end after escape");
                    var escaped = input.charAt(pos);
                    var decoded = switch (escaped) {
                        case '"' -> '"';
                        case '\\' -> '\\';
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        case 'u' -> {
                            if (pos + 4 >= input.length())
                                throw new RuntimeException("Incomplete Unicode escape");
                            var hex = input.substring(pos + 1, pos + 5);
                            try {
                                var codePoint = Integer.parseInt(hex, 16);
                                pos += 4;
                                yield (char) codePoint;
                            } catch (NumberFormatException e) {
                                throw new RuntimeException("Invalid Unicode escape: \\u" + hex);
                            }
                        }
                        default -> escaped;
                    };
                    sb.append(decoded);
                    pos++;
                } else {
                    sb.append(c);
                    pos++;
                }
            }
            throw new RuntimeException("Unterminated string");
        }

        private Object parseBoolean() {
            if (input.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (input.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            throw new RuntimeException("Expected boolean at " + pos);
        }

        private Object parseNull() {
            if (input.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new RuntimeException("Expected null at " + pos);
        }

        
        private Number parseNumber() {
            var start = pos;
            while (pos < input.length() && !isDelimiter(input.charAt(pos))) {
                pos++;
            }
            var numStr = input.substring(start, pos);
            if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                return Double.parseDouble(numStr);
            } else {
                long longVal = Long.parseLong(numStr);
                if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                    return (int) longVal;
                }
                return longVal;
            }
        }

        private boolean isDelimiter(char c) {
            return c == ',' || c == '}' || c == ']' || Character.isWhitespace(c);
        }

        private void skipSpaces() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }
    }
}