package jsonlib.converter;

import jsonlib.node.*;
import java.util.*;

public class JsonToObjectConverter {
    public Object convert(JsonNode node) {
        if (node.isNull()) return null;
        if (node.isString()) return ((JsonString) node).getValue();
        if (node.isNumber()) return ((JsonNumber) node).getValue();
        if (node.isBoolean()) return ((JsonBoolean) node).getValue();
        if (node.isArray()) {
            JsonArray arr = (JsonArray) node;
            List<Object> list = new ArrayList<>();
            for (JsonNode e : arr.getElements()) {
                list.add(convert(e));
            }
            return list;
        }
        if (node.isObject()) {
            JsonObject obj = (JsonObject) node;
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> e : obj.getMembers().entrySet()) {
                map.put(e.getKey(), convert(e.getValue()));
            }
            return map;
        }
        throw new IllegalStateException("Unknown node type");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> convertToMap(JsonNode node) {
        if (!node.isObject()) throw new IllegalArgumentException("Root must be JSON object");
        return (Map<String, Object>) convert(node);
    }
}