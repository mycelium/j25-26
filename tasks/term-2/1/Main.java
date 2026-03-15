import java.util.*;

class Main {
    public static void main(String[] args) {

        // JSON to Object
        String json1 = "{\"name\":\"Alice\",\"age\":20,\"isGaveUp\":false,\"scores\":[5,3,4]}";
        Object obj1 = MyJsonLibrary.parse(json1);
        System.out.println("obj = " + obj1);

        // JSON to Map
        String json2 =
            "{\"name\":\"Rita\",\"age\":20,\"isGaveUp\":false,\"scores\":[3,5,4]}";
        Map<String,Object> map = MyJsonLibrary.parseToMap(json2);
        System.out.println("Map = " + map);
        
        // JSON to complex Map
        String json3 = "{\"students\":[{\"name\":\"Kate\",\"age\":22,\"isGaveUp\":true},{\"name\":\"Mike\",\"age\":21,\"isGaveUp\":false}]}";
        Map<String, Object> map3 = MyJsonLibrary.parseToMap(json3);
        System.out.println("Complex map: " + map3.get("students"));
        
        // JSON to specified Class
        String json4 = "{\"name\":\"Bob\",\"age\":19,\"isGaveUp\":true,\"scores\":[2,3,4]}";
        Student student = MyJsonLibrary.parse(json4, Student.class);
        System.out.println("Student: " + student.name + ", " + student.age + ", " + student.isGaveUp + ", " + student.scores);
    
        // Objects to JSON
        String json5 = MyJsonLibrary.toJson(student);
        System.out.println("JSON from class: " + json5);

        String json = MyJsonLibrary.toJson(obj1);
        System.out.println("JSON from Object: " + json);

        String json6 = MyJsonLibrary.toJson(map);
        System.out.println("JSON from Map: " + json6);

        String json7 = MyJsonLibrary.toJson(map3);
        System.out.println("JSON from Complex Map: " + json7);
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
}        