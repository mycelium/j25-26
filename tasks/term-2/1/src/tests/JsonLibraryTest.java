package tests;

import tokenizer.Json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonLibraryTest {
    public static void main(String[] args) {
        testParseToMap();
        System.out.println("[PASS] parse to map");

        testParseToObjectTree();
        System.out.println("[PASS] parse to object tree");

        testMapToClass();
        System.out.println("[PASS] map to class");

        testStringifyObject();
        System.out.println("[PASS] stringify object");

        testRoundTrip();
        System.out.println("[PASS] round trip");

        System.out.println("All tests passed");
    }

    private static void testParseToMap() {
        String json = """
            {
              "name": "Ivan",
              "age": 20,
              "active": true,
              "tags": ["java", "go"]
            }
            """;
        Map<String, Object> map = Json.parseToMap(json);

        expectEquals("Ivan", map.get("name"), "name");
        expectEquals(20, map.get("age"), "age");
        expectEquals(true, map.get("active"), "active");
        expectEquals(Arrays.asList("java", "go"), map.get("tags"), "tags");
    }

    @SuppressWarnings("unchecked")
    private static void testParseToObjectTree() {
        String json = """
            {
              "arr": [1, 2, 3],
              "obj": {
                "k": "v"
              },
              "n": null
            }
            """;
        Object parsed = Json.parse(json);
        if (!(parsed instanceof Map)) {
            throw new AssertionError("root must be map");
        }
        Map<String, Object> root = (Map<String, Object>) parsed;
        expectEquals(Arrays.asList(1, 2, 3), root.get("arr"), "array");
        expectEquals(null, root.get("n"), "null");

        Map<String, Object> obj = (Map<String, Object>) root.get("obj");
        expectEquals("v", obj.get("k"), "nested value");
    }

    private static void testMapToClass() {
        String json = """
            {
              "name":"Ivan",
              "age":20,
              "active":true,
              "scores":[5,4,5],
              "codes":[101,102],
              "address":{"city":"Moscow","zip":"101000"}
            }
            """;

        User user = Json.parse(json, User.class);
        expectEquals("Ivan", user.name, "user.name");
        expectEquals(20, user.age, "user.age");
        expectEquals(true, user.active, "user.active");
        expectEquals(Arrays.asList(5, 4, 5), user.scores, "user.scores");
        expectEquals(2, user.codes.length, "user.codes.length");
        expectEquals(101, user.codes[0], "user.codes[0]");
        expectEquals("Moscow", user.address.city, "user.address.city");
    }

    private static void testStringifyObject() {
        User user = new User();
        user.name = "Anna";
        user.age = 21;
        user.active = false;
        user.scores = Arrays.asList(1, 2);
        user.codes = new int[]{10, 11};
        user.address = new Address();
        user.address.city = "Kazan";
        user.address.zip = "420000";

        String json = Json.stringify(user);
        Map<String, Object> map = Json.parseToMap(json);

        expectEquals("Anna", map.get("name"), "serialized.name");
        expectEquals(21, map.get("age"), "serialized.age");
        expectEquals(false, map.get("active"), "serialized.active");
    }

    private static void testRoundTrip() {
        String json = """
            {
              "name": "Nina",
              "age": 30,
              "active": true,
              "scores": [3, 4],
              "codes": [7],
              "address": {
                "city": "SPB",
                "zip": "190000"
              }
            }
            """;
        User user = Json.parse(json, User.class);
        String out = Json.stringify(user);
        User user2 = Json.parse(out, User.class);

        expectEquals(user.name, user2.name, "roundtrip.name");
        expectEquals(user.age, user2.age, "roundtrip.age");
        expectEquals(user.address.city, user2.address.city, "roundtrip.city");
    }

    private static void expectEquals(Object expected, Object actual, String label) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw new AssertionError(label + ": expected=" + expected + ", actual=" + actual);
    }

    public static class User {
        public String name;
        public int age;
        public boolean active;
        public List<Integer> scores;
        public int[] codes;
        public Address address;
    }

    public static class Address {
        public String city;
        public String zip;
    }
}
