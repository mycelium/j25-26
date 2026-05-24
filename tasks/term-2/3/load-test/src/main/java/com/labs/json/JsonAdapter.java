package com.labs.json;

import com.labs.config.ParserMode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;


public class JsonAdapter {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parse(byte[] jsonBytes, ParserMode mode) {
        String json = new String(jsonBytes);
        if (mode == ParserMode.GSON) {
            return GSON.fromJson(json, MAP_TYPE);
        } else {
            return GSON.fromJson(json, MAP_TYPE);
        }
    }

    public static String toJson(Object obj, ParserMode mode) {
        if (mode == ParserMode.GSON) {
            return GSON.toJson(obj);
        } else {
            return GSON.toJson(obj);
        }
    }
}