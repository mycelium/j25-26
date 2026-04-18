import json.Json;
import java.util.*;

public class Main {
    public static class Person {
        private String name;
        private int age;
        private boolean active;
        private List<String> tags;
        private Address address;

        public Person() {}

        public Person(String name, int age, boolean active, List<String> tags, Address address) {
            this.name = name;
            this.age = age;
            this.active = active;
            this.tags = tags;
            this.address = address;
        }

        @Override
        public String toString() {
            return String.format("Person{name='%s', age=%d, active=%s, tags=%s, address=%s}",
                    name, age, active, tags, address);
        }
    }

    public static class Address {
        private String city;
        private String street;
        private int building;

        public Address() {}

        public Address(String city, String street, int building) {
            this.city = city;
            this.street = street;
            this.building = building;
        }

        @Override
        public String toString() {
            return String.format("Address{city='%s', street='%s', building=%d}",
                    city, street, building);
        }
    }

    public static void main(String[] args) {
        System.out.println("Проверка примитивов:");
        System.out.println("Число (int): 51 -> " + Json.toJson(51));
        System.out.println("Число (double): 43.28 -> " + Json.toJson(43.28));
        System.out.println("Строка: \"Арто\" -> " + Json.toJson("Арто"));
        System.out.println("Логическое: true -> " + Json.toJson(true));
        System.out.println("Null: null -> " + Json.toJson(null));
        System.out.println("Массив строк: [\"A\", \"B\", \"C\"] -> " + Json.toJson(new String[]{"A", "B", "C"}));
        System.out.println("Список чисел: [1, 2, 3] -> " + Json.toJson(Arrays.asList(1, 2, 3)));

        System.out.println("\nJson В Map");
        String flatJson = "{\"brand\":\"Toyota\",\"year\":2020,\"electric\":false}";
        System.out.println("Исходный JSON: " + flatJson);

        Map<String, Object> carMap = Json.parseToMap(flatJson);
        System.out.println("Результат в Map: " + carMap);

        System.out.println("\nПользовательский класс");
        Address address = new Address("Санкт-Петербург", "Политехническая", 10);
        List<String> tags = Arrays.asList("разработчик", "java", "json");
        Person originalPerson = new Person("Иван Петров", 30, true, tags, address);

        String personJson = Json.toJson(originalPerson);
        System.out.println("Объект Person в Json:\n" + personJson);

        Person restoredPerson = Json.parseToObject(personJson, Person.class);
        System.out.println("\nОбъект Json в Person:");
        System.out.println(restoredPerson);

        System.out.println("\nВложенные структуры");
        Map<String, Object> complexData = new LinkedHashMap<>();
        complexData.put("project", "JSON Library");
        complexData.put("version", 1.0);
        complexData.put("author", restoredPerson);
        complexData.put("supportedTypes", new String[]{"String", "Number", "Boolean", "Array", "Object"});
        String complexJson = Json.toJson(complexData);
        System.out.println("Сложная структура -> JSON:");
        System.out.println(complexJson);
        Map<String, Object> parsedComplex = Json.parseToMap(complexJson);
        System.out.println("\nJSON в Map:");
        System.out.println("Ключ 'project': " + parsedComplex.get("project"));
        System.out.println("Ключ 'version': " + parsedComplex.get("version"));
        System.out.println("Ключ 'author': " + parsedComplex.get("author"));
    }
}