package json.parser;

import java.util.ArrayList;
import java.util.List;

public class JsonArray extends JsonNode {

    private final List<JsonNode> elements = new ArrayList<>();

    public void add(JsonNode element) {
        if(element != null) elements.add(element);
    }

    public JsonNode get(int index) {
        if(index >= 0 && index<elements.size()) return elements.get(index);
        return null;
    }

    public List<JsonNode> getElements() {
        return elements;
    }

    public int size() {
        return elements.size();
    }
}
