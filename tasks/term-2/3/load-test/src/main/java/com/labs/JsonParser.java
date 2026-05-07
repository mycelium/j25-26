package com.labs;
import java.util.Map;

public interface JsonParser {
    Map<String, Object> parse(String json);
    String toJson(Map<String, Object> map);
}