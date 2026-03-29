package ru.derikey.json;

import java.lang.reflect.Type;
import ru.derikey.json.JsonMapper.*;

/**
 * Adapter for custom (de)serialization of a specific type.
 */
public interface TypeAdapter<T> {
    void write(T value, JsonWriter writer) throws Exception;
    T read(Object jsonValue, Type targetType) throws Exception; // jsonValue is parsed intermediate
}

