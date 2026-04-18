package json;

import jsontree.*;
import lexer.*;
import java.util.List;
import java.util.Map;

public class Json {
    public static Map<String, Object> parseToMap(String jsonString) {
        JsonLexer lexer = new JsonLexer(jsonString);
        List<Lexeme> lexemes = lexer.analyzeJson();
        JsonTree tree = new JsonTree(lexemes);
        TreeConverter converter = new TreeConverter(tree.getRoot());
        return converter.convertToMap();
    }

    public static <T> T parseToObject(String jsonString, Class<T> clazz) {
        JsonLexer lexer = new JsonLexer(jsonString);
        List<Lexeme> lexemes = lexer.analyzeJson();
        JsonTree tree = new JsonTree(lexemes);
        TreeConverter converter = new TreeConverter(tree.getRoot());
        return converter.convertToObject(clazz);
    }

    public static String toJson(Object obj) {
        JsonTree tree = new JsonTree(obj);
        TreeConverter converter = new TreeConverter(tree.getRoot());
        return converter.convertToJson();
    }
}