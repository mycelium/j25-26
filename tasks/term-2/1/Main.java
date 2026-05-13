import json.Json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {

    public static class Animal {
        private String species;
        private int legs;
        public Animal() {}
        @Override public String toString() { return "Animal{species=" + species + ", legs=" + legs + "}"; }
    }

    public static class Pet extends Animal {
        private String nickname;
        public Pet() {}
        @Override public String toString() { return "Pet{nickname=" + nickname + ", " + super.toString() + "}"; }
    }

    public static class Classroom {
        private String name;
        private String[] subjects;
        private List<Integer> scores;
        public Classroom() {}
        @Override public String toString() {
            return "Classroom{name=" + name + ", subjects=" + Arrays.toString(subjects) + ", scores=" + scores + "}";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Serialization ===");
        System.out.println(Json.toJson(42));
        System.out.println(Json.toJson("hello"));
        System.out.println(Json.toJson(null));
        System.out.println(Json.toJson(true));

        System.out.println("\n=== Escape sequences ===");
        System.out.println(Json.toJson("line1\nline2"));
        System.out.println(Json.toJson("tab\there"));
        System.out.println(Json.toJson("quote\"inside"));

        String escJson = """
                {"msg":"say \\"hello\\"","path":"c:\\\\temp"}""";
        System.out.println("Parse escaped: " + Json.parseToMap(escJson));

        System.out.println("\n=== Number types ===");
        Map<String, Object> nums = Json.parseToMap("""
                {"small":42,"big":3000000000,"decimal":3.14}""");
        for (var e : nums.entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue() + " (" + e.getValue().getClass().getSimpleName() + ")");
        }

        System.out.println("\n=== Array fields ===");
        String classroomJson = """
                {"name":"Math","subjects":["algebra","geometry"],"scores":[90,85,78]}""";
        Classroom c = Json.parse(classroomJson, Classroom.class);
        System.out.println("Deserialized: " + c);
        System.out.println("Re-serialized: " + Json.toJson(c));

        System.out.println("\n=== Java array serialization ===");
        int[] numbers = {1, 2, 3};
        System.out.println("int[]: " + Json.toJson(numbers));
        String[] words = {"one", "two"};
        System.out.println("String[]: " + Json.toJson(words));

        System.out.println("\n=== Inheritance ===");
        String petJson = """
                {"nickname":"Rex","species":"Dog","legs":4}""";
        Pet pet = Json.parse(petJson, Pet.class);
        System.out.println("Deserialized: " + pet);
        System.out.println("Re-serialized: " + Json.toJson(pet));

        System.out.println("\n=== Round-trip ===");
        String input = """
                {"name":"Ivan","age":30,"active":true}""";
        Map<String, Object> map = Json.parseToMap(input);
        System.out.println("Map: " + map);
        System.out.println("Back: " + Json.toJson(map));
    }
}
