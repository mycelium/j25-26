package jsonlib;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TypeToken<T> {
    private final Type runtimeType;

    private TypeToken(Type runtimeType) {
        if (runtimeType == null) {
            throw new JsonException("Type description must not be null");
        }
        this.runtimeType = runtimeType;
    }

    public static <T> TypeToken<T> of(Class<T> type) {
        return new TypeToken<>(type);
    }

    public static <E> TypeToken<List<E>> listOf(Class<E> elementType) {
        return parameterized(List.class, elementType);
    }

    public static <E> TypeToken<Set<E>> setOf(Class<E> elementType) {
        return parameterized(Set.class, elementType);
    }

    public static <K, V> TypeToken<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType) {
        return parameterized(Map.class, keyType, valueType);
    }

    public static <T> TypeToken<T[]> arrayOf(Class<T> componentType) {
        return new TypeToken<>(new ArrayType(componentType));
    }

    public static <T> TypeToken<T> parameterized(Class<?> rawType, Type... arguments) {
        return new TypeToken<>(new CompositeType(rawType, arguments));
    }

    public Type getType() {
        return runtimeType;
    }

    private static final class CompositeType implements ParameterizedType {
        private final Class<?> rawType;
        private final Type[] arguments;

        private CompositeType(Class<?> rawType, Type[] arguments) {
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

    private static final class ArrayType implements GenericArrayType {
        private final Type componentType;

        private ArrayType(Type componentType) {
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
