package loadtest;

import com.google.gson.Gson;

import java.util.Map;

final class GsonJsonCodec implements JsonCodec {
    private final Gson gson = new Gson();

    @Override
    public Map<String, Object> parseObject(String json) {
        Object parsed = gson.fromJson(json, Map.class);
        if (!(parsed instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("JSON root must be an object");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) map;
        return result;
    }

    @Override
    public String toJson(Object value) {
        return gson.toJson(value);
    }
}
