package jsonlib;

public interface JsonAdapter<T> {
    Object encode(T value);

    T decode(Object value);
}
