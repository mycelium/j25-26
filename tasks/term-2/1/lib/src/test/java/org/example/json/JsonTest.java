package org.example.json;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JsonTest {

    @Test
    void testParseString() {
        assertEquals("hello", Json.parse("\"hello\""));
    }

    @Test
    void testParseNumber() {
        assertEquals(42L, Json.parse("42"));
        assertEquals(3.14, Json.parse("3.14"));
    }

    @Test
    void testParseBoolean() {
        assertEquals(true, Json.parse("true"));
        assertEquals(false, Json.parse("false"));
    }

    @Test
    void testParseNull() {
        assertNull(Json.parse("null"));
    }

    @Test
    void testParseObject() {
        Map<String, Object> map = Json.parseToMap("{\"name\":\"Alice\",\"age\":30}");
        assertEquals("Alice", map.get("name"));
        assertEquals(30L, map.get("age"));
    }

    @Test
    void testParseArray() {
        List<?> list = (List<?>) Json.parse("[1,2,3]");
        assertEquals(3, list.size());
        assertEquals(1L, list.get(0));
    }

    @Test
    void testSerializeObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "Bob");
        map.put("age", 25);
        String json = Json.toJson(map);
        assertTrue(json.contains("\"name\":\"Bob\""));
        assertTrue(json.contains("\"age\":25"));
    }

    @Test
    void testSerializeNull() {
        assertEquals("null", Json.toJson(null));
    }

    @Test
    void testParseToClass() {
        String json = "{\"name\":\"Charlie\",\"age\":20}";
        Person p = Json.parse(json, Person.class);
        assertEquals("Charlie", p.name);
        assertEquals(20, p.age);
    }

    @Test
    void testSerializeClass() {
        Person p = new Person();
        p.name = "Dave";
        p.age = 35;
        String json = Json.toJson(p);
        assertTrue(json.contains("\"name\":\"Dave\""));
        assertTrue(json.contains("\"age\":35"));
    }

    @Test
    void testNestedObject() {
        String json = "{\"person\":{\"name\":\"Eve\",\"age\":28}}";
        Map<String, Object> map = Json.parseToMap(json);
        Map<?, ?> person = (Map<?, ?>) map.get("person");
        assertEquals("Eve", person.get("name"));
    }

    @Test
    void testStringEscaping() {
        String json = "\"hello\\nworld\"";
        assertEquals("hello\nworld", Json.parse(json));
    }

    static class Person {
        String name;
        int age;
    }
}
