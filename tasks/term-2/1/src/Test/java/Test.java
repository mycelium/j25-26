import jsonlib.Json;

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

        System.out.println("Test OK");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
