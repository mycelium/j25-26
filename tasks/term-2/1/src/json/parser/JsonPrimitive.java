package json.parser;

public class JsonPrimitive extends JsonNode {

    private final Object value;

    public JsonPrimitive(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
