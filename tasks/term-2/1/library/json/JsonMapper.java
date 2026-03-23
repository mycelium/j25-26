package json;

import json.exceptions.JsonMappingException;
import json.internal.JsonParser;
import json.internal.JsonWriter;
import json.internal.TypeConverter;

import java.util.Map;
import java.util.Objects;

public final class JsonMapper {
    private final JsonConfig config;

    public JsonMapper() {
        this(JsonConfig.defaultConfig());
    }

    public JsonMapper(JsonConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    public JsonConfig getConfig() {
        return config;
    }

    public Object read(String json) {
        return new JsonParser(json).parse();
    }

    public Map<String, Object> readAsMap(String json) {
        Object parsed = read(json);
        if (!(parsed instanceof Map<?, ?> mapValue)) {
            throw new JsonMappingException("JSON root must be an object to convert into Map<String, Object>");
        }

        for (Object key : mapValue.keySet()) {
            if (!(key instanceof String)) {
                throw new JsonMappingException("JSON object contains non-string key");
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) mapValue;
        return result;
    }

    public <T> T read(String json, Class<T> targetClass) {
        Objects.requireNonNull(targetClass, "targetClass must not be null");
        Object parsed = read(json);
        return new TypeConverter(config).convert(parsed, targetClass);
    }

    public String write(Object value) {
        return new JsonWriter(config).write(value);
    }
}
