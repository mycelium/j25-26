package jsonlib;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class Json {
    private final NamingPolicy fieldNaming;
    private final boolean writeNulls;
    private final Map<Class<?>, JsonAdapter<?>> converters;

    private Json(Builder builder) {
        this.fieldNaming = builder.fieldNaming;
        this.writeNulls = builder.writeNulls;
        this.converters = Collections.unmodifiableMap(new LinkedHashMap<>(builder.converters));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Object parse(String json) {
        return new JsonReader(json).read();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> parseObject(String json) {
        Object root = parse(json);
        if (!(root instanceof Map<?, ?>)) {
            throw new JsonException("Root JSON value is not an object");
        }
        return (Map<String, Object>) root;
    }

    public <T> T fromJson(String json, Class<T> type) {
        return convert(parse(json), type);
    }

    public <T> T fromJson(String json, TypeToken<T> token) {
        @SuppressWarnings("unchecked")
        T result = (T) convert(parse(json), token.getType());
        return result;
    }

    public String toJson(Object value) {
        return new JsonOutput(fieldNaming, writeNulls, converters).write(value);
    }

    public <T> T convert(Object value, Class<T> type) {
        @SuppressWarnings("unchecked")
        T result = (T) convert(value, (Type) type);
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convert(Object value, Type targetType) {
        if (targetType instanceof Class<?> type) {
            JsonAdapter<?> adapter = converters.get(type);
            if (adapter != null) {
                return ((JsonAdapter) adapter).decode(value);
            }

            if (value == null) {
                return type.isPrimitive() ? primitiveDefault(type) : null;
            }
            if (type == Object.class || type.isInstance(value)) {
                return value;
            }
            if (type == String.class) {
                return String.valueOf(value);
            }
            if (type == char.class || type == Character.class) {
                String text = requireString(value, type);
                if (text.length() != 1) {
                    throw new JsonException("Cannot convert string to char: " + text);
                }
                return text.charAt(0);
            }
            if (type == boolean.class || type == Boolean.class) {
                if (value instanceof Boolean bool) {
                    return bool;
                }
                throw typeError(value, type);
            }
            if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
                return convertNumber(value, type);
            }
            if (type.isEnum()) {
                return Enum.valueOf((Class<Enum>) type.asSubclass(Enum.class), requireString(value, type));
            }
            if (type.isArray()) {
                return convertArray(value, type.getComponentType());
            }
            if (Collection.class.isAssignableFrom(type)) {
                return convertCollection(value, type, Object.class);
            }
            if (Map.class.isAssignableFrom(type)) {
                return convertMap(value, type, String.class, Object.class);
            }
            if (value instanceof Map<?, ?> map) {
                return convertObject(map, type);
            }
            throw typeError(value, type);
        }

        if (targetType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class<?> rawClass)) {
                throw new JsonException("Unsupported type: " + targetType);
            }

            if (Collection.class.isAssignableFrom(rawClass)) {
                Type elementType = parameterizedType.getActualTypeArguments()[0];
                return convertCollection(value, rawClass, elementType);
            }
            if (Map.class.isAssignableFrom(rawClass)) {
                Type[] args = parameterizedType.getActualTypeArguments();
                return convertMap(value, rawClass, args[0], args[1]);
            }
            return convert(value, rawClass);
        }

        if (targetType instanceof GenericArrayType arrayType) {
            return convertArray(value, arrayType.getGenericComponentType());
        }

        throw new JsonException("Unsupported type: " + targetType);
    }

    private Object convertArray(Object value, Type componentType) {
        if (!(value instanceof List<?> values)) {
            throw typeError(value, Object[].class);
        }

        Class<?> componentClass = rawClass(componentType);
        Object array = Array.newInstance(componentClass, values.size());
        for (int i = 0; i < values.size(); i++) {
            Array.set(array, i, convert(values.get(i), componentType));
        }
        return array;
    }

    private Collection<Object> convertCollection(Object value, Class<?> collectionType, Type elementType) {
        if (!(value instanceof List<?> values)) {
            throw typeError(value, collectionType);
        }

        Collection<Object> result = createCollection(collectionType);
        for (Object item : values) {
            result.add(convert(item, elementType));
        }
        return result;
    }

    private Map<Object, Object> convertMap(Object value, Class<?> mapType, Type keyType, Type valueType) {
        if (!(value instanceof Map<?, ?> map)) {
            throw typeError(value, mapType);
        }

        Map<Object, Object> result = createMap(mapType);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = convert(entry.getKey(), keyType);
            Object mappedValue = convert(entry.getValue(), valueType);
            result.put(key, mappedValue);
        }
        return result;
    }

    private Object convertObject(Map<?, ?> map, Class<?> type) {
        Object instance = createObject(type);
        for (Field field : fieldsOf(type)) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
                continue;
            }

            String jsonName = fieldNaming.apply(field.getName());
            if (!map.containsKey(jsonName)) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object rawValue = map.get(jsonName);
                if (rawValue == null && field.getType().isPrimitive()) {
                    continue;
                }
                field.set(instance, convert(rawValue, field.getGenericType()));
            } catch (IllegalAccessException exception) {
                throw new JsonException("Cannot write field " + field.getName(), exception);
            }
        }
        return instance;
    }

    private Object convertNumber(Object value, Class<?> type) {
        if (!(value instanceof Number number)) {
            throw typeError(value, type);
        }

        if (type == byte.class || type == Byte.class) {
            return number.byteValue();
        }
        if (type == short.class || type == Short.class) {
            return number.shortValue();
        }
        if (type == int.class || type == Integer.class) {
            return number.intValue();
        }
        if (type == long.class || type == Long.class) {
            return number.longValue();
        }
        if (type == float.class || type == Float.class) {
            return number.floatValue();
        }
        if (type == double.class || type == Double.class) {
            return number.doubleValue();
        }
        throw typeError(value, type);
    }

    private String requireString(Object value, Class<?> targetType) {
        if (value instanceof String text) {
            return text;
        }
        throw typeError(value, targetType);
    }

    private Object primitiveDefault(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        return 0;
    }

    private Class<?> rawClass(Type type) {
        if (type instanceof Class<?> raw) {
            return raw;
        }
        if (type instanceof ParameterizedType parameterized && parameterized.getRawType() instanceof Class<?> raw) {
            return raw;
        }
        if (type instanceof GenericArrayType arrayType) {
            return Array.newInstance(rawClass(arrayType.getGenericComponentType()), 0).getClass();
        }
        throw new JsonException("Unsupported component type: " + type);
    }

    private Collection<Object> createCollection(Class<?> type) {
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            if (Set.class.isAssignableFrom(type)) {
                return new LinkedHashSet<>();
            }
            if (Queue.class.isAssignableFrom(type)) {
                return new ArrayDeque<>();
            }
            return new ArrayList<>();
        }
        return createObject(type);
    }

    private Map<Object, Object> createMap(Class<?> type) {
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            return new LinkedHashMap<>();
        }
        return createObject(type);
    }

    @SuppressWarnings("unchecked")
    private <T> T createObject(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new JsonException("Class must have a no-arg constructor: " + type.getName(), exception);
        }
    }

    private List<Field> fieldsOf(Class<?> type) {
        ArrayList<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            Collections.addAll(fields, current.getDeclaredFields());
            current = current.getSuperclass();
        }
        return fields;
    }

    private JsonException typeError(Object value, Type type) {
        String actual = value == null ? "null" : value.getClass().getName();
        return new JsonException("Cannot convert " + actual + " to " + type);
    }

    public static final class Builder {
        private NamingPolicy fieldNaming = NamingPolicy.KEEP_ORIGINAL;
        private boolean writeNulls = true;
        private final Map<Class<?>, JsonAdapter<?>> converters = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder fieldNames(NamingPolicy fieldNaming) {
            if (fieldNaming == null) {
                throw new JsonException("Field naming mode must not be null");
            }
            this.fieldNaming = fieldNaming;
            return this;
        }

        public Builder writeNulls(boolean writeNulls) {
            this.writeNulls = writeNulls;
            return this;
        }

        public <T> Builder registerConverter(Class<T> type, JsonAdapter<T> converter) {
            if (type == null || converter == null) {
                throw new JsonException("Converter type and implementation must not be null");
            }
            converters.put(type, converter);
            return this;
        }

        public Json build() {
            return new Json(this);
        }
    }
}
