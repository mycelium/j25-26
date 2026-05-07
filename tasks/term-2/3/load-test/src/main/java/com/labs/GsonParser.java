package com.labs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

public class GsonParser implements JsonParser {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
    @Override public Map<String, Object> parse(String json) { return GSON.fromJson(json, MAP_TYPE); }
    @Override public String toJson(Map<String, Object> map) { return GSON.toJson(map); }
}