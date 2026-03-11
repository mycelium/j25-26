package org.example.json;

public class JsonConfig {

    private final boolean prettyPrint;
    private final boolean ignoreUnknownFields;
    private final boolean includeNulls;

    public JsonConfig() {
        this(false, true, true);
    }

    public JsonConfig(boolean prettyPrint, boolean ignoreUnknownFields, boolean includeNulls) {
        this.prettyPrint = prettyPrint;
        this.ignoreUnknownFields = ignoreUnknownFields;
        this.includeNulls = includeNulls;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public boolean isIgnoreUnknownFields() {
        return ignoreUnknownFields;
    }

    public boolean isIncludeNulls() {
        return includeNulls;
    }
}