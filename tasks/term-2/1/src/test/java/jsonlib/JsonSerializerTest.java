package jsonlib;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSerializerTest {

    @Test
    @DisplayName("Serialize null")
    void testNull() {
        assertEquals("null", Json.toJson(null));
    }

    @Test
    @DisplayName("Serialize string")
    void testString() {
        assertEquals("\"hello\"", Json.toJson("hello"));
    }

    @Test
    @DisplayName("Serialize string with quotes")
    void testStringWithQuotes() {
        assertEquals("\"he said \\\"hi\\\"\"", Json.toJson("he said \"hi\""));
    }

    @Test
    @DisplayName("Serialize numbers")
    void testNumbers() {
        assertEquals("42", Json.toJson(42));
        assertEquals("3.14", Json.toJson(3.14));
        assertEquals("true", Json.toJson(true));
        assertEquals("false", Json.toJson(false));
    }

    @Test
    @DisplayName("Serialize array")
    void testArray() {
        int[] arr = {1, 2, 3};
        assertEquals("[1,2,3]", Json.toJson(arr));
    }

    @Test
    @DisplayName("Serialize collection")
    void testCollection() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("[\"a\",\"b\",\"c\"]", Json.toJson(list));
    }

    @Test
    @DisplayName("Serialize Map")
    void testMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "John");
        map.put("age", 30);
        String json = Json.toJson(map);
        assertTrue(json.contains("\"name\":\"John\""));
        assertTrue(json.contains("\"age\":30"));
    }

    @Test
    @DisplayName("Serialize object")
    void testObject() {
        TestPerson person = new TestPerson();
        person.name = "Alice";
        person.age = 25;
        String json = Json.toJson(person);
        assertTrue(json.contains("\"name\":\"Alice\""));
        assertTrue(json.contains("\"age\":25"));
    }

    @Test
    @DisplayName("Pretty print")
    void testPrettyPrint() {
        TestPerson person = new TestPerson();
        person.name = "Bob";
        person.age = 30;
        String pretty = Json.toJson(person, true);
        assertTrue(pretty.contains("\n"));
        assertTrue(pretty.contains("  "));
    }

    @Test
    @DisplayName("Round-trip: object -> JSON -> Map -> JSON")
    void testRoundTrip() {
        Map<String, Object> original = new LinkedHashMap<>();
        original.put("x", 10);
        original.put("y", "text");
        String json1 = Json.toJson(original);
        Map<String, Object> parsed = Json.parseToMap(json1);
        String json2 = Json.toJson(parsed);
        assertEquals(json1, json2);
    }

    public static class TestPerson {
        public String name;
        public int age;
    }
}