package ru.lab.json;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== тесты ===\n");

        User user = new User();
        user.name = "Victoria";
        user.age = 20;           
        user.rank = 100500L;     
        user.tags = new String[]{"java", "lab", "parser"}; 
        user.scores = List.of(95, 100); 
        user.address = new Address();
        user.address.city = "St. Petersburg";
        
        String json = Json.toJson(user);
        System.out.println("1. Object -> JSON:");
        System.out.println(json + "\n");

        System.out.println("2. JSON -> Map:");
        Map<String, Object> map = Json.fromJsonAsMap(json);
        System.out.println("Map keys: " + map.keySet());
        System.out.println("Value for 'name': " + map.get("name") + "\n");

        System.out.println("3. JSON -> Specified Class:");
        User restored = Json.fromJson(json, User.class);
        System.out.println("Restored User: " + restored.name + ", City: " + restored.address.city + "\n");

        System.out.println("4. Parsing Array/Primitives at root level:");
        Object arrayRoot = Json.fromJson("[6, 2, 6]");
        Object stringRoot = Json.fromJson("\"json parser\"");
        System.out.println("Array root: " + arrayRoot);
        System.out.println("String root: " + stringRoot);
    }
}