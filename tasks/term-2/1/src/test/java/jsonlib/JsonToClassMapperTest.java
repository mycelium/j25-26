package jsonlib;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JsonToClassMapperTest {

    @Test
    @DisplayName("Map simple class")
    void testSimpleClass() {
        String json = "{\"title\":\"Java\",\"pages\":500}";
        Book book = Json.parse(json, Book.class);
        assertEquals("Java", book.title);
        assertEquals(500, book.pages);
    }

    @Test
    @DisplayName("Map with nested object")
    void testNestedObject() {
        String json = "{\"title\":\"Bible\",\"author\":{\"name\":\"God\",\"age\":999}}";
        BookWithAuthor book = Json.parse(json, BookWithAuthor.class);
        assertEquals("Bible", book.title);
        assertNotNull(book.author);
        assertEquals("God", book.author.name);
        assertEquals(999, book.author.age);
    }

    @Test
    @DisplayName("Map array field")
    void testArrayField() {
        String json = "{\"name\":\"Shop\",\"tags\":[\"food\",\"drinks\"]}";
        Shop shop = Json.parse(json, Shop.class);
        assertEquals("Shop", shop.name);
        assertArrayEquals(new String[]{"food", "drinks"}, shop.tags);
    }

    @Test
    @DisplayName("Map collection field")
    void testCollectionField() {
        String json = "{\"title\":\"Notes\",\"tags\":[\"java\",\"json\"]}";
        Note note = Json.parse(json, Note.class);
        assertEquals("Notes", note.title);
        assertEquals(Arrays.asList("java", "json"), note.tags);
    }

    @Test
    @DisplayName("Ignore extra JSON fields")
    void testIgnoreExtraFields() {
        String json = "{\"title\":\"Test\",\"extra\":\"ignored\",\"pages\":100}";
        Book book = Json.parse(json, Book.class);
        assertEquals("Test", book.title);
        assertEquals(100, book.pages);
    }

    @Test
    @DisplayName("Missing fields (default values)")
    void testMissingFields() {
        String json = "{\"title\":\"Only\"}";
        Book book = Json.parse(json, Book.class);
        assertEquals("Only", book.title);
        assertEquals(0, book.pages);
    }

    // Test classes
    public static class Book {
        public String title;
        public int pages;
    }

    public static class Author {
        public String name;
        public int age;
    }

    public static class BookWithAuthor {
        public String title;
        public Author author;
    }

    public static class Shop {
        public String name;
        public String[] tags;
    }

    public static class Note {
        public String title;
        public List<String> tags;
    }
}