import java.util.*;

public class Main {

    public static void main(String[] args) {

        System.out.println("1. PARSE TO MAP & LIST");
        String jsonMapStr = "{\"name\": \"Alice\", \"age\": 25, \"active\": true}";
        String jsonArrayStr = "[1, 2.5, \"Hello World\", null]";

        try {
            Map<String, Object> map = FromJsonParser.parseToMap(jsonMapStr);
            System.out.println("   JSON String -> Map: " + map);

            List<Object> list = (List<Object>) FromJsonParser.parseToObject(jsonArrayStr);
            System.out.println("   JSON String -> List: " + list);
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("2. JSON -> Java Object");

        String jsonComplex = """
                {
                    "id": 101,
                    "name": "John Doe",
                    "salary": 5000.75,
                    "address": {
                        "city": "Moscow",
                        "street": "Tverskaya"
                    },
                    "roles": ["admin", "user"]
                }
                """;

        try {
            User user = FromJsonParser.parseToClass(jsonComplex, User.class);
            System.out.println("   Successfully parsed User object!");
            System.out.println("   User ID: " + user.id);
            System.out.println("   User Name: " + user.name);
            System.out.println("   Address City: " + (user.address != null ? user.address.city : "null"));
            System.out.println("   Roles: " + user.roles);
        } catch (Exception e) {
            System.out.println("   Error during parsing: " + e.getMessage());
        }

        System.out.println("3. Java Object -> JSON");

        User newUser = new User();
        newUser.id = 202;
        newUser.name = "Bob Smith";
        newUser.salary = 3000.0;

        Address addr = new Address();
        addr.city = "London";
        addr.street = "Baker Street";
        newUser.address = addr;

        newUser.roles = new ArrayList<>();
        newUser.roles.add("user");

        try {
            String jsonOutput = ToJsonParser.parseToJson(newUser);
            System.out.println("   Generated JSON:");
            System.out.println("   " + jsonOutput);
        } catch (Exception e) {
            System.out.println("   Error during parsing: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("4. EDGE CASES");

        Map<String, Object> edgeCaseMap = new HashMap<>();
        edgeCaseMap.put("intVal", 42);
        edgeCaseMap.put("doubleVal", 3.14);
        edgeCaseMap.put("booleanVal", false);
        edgeCaseMap.put("nullVal", null);
        edgeCaseMap.put("nested", Map.of("key", "value"));

        try {
            String jsonEdge = ToJsonParser.parseToJson(edgeCaseMap);
            System.out.println("   Complex Map -> JSON: " + jsonEdge);

            Map<String, Object> parsedBack = FromJsonParser.parseToMap(jsonEdge);
            System.out.println("   JSON -> Map (Round Trip): " + parsedBack);
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }
    }

    static class User {
        public int id;
        public String name;
        public double salary;
        public Address address;
        public List<String> roles;

        @Override
        public String toString() {
            return "User{" + "id=" + id + ", name='" + name + "', salary=" + salary + ", address=" + address + ", roles=" + roles + '}';
        }
    }

    static class Address {
        public String city;
        public String street;

        @Override
        public String toString() {
            return "Address{" + "city='" + city + "', street='" + street + "'}";
        }
    }
}