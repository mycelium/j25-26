import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import json.Json;

public class Main {

    public static class User {   
        public String name;
        public int age;
    }

    public static void main(String[] args) {

        System.out.println(Json.toJson(10));
        System.out.println(Json.toJson(true));
        System.out.println(Json.toJson("hello"));

        int[] arr = {1, 2, 3};
        String arrJson = Json.toJson(arr);
        System.out.println(arrJson);

        int[] parsedArr = Json.parse(arrJson, int[].class);
        System.out.println(Arrays.toString(parsedArr));


        List<String> list = Arrays.asList("a", "b", "c");
        String listJson = Json.toJson(list);
        System.out.println(listJson);


        Map<String, Object> map = new HashMap<>();
        map.put("x", 10);
        map.put("y", "test");

        String mapJson = Json.toJson(map);
        System.out.println(mapJson);

        System.out.println(Json.parseToMap(mapJson));

        User user = new User();
        user.name = "Varvara";
        user.age = 20;

        String userJson = Json.toJson(user);
        System.out.println(userJson);

        User parsedUser = Json.parse(userJson, User.class);
        System.out.println(parsedUser.name + " " + parsedUser.age);

        System.out.println(Json.toJson(null));
    }
}