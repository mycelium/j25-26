import jsonlib.Json;
import java.util.*;

public class Main {
    public static class Book {
        private String title;
        private int year;
        private List<String> genres;
        private Author author;
        private List<String> characters;

        public Book() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public List<String> getGenres() { return genres; }
        public void setGenres(List<String> genres) { this.genres = genres; }
        public Author getAuthor() { return author; }
        public void setAuthor(Author author) { this.author = author; }
        public List<String> getCharacters() { return characters; }
        public void setCharacters(List<String> characters) { this.characters = characters; }

        @Override
        public String toString() {
            return String.format("Book{title='%s', year=%d, genres=%s, author=%s, characters=%s}",
                    title, year, genres, author, characters);
        }
    }

    public static class Author {
        private String name;
        private int birthYear;

        public Author() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getBirthYear() { return birthYear; }
        public void setBirthYear(int birthYear) { this.birthYear = birthYear; }

        @Override
        public String toString() {
            return String.format("Author{name='%s', birthYear=%d}", name, birthYear);
        }
    }

    public static void main(String[] args) {
        String jsonBook = """
            {
                "title": "The Hobbit",
                "year": 1937,
                "genres": ["Fantasy", "Adventure"],
                "author": {
                    "name": "J.R.R. Tolkien",
                    "birthYear": 1892
                },
                "characters": ["Bilbo", "Gandalf", "Thorin"]
            }
            """;

        Book book = Json.fromJson(jsonBook, Book.class);
        System.out.println("Parsed book: " + book);

        String jsonOut = Json.toJson(book);
        System.out.println("\nSerialized back:\n" + jsonOut);

        String jsonMap = """
            {
                "status": "success",
                "data": {
                    "count": 42,
                    "items": ["item1", "item2"]
                }
            }
            """;
        Map<String, Object> map = Json.fromJsonToMap(jsonMap);
        System.out.println("\nParsed map: " + map);
    }
}