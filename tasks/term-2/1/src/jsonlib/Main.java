package jsonlib;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    static class Address {
        private String city;
        private String street;

        public Address() {} 

        public Address(String city, String street) {
            this.city = city;
            this.street = street;
        }

        @Override
        public String toString() {
            return "Address{city='" + city + "', street='" + street + "'}";
        }
    }

    static class User {
        private String name;
        private int age;
        private boolean active;
        private Double salary; 
        private String[] roles;
        private List<String> tags;
        private Address address;
        private Object dynamicData; 
        
        public User() {}

        public User(String name, int age, boolean active, Double salary, String[] roles, List<String> tags, Address address) {
            this.name = name;
            this.age = age;
            this.active = active;
            this.salary = salary;
            this.roles = roles;
            this.tags = tags;
            this.address = address;
        }

        @Override
        public String toString() {
            return "User{" +
                     "name='" + name + '\'' +
                     ", age=" + age +
                     ", active=" + active +
                     ", salary=" + salary +
                     ", roles=" + Arrays.toString(roles) +
                     ", tags=" + tags +
                     ", address=" + address +
                     ", dynamicData=" + dynamicData +
                    '}';
        }
    }

    public static void main(String[] args) {
        System.out.println("1. Serialization of complex object");
        Address addr = new Address("St. Petersburg", "Nevsky Prospect");
        User user = new User("Olya", 25, true, 1500.50, 
                             new String[]{"admin", "user"}, 
                             Arrays.asList("java", "docker"), 
                             addr);
        
        String json = Json.toJson(user);
        System.out.println(json);
      
        System.out.println("\n2. Parsing to Map");
        Map<String, Object> map = Json.parseToMap(json);
        System.out.println("Name from Map: " + map.get("name"));
        System.out.println("Address Map: " + map.get("address"));

        System.out.println("\n3. Deserialization to object");
        User restoredUser = Json.fromJson(json, User.class);
        System.out.println(restoredUser);
        
        System.out.println("Age match: " + (user.age == restoredUser.age));
        if (restoredUser.address != null) {
            System.out.println("City match: " + restoredUser.address.city.equals("St. Petersburg"));
        }

        System.out.println("\n4. Handling nulls and special types");
        User userWithNulls = new User("Anna", 30, false, null, null, null, null);
        String jsonNulls = Json.toJson(userWithNulls);
        System.out.println(jsonNulls);
        
        System.out.println("\n5. Cyclic dependencies (protection)");
        class Node {
            String val;
            Node next;
            Node(String v) { val = v; }
        }
        Node n1 = new Node("A");
        Node n2 = new Node("B");
        n1.next = n2;
        n2.next = n1; 
        
        String cyclicJson = Json.toJson(n1);
        System.out.println("Cyclic JSON: " + cyclicJson);
    }
}
