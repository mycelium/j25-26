package ru.example.json;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JsonTest {

    @Test
    void testParseToMap() {
        String json = "{\"name\":\"Anna\",\"age\":20}";

        Map<String, Object> map = Json.parseToMap(json);

        assertEquals("Anna", map.get("name"));
        assertEquals(20.0, map.get("age"));
    }

    @Test
    void testToObjectSimple() {
        String json = "{\"name\":\"Anna\",\"age\":20}";

        Person p = Json.toObject(json, Person.class);

        assertEquals("Anna", p.name);
        assertEquals(20, p.age);
    }

    @Test
    void testNestedObject() {
        String json = "{\"name\":\"Anna\",\"address\":{\"city\":\"Berlin\"}}";

        Person p = Json.toObject(json, Person.class);

        assertNotNull(p.address);
        assertEquals("Berlin", p.address.city);
    }

    @Test
    void testArrayMapping() {
        String json = "{\"name\":\"Anna\",\"scores\":[1,2,3]}";

        Person p = Json.toObject(json, Person.class);

        assertArrayEquals(new int[]{1, 2, 3}, p.scores);
    }

    @Test
    void testSerializeObject() {
        Person p = new Person();
        p.name = "Anna";
        p.age = 20;

        String json = Json.stringify(p);

        assertTrue(json.contains("\"name\":\"Anna\""));
        assertTrue(json.contains("\"age\":20"));
    }

    @Test
    void testSerializeMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("x", 10);
        map.put("y", "test");

        String json = Json.stringify(map);

        assertTrue(json.contains("\"x\":10"));
        assertTrue(json.contains("\"y\":\"test\""));
    }

    @Test
    void testCollections() {
        List<Integer> list = Arrays.asList(1, 2, 3);

        String json = Json.stringify(list);

        assertEquals("[1,2,3]", json);
    }

    @Test
    void testNullValue() {
        Person p = new Person();
        p.name = null;

        String json = Json.stringify(p);

        assertTrue(json.contains("null"));
    }

    static class Person {
        public String name;
        public int age;
        public Address address;
        public int[] scores;
    }

    static class Address {
        public String city;
    }
}