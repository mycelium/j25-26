import jsonparser.Json;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    // ----- functionality examples -----
    private static void runTests(){
        // ── 1. Parse JSON to Object (generic) ──────────────────────────────
        System.out.println("=== 1. JSON → Object ===");
        Object arr = Json.parse("[\"string\", 2, false]");
        System.out.println(arr);   // [string, 2, false]

        Object num = Json.parse("42");
        System.out.println(num);   // 42

        // ── 2. Parse JSON to Map<String, Object> ───────────────────────────
        System.out.println("\n=== 2. JSON → Map<String, Object> ===");
        Map<String, Object> map = Json.parseToMap("""
                {
                    "str": "str",
                    "integer": 1,
                    "bool": true,
                    "inner": {
                        "array": ["string", 2, false],
                        "nullable": null
                    }
                }
                """);
        System.out.println(map);
        System.out.println("str    = " + map.get("str"));
        System.out.println("inner  = " + map.get("inner"));

        // ── 3. Parse JSON to specified class ───────────────────────────────
        System.out.println("\n=== 3. JSON → Person.class ===");
        String personJson = """
                {
                    "name":    "Alice",
                    "age":     30,
                    "active":  true,
                    "score":   9.5,
                    "hobbies": ["reading", "coding"],
                    "address": {
                        "street": "123 Main St",
                        "city":   "Springfield",
                        "zip":    12345
                    },
                    "tags":    ["java", "dev", 42],
                    "nullable": null
                }
                """;
        Person person = Json.parse(personJson, Person.class);
        System.out.println(person);
        System.out.println("address.city = " + person.address.city);
        System.out.println("hobbies[1]   = " + person.hobbies[1]);

        // ── 4. Convert Java object to JSON string ──────────────────────────
        System.out.println("\n=== 4. Object → JSON ===");
        System.out.println(Json.stringify(person));

        // ── 5. Serialize arrays ────────────────────────────────────────────
        System.out.println("\n=== 5. Arrays ===");
        int[]    intArr = {1, 2, 3};
        double[] dblArr = {1.1, 2.2, 3.3};
        System.out.println(Json.stringify(intArr));
        System.out.println(Json.stringify(dblArr));
        System.out.println(Json.stringify(new String[]{"a", "b", null}));

        // ── 6. Serialize collections ───────────────────────────────────────
        System.out.println("\n=== 6. Collections ===");
        List<Object> mixed = List.of("hello", 42, true);
        System.out.println(Json.stringify(mixed));

        // ── 7. Serialize map ───────────────────────────────────────────────
        System.out.println("\n=== 7. Map ===");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("x", 1);
        data.put("y", 2.5);
        data.put("label", "origin");
        data.put("active", true);
        data.put("nothing", null);
        System.out.println(Json.stringify(data));

        // ── 8. Nulls and primitives ────────────────────────────────────────
        System.out.println("\n=== 8. Primitives / nulls ===");
        System.out.println(Json.stringify(null));
        System.out.println(Json.stringify(true));
        System.out.println(Json.stringify(3.14));
        System.out.println(Json.stringify("hello \"world\"\nnewline"));

        // ── 9. Round-trip ──────────────────────────────────────────────────
        System.out.println("\n=== 9. Round-trip ===");
        String json1 = Json.stringify(person);
        Person person2 = Json.parse(json1, Person.class);
        System.out.println("Names match: " + person.name.equals(person2.name));
        System.out.println("Cities match: " + person.address.city.equals(person2.address.city));
    }

    // ----- example classes -----

    static class Address {
        String street;
        String city;
        int    zip;

        @Override public String toString() {
            return street + ", " + city + " " + zip;
        }
    }

    static class Person {
        String   name;
        int      age;
        boolean  active;
        double   score;
        String[] hobbies;   // array field
        Address  address;   // nested object
        List<Object> tags;  // collection field
        String   nullable;  // null value

        @Override public String toString() {
            return "Person{name=" + name + ", age=" + age +
                   ", active=" + active + ", score=" + score +
                   ", hobbies=" + Arrays.toString(hobbies) +
                   ", address=(" + address + ")" +
                   ", tags=" + tags +
                   ", nullable=" + nullable + "}";
        }
    }

    public static void main(String[] args) {

        runTests();

    }
}
