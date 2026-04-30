package com.jsonparser;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== JSON Parser ===\n");

        System.out.println("1. JSON to Map:");
        String json1 = "{\"name\":\"Polina\",\"age\":20,\"active\":true}";
        Map<String, Object> map = JsonParser.fromJsonToMap(json1);
        System.out.println("  name: " + map.get("name"));
        System.out.println("  age: " + map.get("age"));
        System.out.println("  active: " + map.get("active"));

        System.out.println("\n2. JSON to Java Object:");
        System.out.println("  " + JsonParser.fromJson("42"));
        System.out.println("  " + JsonParser.fromJson("\"hello\""));
        System.out.println("  " + JsonParser.fromJson("[1,2,3]"));
        System.out.println("  " + JsonParser.fromJson("{\"x\":1}"));

        System.out.println("\n3. JSON to object:");
        String json2 = "{\"name\":\"Alice\",\"age\":25,\"active\":false}";
        Person person = JsonParser.fromJsonToObject(json2, Person.class);
        System.out.println("  name: " + person.name);
        System.out.println("  age: " + person.age);
        System.out.println("  active: " + person.active);

        System.out.println("\n4. Object to JSON:");
        Person person2 = new Person();
        person2.name = "Bob";
        person2.age = 30;
        person2.active = true;
        String json3 = JsonParser.toJson(person2);
        System.out.println("  " + json3);

        System.out.println("\n5. Arrays:");
        String json4 = "{\"name\":\"Team\",\"scores\":[95,87,92]}";
        Map<String, Object> map2 = JsonParser.fromJsonToMap(json4);
        System.out.println("  name: " + map2.get("name"));
        System.out.println("  scores: " + map2.get("scores"));

        System.out.println("\n6. Null values:");
        String json5 = "{\"name\":null,\"age\":null}";
        Map<String, Object> map3 = JsonParser.fromJsonToMap(json5);
        System.out.println("  name: " + map3.get("name"));
        System.out.println("  age: " + map3.get("age"));

        System.out.println("\n7. Nested object in Map:");
        String json7 = "{\"user\":{\"name\":\"Polina\",\"age\":20}}";
        Map<String, Object> map7 = JsonParser.fromJsonToMap(json7);
        System.out.println("  user: " + map7.get("user"));
    }

    public static class Person {
        public String name;
        public int age;
        public boolean active;
    }
}
