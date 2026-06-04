package jsonlib.node;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonObject extends JsonNode {
    private final Map<String, JsonNode> members = new LinkedHashMap<>();

    @Override
    public boolean isObject() { return true; }

    public void put(String key, JsonNode value) { members.put(key, value); }
    public Map<String, JsonNode> getMembers() { return Collections.unmodifiableMap(members); }
    public JsonNode get(String key) { return members.get(key); }
    public int size() { return members.size(); }

    @Override
    public String toJsonString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, JsonNode> e : members.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(e.getKey())).append("\":");
            sb.append(e.getValue().toJsonString());
        }
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }
}