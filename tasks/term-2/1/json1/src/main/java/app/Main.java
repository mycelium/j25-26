package app;

import jsonlab.Json;
import java.util.*;

public class Main {

    static class User {
        public String name;
        public int age;
        public List<String> tags;
        public Address addr;

        @Override
        public String toString() {
            return "User{name='%s', age=%d, tags=%s, addr=%s}".formatted(name, age, tags, addr);
        }
    }

    static class Address {
        public String city;
        public String street;

        @Override
        public String toString() {
            return "Address{city='%s', st='%s'}".formatted(city, street);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== JSON Library Demo ===\n");

        // 1. Базовый парсинг — компактный JSON без переносов
        String objJson = "{\"name\":\"Alice\",\"age\":30,\"active\":true,\"score\":95.5,\"tags\":[\"java\",\"json\"]}";
        Map<String, Object> map = Json.decodeMap(objJson);
        System.out.println("1. Map parse: name=" + map.get("name") + ", age=" + map.get("age") +
                " (" + map.get("age").getClass().getSimpleName() + ")");

        // 2. Десериализация в класс
        String userJson = "{\"name\":\"Bob\",\"age\":25,\"tags\":[\"dev\"],\"addr\":{\"city\":\"MSK\",\"street\":\"Tver\"}}";
        User u = Json.decodeTo(userJson, User.class);
        System.out.println("2. Class bind: " + u);

        // 3. Сериализация
        User newUser = new User();
        newUser.name = "Carol";
        newUser.age = 28;
        newUser.tags = List.of("lead");
        newUser.addr = new Address();
        newUser.addr.city = "SPb";
        newUser.addr.street = "Nevsky";

        String out = Json.encode(newUser);
        System.out.println("3. Serialize: " + out);

        // 4. Round-trip
        User restored = Json.decodeTo(out, User.class);
        System.out.println("4. Round-trip match: " +
                (newUser.name.equals(restored.name) && newUser.age == restored.age));


        String escJson = "{\"msg\":\"Hi \\u0041\\nTab\\tQuote\\\"\"}";
        Map<String, Object> p = Json.decodeMap(escJson);
        System.out.println("5. Escapes: " + p.get("msg"));

        // 6. Ошибки
        try {
            Json.decodeMap("{broken}");
        } catch (Exception e) {
            System.out.println("6. Error caught: " + e.getClass().getSimpleName());
        }

        System.out.println("\n=== Done ===");
    }
}