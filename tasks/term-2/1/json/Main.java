package json;

import java.util.*;

class Main {
    public static void main(String[] args) {

        // JSON to Object
        var json1 = """
            {"name":"Alice","age":20,"isGaveUp":false,"scores":[5,3,4]}
            """.trim();
        var obj1 = MyJsonLibrary.parse(json1);
        System.out.println("obj = " + obj1);

        // JSON to Map
        var json2 =
            """
            {"name":"Rita","age":20,"isGaveUp":false,"scores":[3,5,4]}
            """.trim();
        var map = MyJsonLibrary.parseToMap(json2);
        System.out.println("Map = " + map);
        
        // JSON to complex Map
        var json3 = """
            {"students":[{"name":"Kate","age":22,"isGaveUp":true},{"name":"Mike","age":21,"isGaveUp":false}]}
            """.trim();
        var map3 = MyJsonLibrary.parseToMap(json3);
        System.out.println("Complex map: " + map3.get("students"));
        
        // JSON to specified Class
        var json4 = """
            {"name":"Bob","age":19,"isGaveUp":true,"scores":[2,3,4]}
            """.trim();
        var student = MyJsonLibrary.parse(json4, Student.class);
        System.out.println("Student: " + student.name + ", " + student.age + ", " + student.isGaveUp + ", " + student.scores);
    
        // Objects to JSON
        var json5 = MyJsonLibrary.toJson(student);
        System.out.println("JSON from class: " + json5);

        var json = MyJsonLibrary.toJson(obj1);
        System.out.println("JSON from Object: " + json);

        var json6 = MyJsonLibrary.toJson(map);
        System.out.println("JSON from Map: " + json6);

        var json7 = MyJsonLibrary.toJson(map3);
        System.out.println("JSON from Complex Map: " + json7);

        var jsonEscaped = """
            {"msg":"say \\"hello\\"","note":"number1\\nnumber2\\ttab"}
            """.trim();
        var escapedMap = MyJsonLibrary.parseToMap(jsonEscaped);
        System.out.println("msg =  " + escapedMap.get("msg"));   
        System.out.println("note = " + escapedMap.get("note"));  
        System.out.println("JSON from msg and note:" + MyJsonLibrary.toJson(escapedMap));

        var json8 = """
            {"path":"C:\\\\Users\\\\marty"}
            """.trim();
        var escapedMap2 = MyJsonLibrary.parseToMap(json8);
        System.out.println("path = " + escapedMap2.get("path"));  // C:\Users\marty
        System.out.println("JSON from path:" + MyJsonLibrary.toJson(escapedMap2));

        var jsonAdvanced = """
            {
              "name":"Lena",
              "grades":[100,95,87],
              "aliases":["L","Len"],
              "friends":[{"name":"Tom","age":21,"isGaveUp":false,"scores":[1,2,3]},{"name":"Ira","age":20,"isGaveUp":true,"scores":[4,5,6]}],
              "roles":["admin","user"],
              "meta":{"level":2,"points":75}
            }
            """.trim();
        var advancedStudent = MyJsonLibrary.parse(jsonAdvanced, AdvancedStudent.class);
        System.out.println("AdvancedStudent: " + advancedStudent.name);
        System.out.println(" grades=" + Arrays.toString(advancedStudent.grades));
        System.out.println(" aliases=" + Arrays.toString(advancedStudent.aliases));
        System.out.println(" friends=" + advancedStudent.friends);
        System.out.println(" roles=" + advancedStudent.roles);
        System.out.println(" meta=" + advancedStudent.meta);
    }
}


class Student{
    public String name;
    public int age;
    public boolean isGaveUp;
    public List<Integer> scores;

    public Student() {}
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
        this.isGaveUp = false;
        this.scores = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Student{name='" + name + "', age=" + age + ", isGaveUp=" + isGaveUp + ", scores=" + scores + "}";
    }
}

class AdvancedStudent {
    public String name;
    public int[] grades;
    public String[] aliases;
    public List<Student> friends;
    public Set<String> roles;
    public Map<String, Integer> meta;

    public AdvancedStudent() {}
}
