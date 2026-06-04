package app;

import jsonlib.Json;
import jsonlib.JsonParseException;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   JSON LIBRARY DEMONSTRATION");
        System.out.println("==========================================\n");

        demonstrateParseToObject();
        demonstrateParseToMap();
        demonstrateParseToClass();
        demonstrateSerialization();
        demonstrateEdgeCases();
    }

    // 1) JSON -> Object
    private static void demonstrateParseToObject() {
        printHeader("1. parse(json) -> Object");

        String json = """
            {
                "name": "Kirill",
                "age": 21,
                "city": "SPB",
                "hobbies": ["programming", "gym"],
                "height": 175,
                "course": 3
            }
            """;

        Object obj = Json.parse(json);
        System.out.println("Result: " + obj);
        System.out.println("Type: " + obj.getClass().getName());

        System.out.println("\nSimple values:");
        printResult("String", Json.parse("\"Hello World\""), true);
        printResult("Number 52", Json.parse("42"), true);
        printResult("Number 3.14", Json.parse("3.14"), true);
        printResult("true", Json.parse("true"), true);
        printResult("false", Json.parse("false"), true);
        printResult("null", Json.parse("null"), true);

        System.out.println("\nArray:");
        Object arr = Json.parse("[1, \"two\", true, null, {\"key\":\"value\"}]");
        System.out.println("  " + arr);
        System.out.println("  Type: " + arr.getClass().getName());
    }

    // 2) JSON -> Map<String, Object>
    private static void demonstrateParseToMap() {
        printHeader("2. parseToMap(json) -> Map<String, Object>");

        String json = "{\"product\":\"laptop\",\"price\":99999.99,\"gaming\":true}";

        Map<String, Object> map = Json.parseToMap(json);
        System.out.println("product: " + map.get("product") +
                " (" + map.get("product").getClass().getSimpleName() + ")");
        System.out.println("price: " + map.get("price") +
                " (" + map.get("price").getClass().getSimpleName() + ")");
        System.out.println("gaming: " + map.get("gaming") +
                " (" + map.get("gaming").getClass().getSimpleName() + ")");
    }

    // 3) JSON -> Custom class
    private static void demonstrateParseToClass() {
        printHeader("3. parse(json, Class<T>) -> class instance");

        String json = """
            {
                "name": "Oleg",
                "age": 70,
                "email": "olegCzar@gmail.com",
                "tags": ["developer", "pascal"]
            }
            """;

        User user = Json.parse(json, User.class);
        System.out.println("User object:");
        System.out.println("  name: " + user.name);
        System.out.println("  age: " + user.age);
        System.out.println("  email: " + user.email);
        System.out.println("  tags: " + user.tags);

        String complexJson = """
            {
                "name": "Pavel Durov",
                "age": 41,
                "kid": {
                    "name": "EcoKid",
                    "age": 1
                }
            }
            """;

        Father father = Json.parse(complexJson, Father.class);
        System.out.println("\nPavel Durov and kids info:");
        System.out.println("  name: " + father.name);
        System.out.println("  age: " + father.age);
        System.out.println("  kid.name: " + father.kid.name);
        System.out.println("  kid.age: " + father.kid.age);
    }

    // 4) Java -> JSON
    private static void demonstrateSerialization() {
        printHeader("4. toJson(obj) -> JSON string");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "success");
        data.put("code", 200);
        data.put("data", Arrays.asList(1, 2, 3));

        String compact = Json.toJson(data);
        System.out.println("Compact JSON:");
        System.out.println("  " + compact);

        String pretty = Json.toJson(data, true);
        System.out.println("\nFormatted JSON (pretty):");
        System.out.println(pretty);

        User user = new User();
        user.name = "Durov";
        user.age = 41;
        user.email = "Durov@Durov.com";
        user.tags = Arrays.asList("King", "Creator");

        System.out.println("Serialization of User object:");
        System.out.println("  " + Json.toJson(user, true));
    }

    // 5) Граничные случаи
    private static void demonstrateEdgeCases() {
        printHeader("5. Edge cases");

        System.out.println("Empty object: " + Json.parse("{}"));
        System.out.println("Empty array: " + Json.parse("[]"));
        System.out.println("Escape sequences: " +
                Json.parse("\"Hello\\nWorld\\t!\""));
        System.out.println("Unicode: " + Json.parse("\"\\u0048\\u0065\\u006C\\u006C\\u006F\""));

        System.out.println("\nParse error demonstration:");
        try {
            Json.parse("{invalid json}");
        } catch (JsonParseException e) {
            System.out.println("  Caught exception: " + e.getMessage());
        }

        System.out.println("\nRound-trip (object -> JSON -> object):");
        Map<String, Object> original = new LinkedHashMap<>();
        original.put("x", 10);
        original.put("y", "text");
        String json = Json.toJson(original);
        Map<String, Object> restored = Json.parseToMap(json);
        System.out.println("  Original: " + original);
        System.out.println("  JSON:     " + json);
        System.out.println("  Restored: " + restored);
        System.out.println("  Match: " + original.equals(restored));
    }

    private static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(title);
        System.out.println("=".repeat(60));
    }

    private static void printResult(String label, Object value, boolean showType) {
        String type = showType ? " (" + (value != null ? value.getClass().getSimpleName() : "null") + ")" : "";
        System.out.println("  " + label + ": " + value + type);
    }

    // Тестовые классы
    public static class User {
        public String name;
        public int age;
        public String email;
        public List<String> tags;
    }

    public static class Father {
        public String name;
        public int age;
        public Kid kid;
    }

    public static class Kid {
        public String name;
        public int age;
    }
}