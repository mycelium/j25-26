import java.lang.reflect.Type;
import java.util.Map;
import java.util.List;

public final class JsonLib {

    private final JsonConfig config;

    public JsonLib() {
        this(JsonConfig.defaultConfig());
    }

    public JsonLib(JsonConfig config) {
        if (config == null) throw new RuntimeException("JsonConfig must not be null");
        this.config = config;
    }

    public String toJson(Object obj) {
        return ToJsonParser.parseToJson(obj, config);
    }

    public <T> T fromJson(String json, Class<T> cls) {
        return JsonCast.convert(FromJsonParser.parseToObject(json, config), (Type) cls, config);
    }

    public <T> T fromJson(String json, TypeToken<T> token) {
        return JsonCast.convert(FromJsonParser.parseToObject(json, config), token.getType(), config);
    }

    public Map<String, Object> toMap(String json) {
        return FromJsonParser.parseToMap(json, config);
    }

    public List<Object> toList(String json) {
        Object parsed = FromJsonParser.parseToObject(json, config);
        if (!(parsed instanceof List<?> lst))
            throw new RuntimeException("JSON is not an array");
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) lst;
        return result;
    }

    public JsonConfig getConfig() { return config; }
}
