import java.util.List;
import java.util.Map;

import jsonengine.JsonProcessor;

public class Main {
    // Простой класс автора
    public static class Author {
        public String name;
        public Author() {} 
    }

    // Класс книги
    public static class Book {
        public String title;
        public int year;
        public Author author;
        public List<String> tags;

        public Book() {}

        @Override
        public String toString() {
            return "Книга: " + title + " (" + year + "), Автор: " + (author != null ? author.name : "нет") + ", Теги: " + tags;
        }
    }

    public static void main(String[] args) {
        // Простая JSON строка
        String json = """
            {
                "title": "Мастер и Маргарита",
                "year": 1967,
                "author": { "name": "Михаил Булгаков" },
                "tags": ["Классика", "Роман"]
            }
            """;

        // 1. Читаем в объект
        Book book = JsonProcessor.read(json, Book.class);
        System.out.println("Результат десериализации:");
        System.out.println(book);

        // 2. Читаем в Map
        Map<String, Object> map = JsonProcessor.readAsMap(json);
        System.out.println("\nПоле из Map: " + map.get("title"));

        // 3. Обратно в строку
        String jsonOut = JsonProcessor.write(book);
        System.out.println("\nРезультат сериализации:");
        System.out.println(jsonOut);
    }
}