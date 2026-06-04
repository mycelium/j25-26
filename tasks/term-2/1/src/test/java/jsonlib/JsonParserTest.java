package jsonlib;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JsonParserTest {

    @Test
    @DisplayName("Parse empty object")
    void testEmptyObject() {
        Object result = Json.parse("{}");
        assertTrue(result instanceof Map);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    @DisplayName("Parse simple object")
    void testSimpleObject() {
        Map<String, Object> map = Json.parseToMap("{\"name\":\"John\",\"age\":30}");
        assertEquals("John", map.get("name"));
        assertEquals(30L, map.get("age"));
    }

    @Test
    @DisplayName("Parse nested object")
    void testNestedObject() {
        String json = "{\"person\":{\"name\":\"Alice\",\"age\":25}}";
        Map<String, Object> map = Json.parseToMap(json);
        assertTrue(map.get("person") instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> person = (Map<String, Object>) map.get("person");
        assertEquals("Alice", person.get("name"));
    }

    @Test
    @DisplayName("Parse array")
    void testArray() {
        String json = "[1, 2, 3]";
        Object result = Json.parse(json);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
        assertEquals(1L, list.get(0));
    }

    @Test
    @DisplayName("Parse mixed array")
    void testMixedArray() {
        String json = "[\"text\", 42, true, null]";
        List<?> list = (List<?>) Json.parse(json);
        assertEquals("text", list.get(0));
        assertEquals(42L, list.get(1));
        assertEquals(true, list.get(2));
        assertNull(list.get(3));
    }

    @Test
    @DisplayName("Parse string with escape sequences")
    void testEscapedString() {
        Object result = Json.parse("\"Hello\\nWorld\"");
        assertEquals("Hello\nWorld", result);
    }

    @Test
    @DisplayName("Parse unicode escape")
    void testUnicodeEscape() {
        Object result = Json.parse("\"Hello\\u0020World\"");
        assertEquals("Hello World", result);
    }

    @Test
    @DisplayName("Parse numbers")
    void testNumbers() {
        assertEquals(42L, Json.parse("42"));
        assertEquals(3.14, Json.parse("3.14"));
        assertEquals(-17L, Json.parse("-17"));
        assertEquals(1.0E10, Json.parse("1e10"));
    }

    @Test
    @DisplayName("Parse literals")
    void testLiterals() {
        assertEquals(true, Json.parse("true"));
        assertEquals(false, Json.parse("false"));
        assertNull(Json.parse("null"));
    }

    @Test
    @DisplayName("Error on invalid JSON")
    void testInvalidJson() {
        assertThrows(JsonParseException.class, () -> Json.parse("{invalid}"));
        assertThrows(JsonParseException.class, () -> Json.parse("[1, 2,]"));
    }

    @Test
    @DisplayName("Error on empty string")
    void testEmptyString() {
        assertThrows(JsonParseException.class, () -> Json.parse(""));
        assertThrows(JsonParseException.class, () -> Json.parse("   "));
    }

    @Test
    @DisplayName("Parse complex JSON")
    void testComplexJson() {
        String json = """
            {
                "users": [
                    {"id": 1, "name": "Alice"},
                    {"id": 2, "name": "Bob"}
                ],
                "total": 2,
                "active": true
            }
            """;
        Map<String, Object> map = Json.parseToMap(json);
        assertEquals(2L, map.get("total"));
        assertEquals(true, map.get("active"));
        assertTrue(map.get("users") instanceof List);
        assertEquals(2, ((List<?>) map.get("users")).size());
    }
}