package jsonlib.node;

public class JsonNull extends JsonNode {
    public static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {}

    @Override public boolean isNull() { return true; }

    @Override
    public String toJsonString() { return "null"; }
}