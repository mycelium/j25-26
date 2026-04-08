import java.util.*;

public class Main {
    public static void main(String[] args) {

        System.out.println("1) JSON в Map");
        String json1 = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
        Map<String, Object> map = JsonParser.parseToMap(json1);
        System.out.println("Map: " + map);
        System.out.println("Имя: " + map.get("name"));
        System.out.println("Возраст: " + map.get("age"));

        System.out.println("\n2) JSON в объект Person");
        String json2 = "{" +
                "\"name\":\"Alice\"," +
                "\"age\":25," +
                "\"height\":1.75," +
                "\"isStudent\":true," +
                "\"hobbies\":[\"reading\",\"swimming\",\"coding\"]," +
                "\"address\":{\"city\":\"Moscow\",\"street\":\"Tverskaya\",\"number\":10}," +
                "\"scores\":[95,87,92]" +
                "}";

        Person person = JsonParser.parseToObject(json2, Person.class);
        System.out.println("Объект Person: " + person);

        System.out.println("\n3) Объект в JSON");
        String jsonFromObject = JsonParser.toJson(person);
        System.out.println("JSON из объекта: " + jsonFromObject);

        System.out.println("\n4) Массивы и коллекции");
        String json3 = "{\"numbers\":[1,2,3,4,5],\"names\":[\"Tom\",\"Jerry\"]}";
        Map<String, Object> map2 = JsonParser.parseToMap(json3);
        System.out.println("Спарсенный Map: " + map2);

        List<Integer> numbers = (List<Integer>) map2.get("numbers");
        System.out.println("Первый элемент массива: " + numbers.get(0));

        System.out.println("\n5) Null значения");
        String json4 = "{\"name\":null,\"age\":0}";
        Map<String, Object> map3 = JsonParser.parseToMap(json4);
        System.out.println("Map с null: " + map3);

        System.out.println("\n6) Простые типы");
        String simpleJson = "{\"text\":\"Hello\",\"number\":42,\"flag\":true}";
        Map<String, Object> simpleMap = JsonParser.parseToMap(simpleJson);
        System.out.println("Простой Map: " + simpleMap);
    }
}