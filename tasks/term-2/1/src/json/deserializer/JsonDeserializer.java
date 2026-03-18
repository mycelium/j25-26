package json.deserializer;

import json.parser.JsonArray;
import json.parser.JsonNode;
import json.parser.JsonObject;
import json.parser.JsonPrimitive;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonDeserializer {

    public Object toObject(JsonNode node) {
        if (node == null) return null;

        if (node instanceof JsonPrimitive) {
            return ((JsonPrimitive) node).getValue();
        }

        if (node instanceof JsonArray) {
            JsonArray jsonArray = (JsonArray) node;
            List<Object> list = new ArrayList<>();
            for (JsonNode element : jsonArray.getElements()) {
                list.add(toObject(element));
            }
            return list;
        }

        if (node instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) node;
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> entry : jsonObject.getFields().entrySet()) {
                map.put(entry.getKey(), toObject(entry.getValue()));
            }
            return map;
        }

        throw new IllegalArgumentException("Unknown node type: " + node.getClass().getName());
    }

    public Map<String, Object> toMap(JsonNode node) {
        Object res = toObject(node);
        if (res instanceof Map) {
            return (Map<String, Object>) res;
        }
        throw new RuntimeException("Expected a JSON object but got something else: " + (res == null ? "null" : res.getClass().getSimpleName()));
    }
}
