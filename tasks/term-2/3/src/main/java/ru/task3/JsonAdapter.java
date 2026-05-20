package ru.task3;

public interface JsonAdapter {

    <T> T fromJson(String json, Class<T> clazz);

    String toJson(Object obj);
}