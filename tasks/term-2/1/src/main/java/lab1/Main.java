package lab1;

import lab1.json.Json;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Json json = new Json();

        String text = """
                {
                  "name": "Sonya",
                  "age": 20,
                  "active": true,
                  "address": {
                    "city": "Minsk",
                    "street": "Nezavisimosti"
                  },
                  "tags": ["student", "java"],
                  "scores": [10, 20, 30]
                }
                """;

        Map<String, Object> map = json.parseToMap(text);
        System.out.println("MAP:");
        System.out.println(map);

        User user = json.parse(text, User.class);
        System.out.println("CLASS:");
        System.out.println(user.name + " " + user.age + " " + user.address.city);

        String generated = json.toJson(user);
        System.out.println("TO JSON:");
        System.out.println(generated);
    }
}