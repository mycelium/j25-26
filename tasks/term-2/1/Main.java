import json.Json;
import json.JsonConfig;
import json.JsonMapper;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String studentJson = """
                {
                  "name": "Alice",
                  "age": 21,
                  "skills": ["Java", "JSON"],
                  "address": {
                    "city": "Moscow",
                    "street": "Lenina"
                  }
                }
                """;

        Student student = Json.parse(studentJson, Student.class);
        System.out.println("Student: " + student.name + ", age=" + student.age);
        System.out.println("City: " + student.address.city);

        Map<String, Object> asMap = Json.parseToMap(studentJson);
        System.out.println("Map keys: " + asMap.keySet());

        JsonMapper mapper = Json.mapper(JsonConfig.builder()
                .includeNullFields(false)
                .failOnUnknownProperties(true)
                .build());

        String serialized = mapper.write(student);
        System.out.println("Serialized: " + serialized);
    }

    static class Student {
        public String name;
        public int age;
        public List<String> skills;
        public Address address;
    }

    static class Address {
        public String city;
        public String street;
    }
}
