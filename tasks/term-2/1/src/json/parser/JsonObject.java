package json.parser;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonObject extends JsonNode {

    private final Map<String, JsonNode> fields = new LinkedHashMap<>();

    public void put(String key, JsonNode value) {
        fields.put(key,value);
    }

    public JsonNode get(String key) {
        return fields.get(key);
    }

    public Map<String, JsonNode> getFields() {
        return fields;
    }

    public boolean has(String key) {
        return fields.containsKey(key);
    }
}
