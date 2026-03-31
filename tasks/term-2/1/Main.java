import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jsonlib.Json;
import jsonlib.JsonException;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== JSON Library Tests ===\n");

        testParseToMap();

        testParseToCustomClass();

        testSerialization();

        testArraysAndCollections();

        testEdgeCases();

        System.out.println("\n=== All tests completed ===");
    }

    private static void testParseToMap() {
        System.out.println("--- Test 1: fromJsonToMap ---");
        String json = "{\"name\":\"Alice\",\"age\":30,\"active\":true,\"salary\":75000.5,\"hobbies\":[\"reading\",\"coding\"]}";
        try {
            Map<String, Object> map = Json.fromJsonToMap(json);
            System.out.println("Parsed map: " + map);
            System.out.println("name: " + map.get("name"));
            System.out.println("age: " + map.get("age"));
            System.out.println("active: " + map.get("active"));
            System.out.println("salary: " + map.get("salary"));
            System.out.println("hobbies: " + map.get("hobbies"));
        } catch (JsonException e) {
            System.err.println("Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testParseToCustomClass() {
        System.out.println("--- Test 2: fromJson to custom class ---");
        String json = "{\"name\":\"Bob\",\"age\":25,\"salary\":55000.75,\"active\":false,\"address\":{\"street\":\"123 Main St\",\"city\":\"Springfield\",\"zip\":12345},\"hobbies\":[\"chess\",\"hiking\"],\"nicknames\":[\"Bobby\",\"Bobster\"],\"tags\":null}";
        try {
            Person person = Json.fromJson(json, Person.class);
            System.out.println("Parsed Person:");
            System.out.println("  name: " + person.name);
            System.out.println("  age: " + person.age);
            System.out.println("  salary: " + person.salary);
            System.out.println("  active: " + person.active);
            System.out.println("  address: " + person.address.street + ", " + person.address.city + " " + person.address.zip);
            System.out.println("  hobbies: " + person.hobbies);
            System.out.println("  nicknames: " + Arrays.toString(person.nicknames));
            System.out.println("  tags: " + person.tags);
        } catch (JsonException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testSerialization() {
        System.out.println("--- Test 3: toJson ---");
        
        Person person = new Person();
        person.name = "Charlie";
        person.age = 35;
        person.salary = 120000.0;
        person.active = true;
        person.address = new Address();
        person.address.street = "456 Oak Ave";
        person.address.city = "Metropolis";
        person.address.zip = 67890;
        person.hobbies = Arrays.asList("gaming", "cycling", "photography");
        person.nicknames = new String[]{"Chuck", "C-Man"};
        person.tags = null;

        try {
            String json = Json.toJson(person);
            System.out.println("Serialized Person:\n" + json);
        } catch (JsonException e) {
            System.err.println("Error: " + e.getMessage());
        }

        
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", 42);
        map.put("key3", Arrays.asList(1, 2, 3));
        try {
            String mapJson = Json.toJson(map);
            System.out.println("\nSerialized Map:\n" + mapJson);
        } catch (JsonException e) {
            System.err.println("Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testArraysAndCollections() {
        System.out.println("--- Test 4: Arrays and Collections ---");
    
        String json = "{\"intArray\":[1,2,3],\"stringList\":[\"a\",\"b\",\"c\"],\"matrix\":[[1,2],[3,4]]}";
        try {
            Map<String, Object> map = Json.fromJsonToMap(json);
            System.out.println("Parsed array/collection JSON: " + map);
    
            List<Integer> intArray = (List<Integer>) map.get("intArray");
            System.out.println("intArray[1] = " + intArray.get(1));
            List<String> stringList = (List<String>) map.get("stringList");
            System.out.println("stringList[2] = " + stringList.get(2));
            List<List<Integer>> matrix = (List<List<Integer>>) map.get("matrix");
            System.out.println("matrix[1][0] = " + matrix.get(1).get(0));
        } catch (JsonException e) {
            System.err.println("Error: " + e.getMessage());
        }

        int[] numbers = {5, 10, 15};
        try {
            String arrayJson = Json.toJson(numbers);
            System.out.println("\nSerialized int array: " + arrayJson);
        } catch (JsonException e) {
            System.err.println("Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testEdgeCases() {
        System.out.println("--- Test 5: Edge Cases ---");
        
        String json1 = "{\"emptyObj\":{},\"emptyArray\":[],\"nullField\":null,\"stringNumber\":\"123\"}";
        try {
            Map<String, Object> map = Json.fromJsonToMap(json1);
            System.out.println("Edge case JSON: " + map);
            System.out.println("emptyObj is a Map: " + (map.get("emptyObj") instanceof Map));
            System.out.println("emptyArray is a List: " + (map.get("emptyArray") instanceof List));
            System.out.println("nullField = " + map.get("nullField"));
            System.out.println("stringNumber = " + map.get("stringNumber") + " (class: " + map.get("stringNumber").getClass() + ")");
        } catch (JsonException e) {
            System.err.println("Error: " + e.getMessage());
        }

        Person emptyPerson = new Person();
        try {
            String json = Json.toJson(emptyPerson);
            System.out.println("\nSerialized object with null fields: " + json);
        } catch (JsonException e) {
            System.err.println("Error: " + e.getMessage());
        }
        System.out.println();
    }
}

class Person {
    public String name;
    public int age;
    public double salary;
    public boolean active;
    public Address address;
    public List<String> hobbies;
    public String[] nicknames;
    public List<String> tags; // can be null

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", salary=" + salary +
                ", active=" + active +
                ", address=" + address +
                ", hobbies=" + hobbies +
                ", nicknames=" + Arrays.toString(nicknames) +
                ", tags=" + tags +
                '}';
    }
}

class Address {
    public String street;
    public String city;
    public int zip;

    @Override
    public String toString() {
        return "Address{" +
                "street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", zip=" + zip +
                '}';
    }
}