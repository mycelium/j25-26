package ru.task3;

import com.google.gson.Gson;

public class GsonJsonAdapter implements JsonAdapter {

    private final Gson gson = new Gson();

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    @Override
    public String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
