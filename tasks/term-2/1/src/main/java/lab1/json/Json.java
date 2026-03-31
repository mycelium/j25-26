package lab1.json;

import java.util.Map;

public class Json {
    private final JsonParser parser = new JsonParser();
    private final JsonMapper mapper = new JsonMapper();
    private final JsonSerializer serializer = new JsonSerializer();

    public Object parse(String json) {
        return parser.parse(json);
    }

    public Map<String, Object> parseToMap(String json) {
        Object value = parser.parse(json);
        if (!(value instanceof Map<?, ?> map)) {
            throw new JsonException("JSON root is not an object");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) map;
        return result;
    }

    public <T> T parse(String json, Class<T> clazz) {
        Object value = parser.parse(json);
        return mapper.convert(value, clazz);
    }

    public String toJson(Object object) {
        return serializer.toJson(object);
    }
}