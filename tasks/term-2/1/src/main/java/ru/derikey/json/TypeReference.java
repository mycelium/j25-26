package ru.derikey.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Captures generic type information (e.g., {@code List<Person>}).
 */
public abstract class TypeReference<T> {
    private final Type type;

    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new RuntimeException("Missing type parameter");
        }
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() { return type; }
}

