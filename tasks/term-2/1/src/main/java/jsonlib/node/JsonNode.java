package jsonlib.node;

/**
 * Абстрактный узел JSON.
 */
public abstract class JsonNode {

    public boolean isObject() { return false; }
    public boolean isArray() { return false; }
    public boolean isString() { return false; }
    public boolean isNumber() { return false; }
    public boolean isBoolean() { return false; }
    public boolean isNull() { return false; }

    public abstract String toJsonString();

    @Override
    public String toString() {
        return toJsonString();
    }
}