package jsontree;

public class BaseNode extends JsonNode {
    private final Object value;
    public BaseNode(Object value) {
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
}
