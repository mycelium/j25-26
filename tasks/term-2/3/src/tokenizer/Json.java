package tokenizer;

import java.util.Map;

public class Json {

    public static Object parse(String json) {
        Tokenizer tokenizer = new Tokenizer(json);
        return tokenizer.parseJSON();
    }

    public static Map<String, Object> parseToMap(String json) {
        Tokenizer tokenizer = new Tokenizer(json);
        return tokenizer.parseJSONToMap();
    }

    public static <T> T parse(String json, Class<T> clazz) {
        Object parsed = parse(json);
        return JsonMapper.fromJsonValue(parsed, clazz);
    }

    public static String stringify(Object object) {
        return JsonSerializer.toJson(object);
    }
}
