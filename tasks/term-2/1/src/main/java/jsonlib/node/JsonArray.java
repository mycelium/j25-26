package jsonlib.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonArray extends JsonNode {
    private final List<JsonNode> elements = new ArrayList<>();

    @Override
    public boolean isArray() { return true; }

    public void add(JsonNode value) { elements.add(value); }
    public List<JsonNode> getElements() { return Collections.unmodifiableList(elements); }
    public JsonNode get(int index) { return elements.get(index); }
    public int size() { return elements.size(); }

    @Override
    public String toJsonString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(elements.get(i).toJsonString());
        }
        sb.append("]");
        return sb.toString();
    }
}