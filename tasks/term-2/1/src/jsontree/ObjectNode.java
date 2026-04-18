package jsontree;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectNode extends JsonNode{
    private final Map<String, JsonNode> members = new LinkedHashMap<>();
    public void put(String key, JsonNode value) {
        members.put(key, value);
    }
    public JsonNode get(String key) {
        return members.get(key);
    }
    public Map<String, JsonNode> getMembers() {
        return members;
    }
}
