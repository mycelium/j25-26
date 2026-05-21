package papkaJSON;
import java.util.*;

public class Main {

    static class Person {
        private String fullName;
        private int years;
        private boolean active;
        private List<String> hobbies;
        private Location location;

        public Person(String fullName, int years, boolean active, List<String> hobbies, Location location) {
            this.fullName = fullName;
            this.years = years;
            this.active = active;
            this.hobbies = hobbies;
            this.location = location;
        }

        public Person() {}

        public String toString() {
            return "Person{fullName=" + fullName + ", years=" + years + ", active=" + active + ", " +
                    "hobbies=" + hobbies + ", location=" + location + "}";
        }
    }

    static class Location {
        private String town;
        private String road;
        private int code;

        public Location(String town, String road, int code) {
            this.town = town;
            this.road = road;
            this.code = code;
        }

        public Location() {}

        public String toString() {
            return "Location{town=" + town + ", road=" + road + ", code=" + code + "}";
        }
    }

    public static void main(String[] args)
    {
        System.out.println("Простые случаи:");

        System.out.println("null: " + ConverterJSON.toJSON(null));
        System.out.println("String: " + ConverterJSON.toJSON("Hello World"));
        System.out.println("Number: " + ConverterJSON.toJSON(123));
        System.out.println("Double: " + ConverterJSON.toJSON(45.67));
        System.out.println("Boolean: " + ConverterJSON.toJSON(true));

        System.out.println("Массивы:");

        int[] numbers = {1, 2, 3, 4, 5};
        System.out.println("int[]: " + ConverterJSON.toJSON(numbers));

        String[] names = {"John", "Jane", "Bob"};
        System.out.println("String[]: " + ConverterJSON.toJSON(names));

        System.out.println("Коллекции:");
        List<String> list = Arrays.asList("apple", "banana", "cherry");
        System.out.println("List: " + ConverterJSON.toJSON(list));

        Set<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3));
        System.out.println("Set: " + ConverterJSON.toJSON(set));

        System.out.println("Map:");
        Map<String, Object> map = new HashMap<>();
        map.put("name", "John");
        map.put("age", 30);
        map.put("active", true);
        System.out.println("Map: " + ConverterJSON.toJSON(map));

        // Вложенные структуры
        Map<String, Object> nested = new HashMap<>();
        nested.put("users", Arrays.asList(
                Map.of("name", "John", "age", 30),
                Map.of("name", "Jane", "age", 25)
        ));
        nested.put("count", 2);
        System.out.println("Nested: " + ConverterJSON.toJSON(nested));


        System.out.println("JSON -> Map");

        String jsonMap = "{\"name\":\"Alice\",\"age\":25,\"city\":\"London\"}";
        Map<String, Object> resultMap = ConverterJSON.toMap(jsonMap);
        System.out.println("JSON → Map: " + resultMap);
        System.out.println("  name: " + resultMap.get("name"));
        System.out.println("  age: " + resultMap.get("age"));

        // JSON в List
        String jsonList = "[10, 20, 30, 40, 50]";
        List<Object> resultList = (List<Object>) new ParserJSON().parse(jsonList);
        System.out.println("JSON → List: " + resultList);

        List<String> list1 = Arrays.asList("one", "two", "three");
        System.out.println("  List -> " + ConverterJSON.toJSON(list1));
        List<?> parsedList = ConverterJSON.toClass(ConverterJSON.toJSON(list1), List.class);
        System.out.println("  parsed List -> " + parsedList);
        System.out.println();

        // Map
        Map<String, Object> map1 = new HashMap<>();
        map1.put("key1", 100);
        map1.put("key2", "value");
        System.out.println("  Map -> " + ConverterJSON.toJSON(map1));
        Map<String, Object> parsedMap = ConverterJSON.toMap(ConverterJSON.toJSON(map1));
        System.out.println("  parsed Map -> " + parsedMap);
        System.out.println();

        Location loc = new Location("Tur", "Moscow", 101000);
        Person person = new Person("Ivan", 25, true, Arrays.asList("programmer", "java"), loc);
        String personJson = ConverterJSON.toJSON(person);
        System.out.println("  Person JSON: " + personJson);
        Person parsedPerson = ConverterJSON.toClass(personJson, Person.class);
        System.out.println("  Parsed Person: " + parsedPerson);
        System.out.println();

        System.out.println("  null -> " + ConverterJSON.toJSON(null));
        System.out.println();

        Person[] people = {person, new Person("Petr", 30, false, List.of("c++"), loc)};
        String peopleJson = ConverterJSON.toJSON(people);
        System.out.println("  Person[] JSON: " + peopleJson);
        Person[] parsedPeople = ConverterJSON.toClass(peopleJson, Person[].class);
        System.out.println("  Parsed Person[]: " + Arrays.toString(parsedPeople));

    }

}