import json.Json;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String jsonInput = """
            {
                "title": "Dune",
                "year": 1965,
                "genres": ["Sci-Fi", "Adventure", "Drama"],
                "author": {
                    "name": "Frank Herbert",
                    "birthYear": 1920
                }
            }
            """;

        try {
            System.out.println("Pars Test");
            Map<String, Object> book = Json.parseMap(jsonInput);
            System.out.println("Name: " + book.get("title"));
            
            if (book.get("author") instanceof Map<?, ?> author) {
                System.out.println("Author: " + author.get("name"));
            }

            System.out.println("\nSer Test");
            String serialized = Json.toJson(book);
            System.out.println(serialized);

            System.out.println("\nSucces");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}