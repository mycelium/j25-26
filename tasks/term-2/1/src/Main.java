import java.util.*;

class Address {
    String city;

    @Override
    public String toString() {
        return "Address{city='" + city + "'}";
    }
}

class User {
    String name;         
    int age;             
    Integer rank;        
    String[] tags;       
    List<Integer> scores; 
    Address address;     
    
    @Override
    public String toString() {
        return "User{\n" +
               "  name='" + name + "',\n" +
               "  age=" + age + ",\n" +
               "  rank=" + rank + ",\n" +
               "  tags=" + Arrays.toString(tags) + ",\n" +
               "  scores=" + scores + ",\n" +
               "  address=" + address + "\n" +
               "}";
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("тестовый пример\n");

        User user = new User();
        user.name = "Victoria";
        user.age = 20;
        user.rank = null; 
        user.tags = new String[]{"java", "lab"}; 
        
        List<Integer> scoreList = new ArrayList<>();
        scoreList.add(10);
        scoreList.add(20);
        user.scores = scoreList; 

        user.address = new Address();
        user.address.city = "St. Petersburg"; 

        String json = SimpleJson.toJson(user);
        System.out.println("1. В JSON:");
        System.out.println(json);

        Map<String, Object> map = SimpleJson.fromJson(json);
        System.out.println("\n2. Из JSON Map<String, Object>:");
        System.out.println(map);
        System.out.println("Тип поля scores в Map: " + map.get("scores").getClass().getSimpleName());

        User parsedUser = SimpleJson.fromJson(json, User.class);
        System.out.println("\n3. Из JSON в класс User:");
        System.out.println(parsedUser);
        
        if (parsedUser.scores != null && !parsedUser.scores.isEmpty()) {
            System.out.println("\nвсе восстановлено корректно");
        }
    }
}