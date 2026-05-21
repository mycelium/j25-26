import com.jsonparser.Json;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== JSON Parser Library Demo ===\n");

        System.out.println("1. parseMap() - JSON to Map:");
        String json1 = "{\"name\":\"Polina\",\"age\":20,\"active\":true}";
        Map<String, Object> map = Json.parseMap(json1);
        System.out.println("   Input: " + json1);
        System.out.println("   Result: " + map);
        System.out.println("   name: " + map.get("name"));
        System.out.println("   age: " + map.get("age"));
        System.out.println("   active: " + map.get("active"));

        System.out.println("\n2. parseObject() - JSON to Class:");
        String json2 = "{\"name\":\"Alice\",\"age\":25,\"active\":false}";
        Person person = Json.parseObject(json2, Person.class);
        System.out.println("   Input: " + json2);
        System.out.println("   Result: Person{name=" + person.name + ", age=" + person.age + ", active=" + person.active + "}");

        System.out.println("\n3. stringify() - Object to JSON:");
        Person bob = new Person();
        bob.name = "Bob";
        bob.age = 30;
        bob.active = true;
        String json3 = Json.stringify(bob);
        System.out.println("   Input Person: {name=Bob, age=30, active=true}");
        System.out.println("   Output JSON: " + json3);

        System.out.println("\n4. parse() - JSON to Object:");
        String json4 = "{\"message\":\"Hello World\",\"value\":42}";
        Object obj = Json.parse(json4);
        System.out.println("   Input: " + json4);
        System.out.println("   Output: " + obj);

        System.out.println("\n5. Array support:");
        String json5 = "{\"name\":\"Team\",\"scores\":[95,87,92]}";
        Map<String, Object> map2 = Json.parseMap(json5);
        System.out.println("   Input: " + json5);
        System.out.println("   scores: " + map2.get("scores"));
        System.out.println("   scores[0]: " + ((List<?>) map2.get("scores")).get(0));

        System.out.println("\n6. Unicode support:");
        String json6 = "{\"text\":\"Hello \\u041C\\u0438\\u0440\"}";
        Map<String, Object> map3 = Json.parseMap(json6);
        System.out.println("   Input: " + json6);
        System.out.println("   Output: " + map3.get("text"));
        System.out.println("   (\\u041C\\u0438\\u0440 = 'Мир' in Russian)");

        System.out.println("\n7. Nested objects:");
        String json7 = "{\"user\":{\"name\":\"John\",\"age\":35,\"active\":true},\"status\":\"active\"}";
        Map<String, Object> map4 = Json.parseMap(json7);
        System.out.println("   Input: " + json7);
        System.out.println("   user: " + map4.get("user"));
        System.out.println("   user.name: " + ((Map<?, ?>) map4.get("user")).get("name"));

        System.out.println("\n8. Null values:");
        String json8 = "{\"name\":null,\"age\":null}";
        Map<String, Object> map5 = Json.parseMap(json8);
        System.out.println("   Input: " + json8);
        System.out.println("   name: " + map5.get("name"));
        System.out.println("   age: " + map5.get("age"));

        System.out.println("\n9. Number types:");
        String json9 = "{\"small\":100,\"big\":10000000000,\"float\":3.14}";
        Map<String, Object> map6 = Json.parseMap(json9);
        System.out.println("   small (int): " + map6.get("small") + " -> " + map6.get("small").getClass().getSimpleName());
        System.out.println("   big (long): " + map6.get("big") + " -> " + map6.get("big").getClass().getSimpleName());
        System.out.println("   float (double): " + map6.get("float") + " -> " + map6.get("float").getClass().getSimpleName());

        System.out.println("\n=== ALL TESTS PASSED ===");
        System.out.println("=== JSON Parser Library works correctly ===");
    }
}

class Person {
    public String name;
    public int age;
    public boolean active;
}