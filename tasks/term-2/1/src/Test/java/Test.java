import jsonlib.Json;
import jsonlib.JsonType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        Json json = Json.builder().build();

        String source = """
                {
                  "name": "Aleksei",
                  "age": 20,
                  "skills": ["Java", "SQL", "Docker", "Python"],
                  "meta": {
                    "active": true,
                    "scores": [10, 20, 30]
                  }
                }
                """;

        Map<String, Object> root = json.parseObject(source);

        check("Aleksei".equals(root.get("name")), "name mismatch");
        check(((Number) root.get("age")).intValue() == 20, "age mismatch");

        List<?> skills = (List<?>) root.get("skills");
        check(skills.size() == 4, "skills size mismatch");
        check("Java".equals(skills.get(0)), "skills[0] mismatch");
        check("Docker".equals(skills.get(2)), "skills[2] mismatch");

        Map<?, ?> meta = (Map<?, ?>) root.get("meta");
        check(Boolean.TRUE.equals(meta.get("active")), "meta.active mismatch");

        List<?> scores = (List<?>) meta.get("scores");
        check(((Number) scores.get(1)).intValue() == 20, "scores[1] mismatch");

        String escapedJson = """
                {
                  "text": "quote: \\" slash: \\\\ line: \\n tab: \\t unicode: \\u0041"
                }
                """;
        Map<String, Object> escaped = json.parseObject(escapedJson);
        check("quote: \" slash: \\ line: \n tab: \t unicode: A".equals(escaped.get("text")),
                "escape parsing mismatch");

        String serialized = json.toJson("first line\nsecond line\t\"quoted\"");
        check("\"first line\\nsecond line\\t\\\"quoted\\\"\"".equals(serialized),
                "escape serialization mismatch");

        User user = json.fromJson("""
                {
                  "name": "Shinji Ikari",
                  "age": 14,
                  "active": true,
                  "address": {"city": "Tokyo-3"},
                  "tags": ["student", "java"],
                  "marks": [5, 4, 5]
                }
                """, User.class);
        check("Shinji Ikari".equals(user.name), "user.name mismatch");
        check(user.age == 14, "user.age mismatch");
        check(user.active, "user.active mismatch");
        check("Tokyo-3".equals(user.address.city), "nested object mismatch");
        check(user.tags.size() == 2 && "java".equals(user.tags.get(1)), "collection mismatch");
        check(Arrays.equals(new int[] {5, 4, 5}, user.marks), "array mismatch");

        List<User> users = json.fromJson("""
                [
                  {"name": "A", "age": 20},
                  {"name": "B", "age": 21}
                ]
                """, JsonType.listOf(User.class));
        check(users.size() == 2, "typed list size mismatch");
        check("B".equals(users.get(1).name), "typed list value mismatch");

        Map<Integer, String> numberKeys = json.fromJson("""
                {"1": "one", "2": "two"}
                """, JsonType.mapOf(Integer.class, String.class));
        check("two".equals(numberKeys.get(2)), "map key conversion mismatch");

        check(json.fromJson("null", long.class) == 0L, "primitive null long mismatch");
        check(json.fromJson("null", double.class) == 0.0d, "primitive null double mismatch");
        check(json.toJson(user).contains("\"address\""), "object serialization mismatch");

        System.out.println("Test OK");
    }

    public static class User {
        public String name;
        public int age;
        public boolean active;
        public Address address;
        public List<String> tags;
        public int[] marks;
    }

    public static class Address {
        public String city;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
