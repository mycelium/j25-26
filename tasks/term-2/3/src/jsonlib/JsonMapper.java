package jsonlib;

import java.lang.reflect.*;
import java.util.*;


public final class JsonMapper {

    
    private final FieldNamingStrategy namingStrategy;
    private final boolean ignoreUnknownFields;
    private final Map<Class<?>, TypeAdapter<?>> adapters; // immutable after build

    private JsonMapper(Builder builder) {
        this.namingStrategy      = builder.namingStrategy;
        this.ignoreUnknownFields = builder.ignoreUnknownFields;
        this.adapters            = Collections.unmodifiableMap(new HashMap<>(builder.adapters));
    }

   
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private FieldNamingStrategy namingStrategy    = FieldNamingStrategy.IDENTITY;
        private boolean ignoreUnknownFields           = false;
        private final Map<Class<?>, TypeAdapter<?>> adapters = new HashMap<>();

        /** Set the field naming strategy (default: {@link FieldNamingStrategy#IDENTITY}). */
        public Builder fieldNamingStrategy(FieldNamingStrategy strategy) {
            this.namingStrategy = Objects.requireNonNull(strategy);
            return this;
        }

       
        public Builder ignoreUnknownFields(boolean ignore) {
            this.ignoreUnknownFields = ignore;
            return this;
        }

        
        public <T> Builder registerTypeAdapter(Class<T> type, TypeAdapter<T> adapter) {
            adapters.put(Objects.requireNonNull(type), Objects.requireNonNull(adapter));
            return this;
        }

        public JsonMapper build() { return new JsonMapper(this); }
    }

   
    static final JsonMapper DEFAULT = builder().ignoreUnknownFields(true).build();

    // ================================================================= Public API

    /** Deserialize {@code json} into an instance of {@code clazz}. */
    public <T> T fromJson(String json, Class<T> clazz) {
        Object parsed = JsonParser.parse(json);
        return convertTo(parsed, clazz, clazz);
    }

    
    public <T> T fromJson(String json, TypeReference<T> typeRef) {
        Object parsed = JsonParser.parse(json);
        @SuppressWarnings("unchecked")
        T result = (T) convertToType(parsed, typeRef.getType());
        return result;
    }

    public String toJson(Object obj) {
        return serialize(obj);
    }

   
    @SuppressWarnings("unchecked")
    private <T> T convertTo(Object raw, Class<T> clazz, Type genericType) {
        if (raw == null) return null;

        // 1. User-registered TypeAdapter wins over everything
        TypeAdapter<T> adapter = (TypeAdapter<T>) adapters.get(clazz);
        if (adapter != null) return adapter.fromJson(raw);

        // 2. Primitives / wrappers / String
        if (isPrimitiveOrWrapper(clazz)) return (T) convertScalar(raw, clazz);

        // 3. List<?>
        if (List.class.isAssignableFrom(clazz) && raw instanceof List) {
            return (T) convertToList((List<?>) raw, genericType);
        }

        // 4. Map<?,?>
        if (Map.class.isAssignableFrom(clazz) && raw instanceof Map) {
            return (T) convertToMap((Map<?, ?>) raw, genericType);
        }

        // 5. POJO
        if (raw instanceof Map) {
            return mapToInstance((Map<String, Object>) raw, clazz);
        }

        throw new JsonParseException(
            "Cannot convert " + raw.getClass().getSimpleName() + " to " + clazz.getSimpleName()
        );
    }

    private Object convertToType(Object raw, Type type) {
        if (type instanceof Class<?> cls) {
            return convertTo(raw, cls, type);
        }
        if (type instanceof ParameterizedType pt) {
            Class<?> rawClass = (Class<?>) pt.getRawType();
            if (List.class.isAssignableFrom(rawClass) && raw instanceof List)
                return convertToList((List<?>) raw, type);
            if (Map.class.isAssignableFrom(rawClass) && raw instanceof Map)
                return convertToMap((Map<?, ?>) raw, type);
            return convertTo(raw, rawClass, type);
        }
        return raw; // wildcard / type variable — return as-is
    }

        @SuppressWarnings("unchecked")
    private <T> T mapToInstance(Map<String, Object> jsonMap, Class<T> clazz) {
        ClassInfo info = ClassInfo.of(clazz, namingStrategy);

        try {
            T obj = (T) info.constructor.newInstance();

            for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                String jsonKey = entry.getKey();
                Field field = info.fieldsByJsonKey.get(jsonKey);

                if (field == null) {
                    if (!ignoreUnknownFields) {
                        throw new JsonParseException(
                            "Unknown JSON field \"" + jsonKey + "\" for class "
                            + clazz.getSimpleName()
                            + ". Use .ignoreUnknownFields(true) to suppress this error."
                        );
                    }
                    continue;
                }

                Object converted = convertToType(entry.getValue(), field.getGenericType());
                field.set(obj, converted);
            }

            return obj;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getSimpleName(), e);
        }
    }

    
    private List<Object> convertToList(List<?> rawList, Type genericType) {
        Type elementType = Object.class;
        if (genericType instanceof ParameterizedType pt) {
            elementType = pt.getActualTypeArguments()[0];
        }
        List<Object> result = new ArrayList<>(rawList.size());
        for (Object item : rawList) {
            result.add(convertToType(item, elementType));
        }
        return Collections.unmodifiableList(result);
    }

    private Map<String, Object> convertToMap(Map<?, ?> rawMap, Type genericType) {
        Type valueType = Object.class;
        if (genericType instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length >= 2) valueType = args[1];
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : rawMap.entrySet()) {
            result.put(e.getKey().toString(), convertToType(e.getValue(), valueType));
        }
        return Collections.unmodifiableMap(result);
    }

    // ----------------------------------------------------------------- Scalar coercion

    private static Object convertScalar(Object value, Class<?> type) {
        if (value == null)                                       return null;
        if (type == String.class)                                return value.toString();
        if (type == boolean.class || type == Boolean.class)     return value;
        if (value instanceof Number n) {
            if (type == int.class    || type == Integer.class)  return n.intValue();
            if (type == long.class   || type == Long.class)     return n.longValue();
            if (type == double.class || type == Double.class)   return n.doubleValue();
            if (type == float.class  || type == Float.class)    return n.floatValue();
            if (type == short.class  || type == Short.class)    return n.shortValue();
            if (type == byte.class   || type == Byte.class)     return n.byteValue();
        }
        return value;
    }

    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive()
            || type == String.class  || type == Boolean.class
            || type == Integer.class || type == Long.class
            || type == Double.class  || type == Float.class
            || type == Short.class   || type == Byte.class
            || type == Character.class
            || Number.class.isAssignableFrom(type);
    }

    // ================================================================= Serialization internals

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String serialize(Object obj) {
        if (obj == null)             return "null";

        // TypeAdapter takes priority
        TypeAdapter adapter = adapters.get(obj.getClass());
        if (adapter != null)         return adapter.toJson(obj);

        if (obj instanceof String s)        return "\"" + JsonWriter.escape(s) + "\"";
        if (obj instanceof Boolean)         return obj.toString();
        if (obj instanceof Number n)        return JsonWriter.serializeNumber(n);
        if (obj instanceof Map<?,?> m)      return mapToJsonString(m);
        if (obj instanceof Collection<?> c) return collectionToJsonString(c);
        if (obj.getClass().isArray())       return arrayToJsonString(obj);
        return objectToJsonString(obj);
    }

    private String objectToJsonString(Object obj) {
        ClassInfo info = ClassInfo.of(obj.getClass(), namingStrategy);
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Field f : info.fields()) {
            try {
                Object val = f.get(obj);
                if (!first) sb.append(',');
                first = false;
                String jsonKey = namingStrategy.toJsonKey(f.getName());
                sb.append('"').append(JsonWriter.escape(jsonKey)).append("\":");
                sb.append(serialize(val));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot read field: " + f.getName(), e);
            }
        }
        return sb.append('}').toString();
    }

    private String mapToJsonString(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(JsonWriter.escape(e.getKey().toString())).append("\":");
            sb.append(serialize(e.getValue()));
        }
        return sb.append('}').toString();
    }

    private String collectionToJsonString(Collection<?> col) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object o : col) {
            if (!first) sb.append(',');
            first = false;
            sb.append(serialize(o));
        }
        return sb.append(']').toString();
    }

    private String arrayToJsonString(Object array) {
        StringBuilder sb = new StringBuilder("[");
        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(',');
            sb.append(serialize(Array.get(array, i)));
        }
        return sb.append(']').toString();
    }
}
