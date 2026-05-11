package myjson;

import java.util.*;

public class Main {
    static class Profile {
        private String email;
        private int level;

        public Profile() {}
        public Profile(String email, int level) {
            this.email = email;
            this.level = level;
        }
        @Override
        public String toString() {
            return "Profile{email=" + email + ", level=" + level + "}";
        }
    }

    static class User {
        private String name;
        private int age;
        private double balance;
        private boolean active;
        private Long id;
        private List<String> tags;
        private String[] roles;
        private Profile profile;

        public User() {}
        public User(String name, int age, double balance, boolean active, Long id,
                    List<String> tags, String[] roles, Profile profile) {
            this.name = name;
            this.age = age;
            this.balance = balance;
            this.active = active;
            this.id = id;
            this.tags = tags;
            this.roles = roles;
            this.profile = profile;
        }
        @Override
        public String toString() {
            return "User{name=" + name + ", age=" + age + ", balance=" + balance +
                   ", active=" + active + ", id=" + id + ", tags=" + tags +
                   ", roles=" + Arrays.toString(roles) + ", profile=" + profile + "}";
        }
    }

    public static void main(String[] args) {
        var parser = JsonParser.class; 

        System.out.println("1. Примитивы и строки:");
        System.out.println("  int 42 -> " + JsonParser.toJson(42));
        System.out.println("  double 3.14 -> " + JsonParser.toJson(3.14));
        System.out.println("  boolean true -> " + JsonParser.toJson(true));
        System.out.println("  String \"Hello world\" -> " + JsonParser.toJson("Hello world"));
        System.out.println("  null -> " + JsonParser.toJson(null));
        System.out.println();

        System.out.println("2. Массивы:");
        int[] intArr = {10, 20, 30};
        String[] strArr = {"x", "y", "z"};
        var intJson = JsonParser.toJson(intArr);
        var strJson = JsonParser.toJson(strArr);
        System.out.println("  int[] -> " + intJson);
        System.out.println("  String[] -> " + strJson);
        var parsedIntArr = JsonParser.parse(intJson, int[].class);
        var parsedStrArr = JsonParser.parse(strJson, String[].class);
        System.out.println("  обратно int[] -> " + Arrays.toString(parsedIntArr));
        System.out.println("  обратно String[] -> " + Arrays.toString(parsedStrArr));
        System.out.println();

        System.out.println("3. Коллекция:");
        List<String> list = Arrays.asList("apple", "banana", "cherry");
        Set<Double> set = new LinkedHashSet<>(Arrays.asList(1.1, 2.2, 3.3));
        System.out.println("  List -> " + JsonParser.toJson(list));
        System.out.println("  Set -> " + JsonParser.toJson(set));
        var parsedList = JsonParser.parse(JsonParser.toJson(list), List.class);
        System.out.println("  обратно List -> " + parsedList);
        System.out.println();

        System.out.println("4. Map:");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("title", "Test");
        map.put("count", 5);
        map.put("enabled", false);
        map.put("empty", null);
        var mapJson = JsonParser.toJson(map);
        System.out.println("  Map -> " + mapJson);
        var parsedMap = JsonParser.parseToMap(mapJson);
        System.out.println("  обратно Map -> " + parsedMap);
        System.out.println();

        System.out.println("5. Сериализация и десериализация User:");
        var profile = new Profile("alice@example.com", 42);
        String[] roles = {"admin", "user"};
        var user = new User("Alice", 28, 1500.75, true, 100500L,
                             Arrays.asList("java", "json"), roles, profile);
        var userJson = JsonParser.toJson(user);
        System.out.println("  User JSON: " + userJson);
        var parsedUser = JsonParser.parse(userJson, User.class);
        System.out.println("  Parsed User: " + parsedUser);
        System.out.println();

        System.out.println("6. Массив User:");
        User[] users = {
            new User("Bob", 22, 0.0, false, null, List.of("beginner"), null, null),
            user
        };
        var usersJson = JsonParser.toJson(users);
        System.out.println("  User[] JSON: " + usersJson);
        var parsedUsers = JsonParser.parse(usersJson, User[].class);
        System.out.println("  Parsed User[]: " + Arrays.toString(parsedUsers));
        System.out.println();

        System.out.println("7. Парсинг в Object:");
        var obj = JsonParser.parse(userJson);
        System.out.println("  Тип: " + obj.getClass().getSimpleName() + " -> " + obj);
        System.out.println();

        System.out.println("8. Парсинг в Map (с pattern matching):");
        var userAsMap = JsonParser.parseToMap(userJson);
        System.out.println("  Map: " + userAsMap);
        System.out.println();

        System.out.println("9. Поддержка Unicode escape:");
        var unicodeJson = "\"\\u041F\\u0440\\u0438\\u0432\\u0435\\u0442\"";
        var unicodeString = JsonParser.parse(unicodeJson);
        System.out.println("  JSON: " + unicodeJson + " -> " + unicodeString);
        System.out.println();

        System.out.println("10. Текстовый блок с JSON:");
        var multilineJson = """
                {
                    "name": "TextBlock",
                    "value": 123
                }
                """;
        var parsedMapFromBlock = JsonParser.parseToMap(multilineJson);
        System.out.println("  Парсинг текстового блока: " + parsedMapFromBlock);
    }
}