import json.Json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static class Person {
        private String name;
        private int age;
        public Person() {}
        @Override public String toString() { return "Person{name=" + name + ", age=" + age + "}"; }
    }

    public static void main(String[] args) {
        System.out.println("--- Phase 1: Basic Serialization ---");
        System.out.println("Number: " + Json.toJson(42));
        System.out.println("String: " + Json.toJson("hello"));
        System.out.println("Null:   " + Json.toJson(null));

        System.out.println("\n--- Phase 2: Full Round-Trip (Map) ---");
        String jsonInput = "{\"name\":\"Ivan\",\"age\":30,\"isStudent\":true}";
        System.out.println("Original JSON:  " + jsonInput);

        // String -> Map
        Map<String, Object> map = Json.parseToMap(jsonInput);
        System.out.println("Parsed into Map: " + map);

        // Map -> String
        String jsonOutput = Json.toJson(map);
        System.out.println("Back to String:  " + jsonOutput);

        System.out.println("\n--- Phase 3: Reflection Mapping ---");
        // String -> Custom Class
        String personJson = "{\"name\":\"Anna\",\"age\":25,\"isStudent\":false}";
        Person p = Json.parse(personJson, Person.class);
        System.out.println("Parsed into Reflection Mapping: " + p);

        // Reflection Mapping -> String
        System.out.println("Reflection Mapping Serialized:  " + Json.toJson(p));

        System.out.println("\n--- Phase 4: Complex Nested Structures ---");
        Map<String, Object> complex = new HashMap<>();
        complex.put("user", p);
        complex.put("tags", Arrays.asList("java", "json", "pro"));

        String complexJson = Json.toJson(complex);
        System.out.println("Complex Structure: " + complexJson);
    }
}
