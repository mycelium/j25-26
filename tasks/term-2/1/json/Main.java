
package json;
import json.JsonMapper;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        JsonMapper mapper = new JsonMapper();

        // Тест JSON-строка
        String jsonInput = "{\"name\":\"Sergey\", \"age\":21}";

        // 1.  To Map<String, Object>
        Map<String, Object> mapResult = mapper.fromJsonAsMap(jsonInput);
        System.out.println("As Map: " + mapResult);

        // 2.  To specified class
        Student parsedStudent = mapper.fromJson(jsonInput, Student.class);
        System.out.println("As Class: Name = " + parsedStudent.getName() + ", Age = " + parsedStudent.getAge());
    }
}

class Student {
    private String name;
    private int age;
    
    public Student() {}

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
}