package json;

import json.deserializer.JsonDeserializer;
import json.deserializer.ReflectionDeserializer;
import json.lexer.Lexer;
import json.lexer.Token;
import json.parser.JsonNode;
import json.parser.JsonParser;
import json.serializer.JsonSerializer;

import java.util.List;
import java.util.Map;

public class Json {

    public static Map<String, Object> parseToMap(String json) {
        return new JsonDeserializer().toMap(lexAndParse(json));
    }

    public static <T> T parse(String json, Class<T> clazz) {
        return new ReflectionDeserializer().toClass(lexAndParse(json), clazz);
    }

    public static String toJson(Object obj) {
        return new JsonSerializer().serialize(obj);
    }

    private static JsonNode lexAndParse(String json) {
        Lexer lexer = new Lexer(json);
        List<Token> tokens = lexer.tokenize();
        JsonParser parser = new JsonParser(tokens);
        return parser.parse();
    }
}
