package ru.derikey.json;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main API entry point. Use {@link #builder()} to create a configured instance.
 */
public final class JsonMapper {
    private final FieldNamingStrategy fieldNamingStrategy;
    private final boolean ignoreUnknownFields;
    private final Map<Type, TypeAdapter<?>> adapters;

    private JsonMapper(Builder builder) {
        this.fieldNamingStrategy = builder.fieldNamingStrategy;
        this.ignoreUnknownFields = builder.ignoreUnknownFields;
        this.adapters = Map.copyOf(builder.adapters);
    }

    public static Builder builder() {
        return new Builder();
    }

    // ------------------------------------------------------------------------
    // Parsing (JSON -> intermediate Map/List)
    // ------------------------------------------------------------------------

    public Map<String, Object> parseToMap(String json) {
        Object parsed = parse(json);
        if (parsed instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) parsed;
            return result;
        }
        throw new JsonException("JSON is not an object");
    }

    public List<Object> parseToList(String json) {
        Object parsed = parse(json);
        if (parsed instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> result = (List<Object>) parsed;
            return result;
        }
        throw new JsonException("JSON is not an array");
    }

    public Object parse(String json) {
            return new JsonParser(json).parse();
    }

    // ------------------------------------------------------------------------
    // Deserialization (intermediate -> Java object)
    // ------------------------------------------------------------------------

    public <T> T fromJson(String json, Class<T> clazz) {
        return fromJson(json, (Type) clazz);
    }

    public <T> T fromJson(String json, TypeReference<T> typeRef) {
        return fromJson(json, typeRef.getType());
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Type type) {
        Object parsed = parse(json);
        return (T) convert(parsed, type);
    }

    // ------------------------------------------------------------------------
    // Serialization (Java object -> JSON)
    // ------------------------------------------------------------------------

    public String toJson(Object object) {
        return toJson(object, object != null ? object.getClass() : Object.class);
    }

    public String toJson(Object object, Type type) {
        JsonWriter writer = new JsonWriter();
        serialize(object, type, writer);
        return writer.toString();
    }


    private Object convert(Object value, Type targetType) {
        if (value == null) return null;

        // Direct match? No conversion needed for simple types
        if (targetType == String.class) return value.toString();
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) return value;
            throw new JsonException("Cannot convert " + value + " to boolean");
        }
        if (isNumberType(targetType)) {
            return convertToNumber(value, targetType);
        }
        if (targetType == Character.class || targetType == char.class) {
            String s = value.toString();
            if (s.length() == 1) return s.charAt(0);
            throw new JsonException("Cannot convert '" + s + "' to char");
        }

        // Use custom adapter if registered
        TypeAdapter<?> adapter = adapters.get(targetType);
        if (adapter != null) {
            try {
                return adapter.read(value, targetType);
            } catch (Exception e) {
                throw new JsonException("Custom adapter failed for " + targetType, e);
            }
        }

        // Handle parameterized types
        if (targetType instanceof ParameterizedType pType) {
            Type rawType = pType.getRawType();
            if (rawType instanceof Class<?> rawClass) {
                if (Collection.class.isAssignableFrom(rawClass)) {
                    return convertToCollection(value, pType);
                } else if (Map.class.isAssignableFrom(rawClass)) {
                    return convertToMap(value, pType);
                }
            }
        }

        // Handle raw collections / arrays
        if (targetType instanceof Class<?> clazz) {
            if (clazz.isArray()) {
                return convertToArray(value, clazz.getComponentType());
            }
            if (Collection.class.isAssignableFrom(clazz)) {
                return convertToCollection(value, clazz);
            }
            if (Map.class.isAssignableFrom(clazz)) {
                return convertToMap(value, clazz);
            }
            // Plain class: treat as object
            return convertToObject(value, clazz);
        }

        throw new JsonException("Unsupported type: " + targetType);
    }

    private Object convertToNumber(Object value, Type target) {
        Class<?> targetClass = (Class<?>) target; // Number types are always Class
        if (!(value instanceof Number num)) {
            throw new JsonException("Expected number, got " + value.getClass());
        }
        if (targetClass == byte.class || targetClass == Byte.class) return num.byteValue();
        if (targetClass == short.class || targetClass == Short.class) return num.shortValue();
        if (targetClass == int.class || targetClass == Integer.class) return num.intValue();
        if (targetClass == long.class || targetClass == Long.class) return num.longValue();
        if (targetClass == float.class || targetClass == Float.class) return num.floatValue();
        if (targetClass == double.class || targetClass == Double.class) return num.doubleValue();
        throw new JsonException("Unknown number type: " + targetClass);
    }

    private boolean isNumberType(Type type) {
        if (!(type instanceof Class<?> clazz)) return false;
        return clazz.isPrimitive() && clazz != boolean.class && clazz != char.class
                || Number.class.isAssignableFrom(clazz);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Collection<?> convertToCollection(Object value, Type type) {
        if (!(value instanceof List<?> list)) {
            throw new JsonException("Expected JSON array, got " + value.getClass());
        }
        Collection collection;
        if (type instanceof Class) {
            // Raw type – ArrayList
            collection = new ArrayList();
        } else if (type instanceof ParameterizedType pType) {
            Class<?> rawClass = (Class<?>) pType.getRawType();
            Type elementType = pType.getActualTypeArguments()[0];
            collection = createCollection(rawClass);
            for (Object item : list) {
                collection.add(convert(item, elementType));
            }
            return collection;
        } else {
            throw new JsonException("Unsupported collection type: " + type);
        }
        // Raw type case: treat elements as Object
        collection.addAll(list);
        return collection;
    }

    private Collection<?> createCollection(Class<?> rawClass) {
        if (rawClass.isInterface()) {
            if (rawClass == List.class) return new ArrayList<>();
            if (rawClass == Set.class) return new LinkedHashSet<>();
            // fallback
        }
        try {
            return (Collection<?>) rawClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // Fallback to ArrayList
            return new ArrayList<>();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<?, ?> convertToMap(Object value, Type type) {
        if (!(value instanceof Map)) {
            throw new JsonException("Expected JSON object, got " + value.getClass());
        }
        Map<String, ?> map = (Map<String, ?>) value;
        Map result;
        Type keyType = null, valueType = null;
        if (type instanceof ParameterizedType pType) {
            Type[] args = pType.getActualTypeArguments();
            keyType = args[0];
            valueType = args[1];
            Class<?> rawClass = (Class<?>) pType.getRawType();
            result = createMap(rawClass);
        } else {
            result = new LinkedHashMap(); // raw map
        }
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            Object convertedKey = keyType == null ? entry.getKey() : convert(entry.getKey(), keyType);
            Object convertedValue = valueType == null ? entry.getValue() : convert(entry.getValue(), valueType);
            result.put(convertedKey, convertedValue);
        }
        return result;
    }

    private Map<?, ?> createMap(Class<?> rawClass) {
        if (rawClass.isInterface()) {
            if (rawClass == Map.class) return new LinkedHashMap<>();
        }
        try {
            return (Map<?, ?>) rawClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private Object convertToArray(Object value, Class<?> componentType) {
        if (!(value instanceof List<?> list)) {
            throw new JsonException("Expected JSON array, got " + value.getClass());
        }
        Object array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, convert(list.get(i), componentType));
        }
        return array;
    }

    private <T> T convertToObject(Object value, Class<T> clazz) {
        if (!(value instanceof Map)) {
            throw new JsonException("Expected JSON object, got " + value.getClass());
        }
        @SuppressWarnings("unchecked")
        Map<String, ?> map = (Map<String, ?>) value;
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            ClassInfo info = ClassInfo.get(clazz);
            for (FieldInfo field : info.fields) {
                String jsonName = fieldNamingStrategy.translateName(field.name);
                Object fieldValue = map.get(jsonName);
                if (fieldValue == null && !map.containsKey(jsonName)) {
                    if (ignoreUnknownFields) continue;
                    throw new JsonException("Missing field '" + jsonName + "' in JSON");
                }
                Object converted = convert(fieldValue, field.genericType);
                field.set(instance, converted);
            }
            return instance;
        } catch (Exception e) {
            throw new JsonException("Cannot instantiate " + clazz, e);
        }
    }


    private void serialize(Object object, Type type, JsonWriter writer) {
        if (object == null) {
            writer.writeNull();
            return;
        }

        // Custom adapter
        TypeAdapter<?> adapter = adapters.get(type != null ? type : object.getClass());
        if (adapter != null) {
            @SuppressWarnings("unchecked")
            TypeAdapter<Object> objAdapter = (TypeAdapter<Object>) adapter;
            try {
                objAdapter.write(object, writer);
                return;
            } catch (Exception e) {
                throw new JsonException("Custom adapter failed", e);
            }
        }

        Class<?> clazz = object.getClass();
        if (clazz == String.class) {
            writer.writeString((String) object);
        } else if (clazz == Boolean.class) {
            writer.writeBoolean((Boolean) object);
        } else if (Number.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
            writer.writeNumber(object.toString());
        } else if (clazz.isArray()) {
            writer.writeArrayStart();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                if (i > 0) writer.writeSeparator();
                serialize(Array.get(object, i), null, writer);
            }
            writer.writeArrayEnd();
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection<?> collection = (Collection<?>) object;
            writer.writeArrayStart();
            boolean first = true;
            for (Object item : collection) {
                if (!first) writer.writeSeparator();
                serialize(item, null, writer);
                first = false;
            }
            writer.writeArrayEnd();
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map<?, ?> map = (Map<?, ?>) object;
            writer.writeObjectStart();
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) writer.writeSeparator();
                String key = entry.getKey().toString(); // JSON keys must be strings
                writer.writeString(key);
                writer.writeNameSeparator();
                serialize(entry.getValue(), null, writer);
                first = false;
            }
            writer.writeObjectEnd();
        } else {
            // Regular Java object
            writer.writeObjectStart();
            boolean first = true;
            ClassInfo info = ClassInfo.get(clazz);
            for (FieldInfo field : info.fields) {
                Object value = field.get(object);
                String jsonName = fieldNamingStrategy.translateName(field.name);
                if (!first) writer.writeSeparator();
                writer.writeString(jsonName);
                writer.writeNameSeparator();
                serialize(value, field.genericType, writer);
                first = false;
            }
            writer.writeObjectEnd();
        }
    }


    public static class Builder {
        private FieldNamingStrategy fieldNamingStrategy = FieldNamingStrategies.IDENTITY;
        private boolean ignoreUnknownFields = false;
        private final Map<Type, TypeAdapter<?>> adapters = new HashMap<>();

        public Builder fieldNamingStrategy(FieldNamingStrategy strategy) {
            this.fieldNamingStrategy = Objects.requireNonNull(strategy);
            return this;
        }

        public Builder ignoreUnknownFields(boolean ignore) {
            this.ignoreUnknownFields = ignore;
            return this;
        }

        public <T> Builder registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
            adapters.put(type, adapter);
            return this;
        }

        public <T> Builder registerTypeAdapter(Type type, TypeAdapter<T> adapter) {
            adapters.put(type, adapter);
            return this;
        }

        public JsonMapper build() {
            return new JsonMapper(this);
        }
    }

    // ------------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------------

    /**
     * Caches field metadata per class.
     */
    private static final class ClassInfo {
        private static final Map<Class<?>, ClassInfo> CACHE = new ConcurrentHashMap<>();
        final List<FieldInfo> fields;

        private ClassInfo(Class<?> clazz) {
            List<FieldInfo> list = new ArrayList<>();
            Class<?> current = clazz;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    field.setAccessible(true);
                    list.add(new FieldInfo(field));
                }
                current = current.getSuperclass();
            }
            this.fields = Collections.unmodifiableList(list);
        }

        static ClassInfo get(Class<?> clazz) {
            return CACHE.computeIfAbsent(clazz, ClassInfo::new);
        }
    }

    /**
     * Holds a reflected field and its generic type.
     */
    private static final class FieldInfo {
        final Field field;
        final String name;
        final Type genericType;

        FieldInfo(Field field) {
            this.field = field;
            this.name = field.getName();
            this.genericType = field.getGenericType();
        }

        Object get(Object target) {
            try {
                return field.get(target);
            } catch (IllegalAccessException e) {
                throw new JsonException("Cannot read field " + name, e);
            }
        }

        void set(Object target, Object value) {
            try {
                field.set(target, value);
            } catch (IllegalAccessException e) {
                throw new JsonException("Cannot set field " + name, e);
            }
        }
    }

    /**
     * Simple recursive‑descent JSON parser.
     */
    private static final class JsonParser {
        private final String input;
        private int pos = 0;

        JsonParser(String input) {
            this.input = input;
        }

        Object parse() {
            skipWhitespace();
            if (pos >= input.length()) throw new JsonException("Empty JSON");
            char c = input.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || (c >= '0' && c <= '9')) return parseNumber();
            throw new JsonException("Unexpected character: " + c);
        }

        private Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (input.charAt(pos) == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parse();
                map.put(key, value);
                skipWhitespace();
                char c = input.charAt(pos);
                if (c == '}') {
                    pos++;
                    break;
                }
                if (c == ',') {
                    pos++;
                    continue;
                }
                throw new JsonException("Expected ',' or '}' after object entry");
            }
            return map;
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (input.charAt(pos) == ']') {
                pos++;
                return list;
            }
            while (true) {
                list.add(parse());
                skipWhitespace();
                char c = input.charAt(pos);
                if (c == ']') {
                    pos++;
                    break;
                }
                if (c == ',') {
                    pos++;
                    continue;
                }
                throw new JsonException("Expected ',' or ']' after array element");
            }
            return list;
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < input.length()) {
                char c = input.charAt(pos++);
                if (c == '"') break;
                if (c == '\\') {
                    if (pos == input.length()) throw new JsonException("Unterminated escape");
                    char e = input.charAt(pos++);
                    switch (e) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        default: throw new JsonException("Unsupported escape: \\" + e);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (input.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            } else if (input.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            throw new JsonException("Invalid boolean");
        }

        private Object parseNull() {
            if (input.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new JsonException("Invalid null");
        }

        private Number parseNumber() {
            int start = pos;
            boolean isFloating = false;
            if (input.charAt(pos) == '-') pos++;
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
            if (pos < input.length() && input.charAt(pos) == '.') {
                isFloating = true;
                pos++;
                while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
            }
            if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
                isFloating = true;
                pos++;
                if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) pos++;
                while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
            }
            String numStr = input.substring(start, pos);
            try {
                if (isFloating) return Double.parseDouble(numStr);
                long l = Long.parseLong(numStr);
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
                return l;
            } catch (NumberFormatException e) {
                throw new JsonException("Invalid number: " + numStr);
            }
        }

        private void expect(char ch) {
            if (pos >= input.length() || input.charAt(pos) != ch)
                throw new JsonException("Expected '" + ch + "'");
            pos++;
        }

        private void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++;
        }
    }

    /**
     * JSON writer that builds a string.
     */
    public static final class JsonWriter {
        private final StringBuilder out = new StringBuilder();

        void writeObjectStart() { out.append('{'); }
        void writeObjectEnd() { out.append('}'); }
        void writeArrayStart() { out.append('['); }
        void writeArrayEnd() { out.append(']'); }
        void writeNameSeparator() { out.append(':'); }
        void writeSeparator() { out.append(','); }

        void writeString(String s) {
            out.append('"');
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"': out.append("\\\""); break;
                    case '\\': out.append("\\\\"); break;
                    case '\b': out.append("\\b"); break;
                    case '\f': out.append("\\f"); break;
                    case '\n': out.append("\\n"); break;
                    case '\r': out.append("\\r"); break;
                    case '\t': out.append("\\t"); break;
                    default:
                        if (c < 0x20) {
                            out.append(String.format("\\u%04x", (int) c));
                        } else {
                            out.append(c);
                        }
                }
            }
            out.append('"');
        }

        void writeNumber(String n) { out.append(n); }
        void writeBoolean(boolean b) { out.append(b); }
        void writeNull() { out.append("null"); }

        @Override
        public String toString() { return out.toString(); }
    }
}