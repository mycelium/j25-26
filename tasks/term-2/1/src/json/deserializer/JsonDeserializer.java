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

        if (node instanceof JsonPrimitive p) {
            return p.getValue();
        }

        if (node instanceof JsonArray arr) {
            List<Object> list = new ArrayList<>();
            for (JsonNode element : arr.getElements()) {
                list.add(toObject(element));
            }
            return list;
        }

        if (node instanceof JsonObject obj) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (var entry : obj.getFields().entrySet()) {
                map.put(entry.getKey(), toObject(entry.getValue()));
            }
            return map;
        }

        throw new RuntimeException("Unknown node type: " + node.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap(JsonNode node) {
        Object res = toObject(node);
        if (res instanceof Map<?,?> map) {
            return (Map<String, Object>) map;
        }
        throw new RuntimeException("Expected a JSON object but got: " + (res == null ? "null" : res.getClass().getSimpleName()));
    }
}
