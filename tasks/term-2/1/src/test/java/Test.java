import ru.derikey.json.JsonMapper;

public class Test {
    public static void main(String[] args) {
        JsonMapper mapper = JsonMapper.builder().build();
        String json = "{\"name\":\"Erik\",\"age\":20}";
        var map = mapper.parseToMap(json);
        System.out.println(map.get("name"));
        System.out.println(map.get("age"));
    }
}