package com.labs;
import jsonlib.Json;
import java.util.Map;

public class OwnJsonParser implements JsonParser {
    @Override
    public Map<String, Object> parse(String json) { return Json.fromJsonToMap(json); }
    @Override
    public String toJson(Map<String, Object> map) { return Json.toJson(map); }
}