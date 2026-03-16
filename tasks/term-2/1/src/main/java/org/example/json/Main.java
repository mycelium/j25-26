package org.example.json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    
    static class User {
        private String name;
        private int age;             // primitive
        private boolean isActive;    // primitive
        private Double salary;       // boxing type
        private String nickname;     // null
        private String[] roles;      // array
        private List<String> perms;  // collection
        private Address address;     // class

        public User() {}

        public User(String name, int age, boolean isActive, Double salary, 
                    String nickname, String[] roles, List<String> perms, Address address) {
            this.name = name;
            this.age = age;
            this.isActive = isActive;
            this.salary = salary;
            this.nickname = nickname;
            this.roles = roles;
            this.perms = perms;
            this.address = address;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + ", isActive=" + isActive +
                   ", salary=" + salary + ", nickname=" + nickname +
                   ", roles=" + Arrays.toString(roles) + ", perms=" + perms + 
                   ", address=" + address + "}";
        }
    }

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

    static class Node {
        public String name;
        public Node next;                 // Для создания цикла
        public double badNumber = Double.NaN; // Не поддерживается в JSON
        public Thread badObject = new Thread(); // Не поддерживается в JSON

        public Node() {}
        public Node(String name) { this.name = name; }
    }

    // --- 3. Сам тест ---
    public static void main(String[] args) {
        System.out.println("ТЕСТ 1: Базовые типы, массивы, коллекции, классы");
        Address address = new Address("Moscow", "Lenina");
        List<String> permissions = Arrays.asList("read", "write", "execute");
        
        // salary - это Double (boxing), nickname передаем как null
        User user = new User("Ivan", 30, true, 2500.50, null, new String[]{"admin", "user"}, permissions, address);
        
        String jsonString = Json.toJson(user);
        System.out.println("Сериализованный JSON:\n" + jsonString);

        System.out.println("\nТЕСТ 2: Десериализация в Map<String, Object>");
        Map<String, Object> jsonMap = Json.parseMap(jsonString);
        System.out.println("Полученный Map:\n" + jsonMap);

        System.out.println("\nТЕСТ 3: Десериализация в конкретный Class");
        User parsedUser = Json.parseObject(jsonString, User.class);
        System.out.println("Полученный Java Object:\n" + parsedUser);


        System.out.println("\nТЕСТ 4: Limitations (Циклы и непредставимые типы)");
        Node a = new Node("Node A");
        Node b = new Node("Node B");
        
        // циклические зависимости: A -> B -> A
        a.next = b;
        b.next = a;

        String nodeJson = Json.toJson(a);
        System.out.println("JSON с циклами:\n" + nodeJson);
    }
}