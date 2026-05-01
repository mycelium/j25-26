import tokenizer.Json;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String json = """
            {
              "name":"Ivan",
              "age":20,
              "skills":["java","go"]
            }
            """;

        User user = Json.parse(json, User.class);
        System.out.println(user.name + " " + user.age + " " + user.skills);
        System.out.println(Json.stringify(user));
    }

    public static class User {
        public String name;
        public int age;
        public List<String> skills;
    }
}
