import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class lab9{

    public static void main(String[] args){
        List<String> list = List.of("apple", "apple", "banana", "grape", "pineapple", 
                                "apple", "banana", "apple", "grape", "banana");

        var map = getmap(list);
        for (var key : map.keySet()) {
            System.out.println(key, ", ", map.get(key));
        }
    }

    public static Map<Object,Integer> getmap(List<String> list){
        return list.stream()
            .collect(Collectors.groupingBy(
                x -> x
            ))
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, x-> x.getValue().size())
        );
    }
}