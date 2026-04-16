package org.example.json;

import java.util.*;

public class Main {
    
    static class Person {
        private String name;
        private int age;
        private boolean active;
        private Double salary;
        private String[] tags;
        private List<String> hobbies;
        private Address address;
        
        public Person() {}
        
        public void setName(String name) { this.name = name; }
        public void setAge(int age) { this.age = age; }
        public void setActive(boolean active) { this.active = active; }
        public void setSalary(Double salary) { this.salary = salary; }
        public void setTags(String[] tags) { this.tags = tags; }
        public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }
        public void setAddress(Address address) { this.address = address; }
        
        public String toString() {
            return String.format("Person{name='%s', age=%d, active=%s, salary=%s, tags=%s, hobbies=%s, address=%s}",
                name, age, active, salary, Arrays.toString(tags), hobbies, address);
        }
    }
    
    static class Address {
        private String city;
        private String street;
        
        public Address() {}
        
        public void setCity(String city) { this.city = city; }
        public void setStreet(String street) { this.street = street; }
        
        public String toString() {
            return String.format("Address{city='%s', street='%s'}", city, street);
        }
    }
    
    public static void main(String[] args) {
        Address addr = new Address();
        addr.setCity("Moscow");
        addr.setStreet("Tverskaya");
        
        Person person = new Person();
        person.setName("Ivan Petrov");
        person.setAge(28);
        person.setActive(true);
        person.setSalary(45000.50);
        person.setTags(new String[]{"developer", "java"});
        person.setHobbies(Arrays.asList("chess", "reading", "swimming"));
        person.setAddress(addr);
        
        String json = JsonLib.stringify(person);
        System.out.println("JSON:\n" + json);
        
        Map<String, Object> map = JsonLib.readAsMap(json);
        System.out.println("\nMap:\n" + map);
        
        Person restored = JsonLib.readAsObject(json, Person.class);
        System.out.println("\nВосстановленный объект:\n" + restored);
        
        String sampleJson = "{\"users\": [\"Alice\", \"Bob\"], \"count\": 2}";
        Object parsed = JsonLib.read(sampleJson);
        System.out.println("\nРаспарсенный JSON:\n" + parsed);
    }
}
