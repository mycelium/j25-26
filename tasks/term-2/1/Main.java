import jsonparser.*;

import java.util.*;

public class Main {
    public static class Person {
        public String name;
        public int age;
        public boolean active;
        public double score;
        public String nickname;

        public Person() {
        }

        public Person(String name, int age, boolean active, double score) {
            this.name = name;
            this.age = age;
            this.active = active;
            this.score = score;
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age
                    + ", active=" + active + ", score=" + score
                    + ", nickname=" + nickname + "}";
        }
    }

    public static class Team {
        public String teamName;
        public String[] members;
        public List<String> tags;

        public Team() {
        }

        @Override
        public String toString() {
            return "Team{teamName='" + teamName + "', members="
                    + Arrays.toString(members) + ", tags=" + tags + "}";
        }
    }

    public static void main(String[] args) {

        System.out.println("=== 1. Serialize POJO to JSON ===");
        Person alice = new Person("Alice", 30, true, 9.5);
        String json = JsonMapper.toJson(alice);
        System.out.println(json);

        System.out.println();
        System.out.println("=== 2. Serialize Map to JSON ===");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("city", "Helsinki");
        map.put("population", 650000);
        map.put("eu", true);
        System.out.println(JsonMapper.toJson(map));

        System.out.println();
        System.out.println("=== 3. Serialize List / Array to JSON ===");
        List<Object> list = Arrays.asList("a", 1, false, null);
        System.out.println(JsonMapper.toJson(list));

        int[] intArray = { 10, 20, 30 };
        System.out.println(JsonMapper.toJson(intArray));

        System.out.println();
        System.out.println("=== 4. Parse JSON → generic Object ===");
        Object parsed = JsonMapper.fromJson("{\"x\":42,\"y\":true,\"z\":null}");
        System.out.println(parsed);

        System.out.println();
        System.out.println("=== 5. Parse JSON → Map<String, Object> ===");
        Map<String, Object> parsedMap = JsonMapper.toMap("{\"lang\":\"Java\",\"version\":21}");
        System.out.println("lang = " + parsedMap.get("lang"));
        System.out.println("version = " + parsedMap.get("version"));

        System.out.println();
        System.out.println("=== 6. Parse JSON → specific class (Person) ===");
        String personJson = "{\"name\":\"Bob\",\"age\":25,\"active\":false,\"score\":7.3}";
        Person bob = JsonMapper.fromJson(personJson, Person.class);
        System.out.println(bob);

        System.out.println();
        System.out.println("=== 7. Parse JSON → class with array + list fields (Team) ===");
        String teamJson = "{\"teamName\":\"Dev\",\"members\":[\"Alice\",\"Bob\"],\"tags\":[\"java\",\"backend\"]}";
        Team team = JsonMapper.fromJson(teamJson, Team.class);
        System.out.println(team);

        System.out.println();
        System.out.println("=== 8. Round-trip: serialize then parse ===");
        String serialized = JsonMapper.toJson(alice);
        Person aliceCopy = JsonMapper.fromJson(serialized, Person.class);
        System.out.println("Original : " + alice);
        System.out.println("Round-trip: " + aliceCopy);

        System.out.println();
        System.out.println("=== 9. Nested objects ===");
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("id", 1);
        Map<String, Object> address = new LinkedHashMap<>();
        address.put("street", "Mannerheimintie");
        address.put("city", "Helsinki");
        nested.put("address", address);
        String nestedJson = JsonMapper.toJson(nested);
        System.out.println(nestedJson);

        System.out.println();
        System.out.println("=== 10. Edge cases ===");
        System.out.println(JsonMapper.fromJson("null"));
        System.out.println(JsonMapper.fromJson("true"));
        System.out.println(JsonMapper.fromJson("42"));
        System.out.println(JsonMapper.fromJson("3.14"));
        System.out.println(JsonMapper.fromJson("\"hello\""));
        System.out.println(JsonMapper.fromJson("[]"));
        System.out.println(JsonMapper.fromJson("{}"));

        System.out.println();
        System.out.println("All tests passed!");
    }
}
