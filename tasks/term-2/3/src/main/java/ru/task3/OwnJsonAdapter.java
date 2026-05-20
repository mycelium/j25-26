package ru.task3;

import ru.example.json.Json;

public class OwnJsonAdapter implements JsonAdapter {

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        return Json.toObject(json, clazz);
    }

    @Override
    public String toJson(Object obj) {
        return Json.stringify(obj);
    }
}
