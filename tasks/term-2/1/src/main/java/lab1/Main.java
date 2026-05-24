package lab1;

import lab1.json.Json;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Json json = new Json();

        String text = """
                {
                  "id": 3000000000,
                  "name": "Sonya \\"Java\\"",
                  "age": 20,
                  "rating": 4.95,
                  "active": true,
                  "address": {
                    "city": "Minsk",
                    "street": "Nezavisimosti\\nProspekt"
                  },
                  "tags": ["student", "java", null],
                  "previousAddresses": [
                    {"city": "Grodno", "street": "Sovetskaya"},
                    {"city": "Brest", "street": "Moskovskaya"}
                  ],
                  "scores": [10, 20, 30]
                }
                """;

        Map<String, Object> map = json.parseToMap(text);
        System.out.println("MAP:");
        System.out.println(map);

        User user = json.parse(text, User.class);
        System.out.println("CLASS:");
        System.out.println(user.name + " " + user.age + " " + user.address.city);
        System.out.println(user.previousAddresses.get(0).city);

        String generated = json.toJson(user);
        System.out.println("TO JSON:");
        System.out.println(generated);

        User fromGenerated = json.parse(generated, User.class);
        System.out.println("ROUND TRIP:");
        System.out.println(fromGenerated.name + " " + fromGenerated.id + " " + fromGenerated.scores[0]);

        User createdInJava = new User(
                42L,
                "Alex \"Test\"",
                21,
                null,
                true,
                new Address("Helsinki", "Main\\Street"),
                List.of("json", "parser"),
                List.of(new Address("Minsk", "Lenina")),
                new int[] {1, 2, 3}
        );

        System.out.println("JAVA OBJECT TO JSON:");
        System.out.println(json.toJson(createdInJava));
    }
}
