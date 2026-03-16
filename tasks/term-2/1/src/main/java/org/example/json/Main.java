package org.example.json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    // Пользовательский класс для теста
    static class User {
        private String name;
        private int age;
        private boolean isActive;
        private String[] roles;
        private Address address;

        public User() {}

        public User(String name, int age, boolean isActive, String[] roles, Address address) {
            this.name = name;
            this.age = age;
            this.isActive = isActive;
            this.roles = roles;
            this.address = address;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + ", isActive=" + isActive + 
                   ", roles=" + Arrays.toString(roles) + ", address=" + address + "}";
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

    public static void main(String[] args) {
        System.out.println("1. Сериализация в JSON");
        Address address = new Address("Moscow", "Lenina");
        User user = new User("Ivan", 30, true, new String[]{"admin", "user"}, address);
        
        String jsonString = Json.toJson(user);
        System.out.println("Сериализованный JSON:\n" + jsonString);


        System.out.println("\n2. Десериализация в Map<String, Object>");
        Map<String, Object> jsonMap = Json.parseMap(jsonString);
        System.out.println("Полученный Map:\n" + jsonMap);


        System.out.println("\n3. Десериализация в конкретный Class");
        User parsedUser = Json.parseObject(jsonString, User.class);
        System.out.println("Полученный Java Object:\n" + parsedUser);
    }
}