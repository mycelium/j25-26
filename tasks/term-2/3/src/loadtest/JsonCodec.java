package loadtest;

import java.util.Map;

interface JsonCodec {
    Map<String, Object> parseObject(String json);

    String toJson(Object value);
}
