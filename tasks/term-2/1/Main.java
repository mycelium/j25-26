import jsonlib.Json;
import java.util.*;

public class Main {

    public static void main(String[] args) {

  
        String jsonUser = """
            {
                "name": "Ali",
                "age": 25,
                "active": true,
                "address": {
                    "city": "Karachi",
                    "country": "Pakistan"
                },
                "skills": ["Java", "JSON", "Backend"]
            }
            """;

        User user = Json.toObject(jsonUser, User.class);

        System.out.println("OBJECT RESULT ");
        System.out.println("Name: " + user.name);
        System.out.println("Age: " + user.age);
        System.out.println("Active: " + user.active);
        System.out.println("City: " + user.address.city);
        System.out.println("Skills: " + user.skills);

        
        String serialized = Json.toJson(user);

        System.out.println("\n SERIALIZED JSON ");
        System.out.println(serialized);

      
        String jsonMap = """
            {
                "status": "ok",
                "code": 200,
                "data": {
                    "users": ["Ali", "Sara", "John"],
                    "count": 3
                }
            }
            """;

        Map<String, Object> map = Json.toMap(jsonMap);

        System.out.println("\n MAP RESULT");
        System.out.println(map);


      
        String complex = """
            {
                "users": [
                    {"name": "Ali", "age": 20},
                    {"name": "Sara", "age": 22}
                ],
                "total": 2
            }
            """;

        Map<String, Object> complexMap = Json.toMap(complex);

        System.out.println("\n COMPLEX MAP ");
        System.out.println(complexMap);
    }
}