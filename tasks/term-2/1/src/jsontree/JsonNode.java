package jsontree;

public abstract class JsonNode {
    public boolean isObject() { return this instanceof ObjectNode; }
    public boolean isArray() { return this instanceof ArrayNode; }
    public boolean isPrimitive() { return this instanceof BaseNode; }
}
