import java.util.*;

public class Test21 {
    static class Address {
        public String city;
        public String street;
        public Address() {}
    }
    static class Person {
        public String name;
        public int age;
        public Address address;
        public List<String> phones;
        public Person() {}
    }
    
    public static void main(String[] args) {
        LibMain mapper = new LibMain();
        
        String jsonInput = "{\"name\": \"Name\", \"age\": 20, \"address\": {\"city\": \"Saint-Pitersburg\", \"street\": \"Politechnicheskaya\"}, \"phones\": [\"+78888888888\", \"+79999999999\"]}";
        Person person = mapper.readValue(jsonInput, Person.class);

        System.out.println("input json: " + jsonInput);
        System.out.println("json to obect: ");
        System.out.println("\tИмя: " + person.name);
        System.out.println("\tВозраст: " + person.age);
        System.out.println("\tГород: " + person.address.city);
        System.out.println("\tТелефоны: " + person.phones);
        
        String jsonOutput = mapper.writeValueAsString(person);
        System.out.print("object to json\n\t");
        System.out.println(jsonOutput);
        
        System.out.println("json to map:");
        Map<String, Object> map = mapper.parseToMap(jsonInput);

        System.out.println("\tИмя: " + map.get("name"));
        System.out.println("\tВозраст: " + map.get("age"));
        System.out.println("\tАдрес: " + map.get("address"));
        System.out.println("\tТелефоны: " + map.get("phones"));
        
    }
}