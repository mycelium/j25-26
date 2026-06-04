package app;

import jsonlib.Json;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public interface ParserType {
    <T> T parse(String json, Class<T> clazz) throws Exception;
    String toJson(Object obj) throws Exception;

    static ParserType create(String name) {
        return switch (name.toLowerCase()) {
            case "own" -> new ParserType() {
                public <T> T parse(String json, Class<T> clazz) {
                    return Json.parse(json, clazz);
                }
                public String toJson(Object obj) {
                    return Json.toJson(obj);
                }
            };
            case "jackson" -> new ParserType() {
                final ObjectMapper mapper = new ObjectMapper();
                public <T> T parse(String json, Class<T> clazz) throws Exception {
                    return mapper.readValue(json, clazz);
                }
                public String toJson(Object obj) throws Exception {
                    return mapper.writeValueAsString(obj);
                }
            };
            case "gson" -> new ParserType() {
                final Gson gson = new Gson();
                public <T> T parse(String json, Class<T> clazz) {
                    return gson.fromJson(json, clazz);
                }
                public String toJson(Object obj) {
                    return gson.toJson(obj);
                }
            };
            default -> throw new IllegalArgumentException("Unknown parser: " + name);
        };
    }
}