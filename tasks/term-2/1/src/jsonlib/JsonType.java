package jsonlib;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JsonType<T> {
    private final Type runtimeType;

    private JsonType(Type runtimeType) {
        if (runtimeType == null) {
            throw new JsonException("Type description must not be null");
        }
        this.runtimeType = runtimeType;
    }

    public static <T> JsonType<T> of(Class<T> type) {
        return new JsonType<>(type);
    }

    public static <E> JsonType<List<E>> listOf(Class<E> elementType) {
        return parameterized(List.class, elementType);
    }

    public static <E> JsonType<Set<E>> setOf(Class<E> elementType) {
        return parameterized(Set.class, elementType);
    }

    public static <K, V> JsonType<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType) {
        return parameterized(Map.class, keyType, valueType);
    }

    public static <T> JsonType<T[]> arrayOf(Class<T> componentType) {
        return new JsonType<>(new ArrayShape(componentType));
    }

    public static <T> JsonType<T> parameterized(Class<?> rawType, Type... arguments) {
        return new JsonType<>(new TypeShape(rawType, arguments));
    }

    Type getType() {
        return runtimeType;
    }

    private static final class TypeShape implements ParameterizedType {
        private final Class<?> rawType;
        private final Type[] arguments;

        private TypeShape(Class<?> rawType, Type[] arguments) {
            if (rawType == null) {
                throw new JsonException("Raw type must not be null");
            }
            this.rawType = rawType;
            this.arguments = arguments.clone();
        }

        @Override
        public Type[] getActualTypeArguments() {
            return arguments.clone();
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

    private static final class ArrayShape implements GenericArrayType {
        private final Type componentType;

        private ArrayShape(Type componentType) {
            if (componentType == null) {
                throw new JsonException("Array component type must not be null");
            }
            this.componentType = componentType;
        }

        @Override
        public Type getGenericComponentType() {
            return componentType;
        }
    }
}
