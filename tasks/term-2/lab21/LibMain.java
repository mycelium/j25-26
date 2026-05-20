import java.util.Map;

public class LibMain {
    public <T> T readValue(String json, Class<T> clazz) {
        if (clazz == Map.class) {
            return (T) parseToMap(json);
        } else {
            Splitter splitter = new Splitter(json);
        Parser parser = new Parser(splitter);
        return parser.parseObject(clazz);
        }
    }
    public String writeValueAsString(Object obj) {
        return Serializer.serialize(obj);
    }
    public Map<String, Object> parseToMap(String json) {
        Splitter splitter = new Splitter(json);
        Parser parser = new Parser(splitter);
        return parser.parseMap();
    }
}