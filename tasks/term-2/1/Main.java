import jsonlib.*;
import java.util.*;


class Address {
    String city;
    String country;
    Address() {}
}

class User {
    String name;
    int age;
    boolean active;
    Address address;
    List<String> skills;
    User() {}
}

class Order {
    String product;
    double price;
    Order() {}
}


class UUIDAdapter implements TypeAdapter<UUID> {
    @Override public String toJson(UUID value) { return "\"" + value + "\""; }
    @Override public UUID fromJson(Object raw) { return UUID.fromString((String) raw); }
}

class Product {
    UUID id;
    String label;
    Product() {}
}

// ── snake_case POJO ───────────────────────────────────────────────────────────

class ApiResponse {
    String firstName;
    String lastName;
    int createdAt;
    ApiResponse() {}
}

// ─────────────────────────────────────────────────────────────────────────────

public class Main {

    // Small helper to print section headers
    static void section(String title) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("  " + title);
        System.out.println("══════════════════════════════════════════");
    }

    public static void main(String[] args) {

        // ── 1. Basic toObject + nested object + List<String> ─────────────────
        section("1 · toObject — nested object + List<String>");

        String jsonUser = """
            {
                "name": "Ali",
                "age": 25,
                "active": true,
                "address": { "city": "Karachi", "country": "Pakistan" },
                "skills": ["Java", "JSON", "Backend"]
            }
            """;

        User user = Json.toObject(jsonUser, User.class);
        System.out.println("Name    : " + user.name);
        System.out.println("Age     : " + user.age);
        System.out.println("Active  : " + user.active);
        System.out.println("City    : " + user.address.city);
        System.out.println("Country : " + user.address.country);
        System.out.println("Skills  : " + user.skills);

        // ── 2. Serialization (toJson) ─────────────────────────────────────────
        section("2 · toJson — round-trip serialization");

        String serialized = Json.toJson(user);
        System.out.println(serialized);

        // ── 3. toMap ──────────────────────────────────────────────────────────
        section("3 · toMap — arbitrary JSON object");

        String jsonMap = """
            {
                "status": "ok",
                "code": 200,
                "data": { "users": ["Ali", "Sara", "John"], "count": 3 }
            }
            """;
        Map<String, Object> map = Json.toMap(jsonMap);
        System.out.println(map);

        // ── 4. TypeReference — List<User> deserialization ─────────────────────
        section("4 · TypeReference — List<User> (generic collection)");

        String jsonUsers = """
            [
                {"name": "Ali",  "age": 20, "active": true,  "address": null, "skills": []},
                {"name": "Sara", "age": 22, "active": false, "address": null, "skills": ["Python"]}
            ]
            """;

        List<User> users = Json.toObject(jsonUsers, new TypeReference<List<User>>() {});
        for (User u : users) {
            System.out.println(u.name + " — age " + u.age + " — skills " + u.skills);
        }

        // ── 5. TypeReference — Map<String, List<Order>> ───────────────────────
        section("5 · TypeReference — Map<String, List<Order>>");

        String jsonOrders = """
            {
                "alice": [
                    {"product": "Laptop", "price": 999.99},
                    {"product": "Mouse",  "price": 29.5}
                ],
                "bob": [
                    {"product": "Keyboard", "price": 59.0}
                ]
            }
            """;

        JsonMapper defaultMapper = JsonMapper.builder().ignoreUnknownFields(true).build();
        Map<String, List<Order>> orders = defaultMapper.fromJson(
            jsonOrders,
            new TypeReference<Map<String, List<Order>>>() {}
        );
        orders.forEach((owner, list) -> {
            System.out.println(owner + ":");
            list.forEach(o -> System.out.println("   " + o.product + " → $" + o.price));
        });

        // ── 6. TypeAdapter — custom UUID handling ─────────────────────────────
        section("6 · TypeAdapter — UUID field");

        JsonMapper adapterMapper = JsonMapper.builder()
            .registerTypeAdapter(UUID.class, new UUIDAdapter())
            .ignoreUnknownFields(true)
            .build();

        String jsonProduct = """
            {"id": "550e8400-e29b-41d4-a716-446655440000", "label": "Widget"}
            """;

        Product product = adapterMapper.fromJson(jsonProduct, Product.class);
        System.out.println("UUID class : " + product.id.getClass().getSimpleName());
        System.out.println("UUID value : " + product.id);
        System.out.println("Label      : " + product.label);
        System.out.println("Back to JSON: " + adapterMapper.toJson(product));

        // ── 7. FieldNamingStrategy — SNAKE_CASE ───────────────────────────────
        section("7 · FieldNamingStrategy — SNAKE_CASE");

        JsonMapper snakeMapper = JsonMapper.builder()
            .fieldNamingStrategy(FieldNamingStrategy.SNAKE_CASE)
            .ignoreUnknownFields(false)
            .build();

        String snakeJson = """
            {"first_name": "John", "last_name": "Doe", "created_at": 1716000000}
            """;

        ApiResponse api = snakeMapper.fromJson(snakeJson, ApiResponse.class);
        System.out.println("firstName : " + api.firstName);
        System.out.println("lastName  : " + api.lastName);
        System.out.println("createdAt : " + api.createdAt);
        System.out.println("Back to JSON (snake): " + snakeMapper.toJson(api));

        // ── 8. ignoreUnknownFields — strict vs lenient ────────────────────────
        section("8 · ignoreUnknownFields — strict mode throws, lenient passes");

        String extraFields = """
            {"name": "Ali", "age": 30, "unknownField": "oops",
             "active": false, "address": null, "skills": []}
            """;

        // Lenient (default via Json facade) — should succeed
        try {
            User lenient = Json.toObject(extraFields, User.class);
            System.out.println("Lenient  → OK, name=" + lenient.name);
        } catch (Exception e) {
            System.out.println("Lenient  → FAIL: " + e.getMessage());
        }

        // Strict — should throw on "unknownField"
        JsonMapper strictMapper = JsonMapper.builder().ignoreUnknownFields(false).build();
        try {
            strictMapper.fromJson(extraFields, User.class);
            System.out.println("Strict   → OK (unexpected)");
        } catch (JsonParseException e) {
            System.out.println("Strict   → caught: " + e.getMessage());
        }

        // ── 9. Number edge cases (int / long / double / scientific) ───────────
        section("9 · Parser — number edge cases");

        System.out.println(Json.parse("42"));           // Integer
        System.out.println(Json.parse("2147483648"));   // Long (> Integer.MAX_VALUE)
        System.out.println(Json.parse("3.14"));         // Double
        System.out.println(Json.parse("1.5e3"));        // Double via scientific notation → 1500.0
        System.out.println(Json.parse("-0.001"));       // Negative double

        // ── 10. String escaping round-trip ────────────────────────────────────
        section("10 · JsonWriter — escape round-trip");

        String tricky = "Line1\nLine2\t\"quoted\"\\ end";
        String escapedJson = Json.toJson(tricky);
        System.out.println("Serialized : " + escapedJson);
        String parsed = (String) Json.parse(escapedJson);
        System.out.println("Round-trip OK: " + tricky.equals(parsed));
    }
}