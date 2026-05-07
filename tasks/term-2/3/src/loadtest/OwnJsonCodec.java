package loadtest;

import json.Json;

import java.util.Map;

final class OwnJsonCodec implements JsonCodec {
    @Override
    public Map<String, Object> parseObject(String json) {
        return Json.parseToMap(json);
    }

    @Override
    public String toJson(Object value) {
        return Json.stringify(value);
    }
}
