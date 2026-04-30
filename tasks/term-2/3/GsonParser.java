import java.util.*;

/**
 * Обёртка, имитирующая Gson.
 * Чтобы использовать настоящий Gson:
 *   1. Скачать gson.jar с https://search.maven.org/artifact/com.google.code.gson/gson
 *   2. Заменить тело методов ниже на:
 *      private static final com.google.gson.Gson gson = new com.google.gson.Gson();
 *      Map<String,Object> parseToMap(String json) { return gson.fromJson(json, Map.class); }
 *      String toJson(Object obj) { return gson.toJson(obj); }
 */
public class GsonParser {

    public static Map<String, Object> parseToMap(String json) {
        // заменить на gson.fromJson(json, Map.class) при использовании настоящего Gson
        return JsonParser.parseToMap(json);
    }

    public static String toJson(Object obj) {
        // заменить на gson.toJson(obj) при использовании настоящего Gson
        return JsonParser.toJson(obj);
    }
}
