import jsonparser.Json;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    static class Address {
        String city;
        String street;
        int zip;

        @Override
        public String toString() {
            return street + ", " + city + " " + zip;
        }
    }

    static class Person {
        String name;
        int age;
        boolean active;
        double score;
        String[] hobbies;
        Address address;
        List<Object> tags;
        String nullable;

        @Override
        public String toString() {
            return "Person{name=" + name +
                    ", age=" + age +
                    ", active=" + active +
                    ", score=" + score +
                    ", hobbies=" + Arrays.toString(hobbies) +
                    ", address=(" + address + ")" +
                    ", tags=" + tags +
                    ", nullable=" + nullable + "}";
        }
    }


    public static void main(String[] args) {

        // 1. JSON → Object (обобщённый)
        System.out.println("=== 1. JSON → Object ===");
        Object list = Json.parse("[\"hello\", 42, true, null]");
        System.out.println(list);

        Object num = Json.parse("3.14");
        System.out.println(num);

        // 2. JSON → Map<String, Object>
        System.out.println("\n=== 2. JSON → Map<String, Object> ===");
        Map<String, Object> map = Json.parseToMap("""
                {
                    "name": "Alice",
                    "age": 30,
                    "active": true,
                    "inner": {
                        "items": [1, 2, 3],
                        "empty": null
                    }
                }
                """);
        System.out.println(map);
        System.out.println("name  = " + map.get("name"));
        System.out.println("inner = " + map.get("inner"));

        // 3. JSON → конкретный класс
        System.out.println("\n=== 3. JSON → Person.class ===");
        String personJson = """
                {
                    "name":    "Bob",
                    "age":     25,
                    "active":  false,
                    "score":   7.8,
                    "hobbies": ["gaming", "hiking"],
                    "address": {
                        "city":   "Riga",
                        "street": "Brivibas iela 1",
                        "zip":    1010
                    },
                    "tags":    ["java", "student", 2024],
                    "nullable": null
                }
                """;
        Person person = Json.parse(personJson, Person.class);
        System.out.println(person);
        System.out.println("city      = " + person.address.city);
        System.out.println("hobbies[0]= " + person.hobbies[0]);

        // 4. Java-объект → JSON-строка
        System.out.println("\n=== 4. Object → JSON ===");
        System.out.println(Json.toJson(person));

        // 5. Массивы примитивов
        System.out.println("\n=== 5. Примитивные массивы ===");
        System.out.println(Json.toJson(new int[]{1, 2, 3}));
        System.out.println(Json.toJson(new double[]{1.1, 2.2, 3.3}));
        System.out.println(Json.toJson(new boolean[]{true, false, true}));
        System.out.println(Json.toJson(new String[]{"a", "b", null}));

        // 6. Коллекции
        System.out.println("\n=== 6. Collection ===");
        List<Object> mixed = List.of("text", 100, false);
        System.out.println(Json.toJson(mixed));

        // 7. Map
        System.out.println("\n=== 7. Map ===");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("x", 10);
        data.put("y", 2.5);
        data.put("label", "point");
        data.put("visible", true);
        data.put("extra", null);
        System.out.println(Json.toJson(data));

        // 8. Примитивы и null
        System.out.println("\n=== 8. Примитивы и null ===");
        System.out.println(Json.toJson(null));
        System.out.println(Json.toJson(42));
        System.out.println(Json.toJson(true));
        System.out.println(Json.toJson("hello \"world\"\nnewline"));

        // 9. Round-trip: объект → JSON → объект
        System.out.println("\n=== 9. Round-trip ===");
        String json = Json.toJson(person);
        Person copy = Json.parse(json, Person.class);
        System.out.println("Имена совпадают: " + person.name.equals(copy.name));
        System.out.println("Города совпадают: " + person.address.city.equals(copy.address.city));
        System.out.println("Хобби[1] совпадают: " + person.hobbies[1].equals(copy.hobbies[1]));
    }
}
