package org.example.json;

import java.util.Map;

public final class Json {

    private final JsonParser parser;
    private final JsonSerializer serializer;
    private final JsonMap mapper;
    private final JsonConfig config;

    public Json() {
        this(new JsonConfig());
    }

    public Json(JsonConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("JsonConfig must not be null");
        }

        this.config = config;
        this.parser = new JsonParser(config);
        this.serializer = new JsonSerializer(config);
        this.mapper = new JsonMap(config);
    }

    public Object parse(String json) {
        return parser.parse(json);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap(String json) {
        Object parsed = parser.parse(json);

        if (!(parsed instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("JSON root is not an object");
        }

        return (Map<String, Object>) parsed;
    }

    public <T> T fromJson(String json, Class<T> targetClass) {
        Object parsed = parser.parse(json);
        return mapper.toObject(parsed, targetClass);
    }

    public String toJson(Object object) {
        return serializer.serialize(object);
    }

    public JsonConfig getConfig() {
        return config;
    }
}