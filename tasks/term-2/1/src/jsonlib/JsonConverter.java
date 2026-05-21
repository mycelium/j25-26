package jsonlib;

public interface JsonConverter<T> {
    Object encode(T value);

    T decode(Object value);
}
