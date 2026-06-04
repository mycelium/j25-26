package jsonlib.node;

public class JsonBoolean extends JsonNode {
    public static final JsonBoolean TRUE = new JsonBoolean(true);
    public static final JsonBoolean FALSE = new JsonBoolean(false);

    private final boolean value;

    private JsonBoolean(boolean value) { this.value = value; }

    @Override public boolean isBoolean() { return true; }
    public boolean getValue() { return value; }

    public static JsonBoolean valueOf(boolean b) { return b ? TRUE : FALSE; }

    @Override
    public String toJsonString() { return value ? "true" : "false"; }
}