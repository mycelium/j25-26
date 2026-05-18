package jsonlib;


public interface TypeAdapter<T> {

   
    String toJson(T value);
    T fromJson(Object raw);
}
