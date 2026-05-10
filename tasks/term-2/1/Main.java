import java.util.*;

public class Main {

    static class Address {
        public String city;
        public String street;

        @Override
        public String toString() {
            return "Address{city='" + city + "', street='" + street + "'}";
        }
    }

    static class User {
        public int           id;
        public String        name;
        public double        salary;
        public String        nickname;
        public Address       address;
        public List<String>  roles;

        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', salary=" + salary
                    + ", nickname=" + nickname + ", address=" + address + ", roles=" + roles + '}';
        }
    }

    static class Student extends User {
        public String university;
        public int    year;

        @Override
        public String toString() {
            return "Student{" + super.toString()
                    + ", university='" + university + "', year=" + year + '}';
        }
    }

    public static void main(String[] args) {

        System.out.println("1. PARSE TO MAP & LIST");
        String jsonMapStr   = "{\"name\": \"Alice\", \"age\": 25, \"active\": true}";
        String jsonArrayStr = "[1, 2.5, \"Hello World\", null]";

        try {
            JsonLib lib = new JsonLib();

            Map<String, Object> map = lib.toMap(jsonMapStr);
            System.out.println("   JSON String -> Map: " + map);

            List<Object> list = lib.toList(jsonArrayStr);
            System.out.println("   JSON String -> List: " + list);
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("2. JSON -> Java Object");

        String jsonComplex = """
                {
                    "id": 101,
                    "name": "John Doe",
                    "salary": 5000.75,
                    "nickname": null,
                    "address": {
                        "city": "Moscow",
                        "street": "Tverskaya"
                    },
                    "roles": ["admin", "user"]
                }
                """;

        try {
            JsonLib lib = new JsonLib();
            User user = lib.fromJson(jsonComplex, User.class);
            System.out.println("   Successfully parsed User object!");
            System.out.println("   User ID: "       + user.id);
            System.out.println("   User Name: "     + user.name);
            System.out.println("   User Salary: "   + user.salary);
            System.out.println("   User Nickname: " + user.nickname);
            System.out.println("   Address City: "  + (user.address != null ? user.address.city : "null"));
            System.out.println("   Roles: "         + user.roles);
        } catch (Exception e) {
            System.out.println("   Error during parsing: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("3. Java Object -> JSON");

        User newUser   = new User();
        newUser.id     = 202;
        newUser.name   = "Bob Smith";
        newUser.salary = 3000.0;

        Address addr = new Address();
        addr.city   = "London";
        addr.street = "Baker Street";
        newUser.address = addr;

        newUser.roles = new ArrayList<>();
        newUser.roles.add("user");

        try {
            JsonLib lib = new JsonLib();
            String jsonOutput = lib.toJson(newUser);
            System.out.println("   Generated JSON (nulls skipped):");
            System.out.println("   " + jsonOutput);

            JsonLib libWithNulls = new JsonLib(JsonConfig.builder().serializeNulls(true).build());
            String jsonOutputWithNulls = libWithNulls.toJson(newUser);
            System.out.println("   Generated JSON (serializeNulls=true):");
            System.out.println("   " + jsonOutputWithNulls);
        } catch (Exception e) {
            System.out.println("   Error during serialization: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("4. EDGE CASES");

        Map<String, Object> edgeCaseMap = new HashMap<>();
        edgeCaseMap.put("intVal",     42);
        edgeCaseMap.put("doubleVal",  3.14);
        edgeCaseMap.put("booleanVal", false);
        edgeCaseMap.put("nullVal",    null);
        edgeCaseMap.put("nested",     Map.of("key", "value"));

        try {
            JsonLib lib = new JsonLib(JsonConfig.builder().serializeNulls(true).build());
            String jsonEdge = lib.toJson(edgeCaseMap);
            System.out.println("   Complex Map -> JSON: " + jsonEdge);

            Map<String, Object> parsedBack = lib.toMap(jsonEdge);
            System.out.println("   JSON -> Map (Round Trip): " + parsedBack);
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("5. GENERIC TYPES via TypeToken");

        try {
            JsonLib lib = new JsonLib();

            String listStrJson = "[\"alpha\", \"beta\", \"gamma\"]";
            List<String> strList = lib.fromJson(listStrJson, new TypeToken<List<String>>(){});
            System.out.println("   List<String>: " + strList);

            String listIntJson = "[10, 20, 30, 40]";
            List<Integer> intList = lib.fromJson(listIntJson, new TypeToken<List<Integer>>(){});
            System.out.println("   List<Integer>: " + intList);

            String mapIntJson = "{\"one\": 1, \"two\": 2, \"three\": 3}";
            Map<String, Integer> typedMap = lib.fromJson(mapIntJson, new TypeToken<Map<String, Integer>>(){});
            System.out.println("   Map<String, Integer>: " + typedMap);

            User u1 = new User(); u1.id = 1; u1.name = "Ann"; u1.salary = 100.0;
            u1.roles = List.of("admin");
            User u2 = new User(); u2.id = 2; u2.name = "Ben"; u2.salary = 200.0;
            u2.roles = List.of("user");
            String listUserJson = lib.toJson(List.of(u1, u2));
            List<User> userList = lib.fromJson(listUserJson, new TypeToken<List<User>>(){});
            System.out.println("   List<User> size: " + userList.size());
            System.out.println("   List<User>[0]:   " + userList.get(0));
            System.out.println("   List<User>[1]:   " + userList.get(1));
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("6. JsonConfig — ignoreUnknownFields");

        String jsonWithExtra = "{\"id\": 5, \"name\": \"Eve\", \"salary\": 0.0, "
                + "\"country\": \"RU\", \"age\": 99}";

        try {
            JsonLib strict = new JsonLib();
            User u = strict.fromJson(jsonWithExtra, User.class);
            System.out.println("   strict: исключение на лишнее поле — ожидалось, но распарсил: " + u);
        } catch (RuntimeException e) {
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            System.out.println("   strict: исключение на лишнее поле — " + msg);
        }

        try {
            JsonLib lenient = new JsonLib(JsonConfig.builder().ignoreUnknownFields(true).build());
            User u = lenient.fromJson(jsonWithExtra, User.class);
            System.out.println("   lenient: лишние поля проигнорированы, name=" + u.name);
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("7. JsonConfig — failOnDuplicateKeys");

        String dupJson = "{\"x\": 1, \"x\": 2}";

        try {
            JsonLib strict = new JsonLib();
            Map<String, Object> m = strict.toMap(dupJson);
            System.out.println("   strict: неожиданно распарсил — " + m);
        } catch (RuntimeException e) {
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            System.out.println("   strict: исключение на дубликат — " + msg);
        }

        try {
            JsonLib lenient = new JsonLib(JsonConfig.builder().failOnDuplicateKeys(false).build());
            Map<String, Object> m = lenient.toMap(dupJson);
            System.out.println("   lenient: x=" + m.get("x") + " (последнее значение победило)");
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("8. INHERITANCE — Student extends User");

        try {
            JsonLib lib = new JsonLib(JsonConfig.builder().serializeNulls(true).build());

            Student s    = new Student();
            s.id         = 303;
            s.name       = "Alexey";
            s.salary     = 0.0;
            s.university = "ITMO";
            s.year       = 3;
            s.roles      = List.of("student");
            s.address    = null;

            String stuJson  = lib.toJson(s);
            System.out.println("   Student -> JSON: " + stuJson);

            Student stuBack = lib.fromJson(stuJson, Student.class);
            System.out.println("   JSON -> Student: " + stuBack);
            System.out.println("   name (из User):  " + stuBack.name);
            System.out.println("   university:      " + stuBack.university);
            System.out.println("   year:            " + stuBack.year);
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("9. NUMBERS — экспонента, long, int");

        try {
            JsonLib lib = new JsonLib();
            List<Object> nums = lib.toList("[1e3, 2.5E-1, 9000000000, 42, -7, 0]");
            System.out.println("   1e3        → " + nums.get(0) + " (" + nums.get(0).getClass().getSimpleName() + ")");
            System.out.println("   2.5E-1     → " + nums.get(1) + " (" + nums.get(1).getClass().getSimpleName() + ")");
            System.out.println("   9000000000 → " + nums.get(2) + " (" + nums.get(2).getClass().getSimpleName() + ")");
            System.out.println("   42         → " + nums.get(3) + " (" + nums.get(3).getClass().getSimpleName() + ")");
            System.out.println("   -7         → " + nums.get(4) + " (" + nums.get(4).getClass().getSimpleName() + ")");
            System.out.println("   0          → " + nums.get(5) + " (" + nums.get(5).getClass().getSimpleName() + ")");
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("10. STRING ESCAPING");

        try {
            JsonLib lib = new JsonLib();
            System.out.println("   кавычки:    " + lib.toJson("say \"hello\""));
            System.out.println("   слеш:       " + lib.toJson("a\\b"));
            System.out.println("   перенос:    " + lib.toJson("line1\nline2"));
            System.out.println("   табуляция:  " + lib.toJson("a\tb"));
            System.out.println("   ctrl<0x20>: " + lib.toJson("ctrl\u0001char"));

            List<Object> unicodeList = lib.toList("[\"\\u0041\\u0042\\u0043\"]");
            System.out.println("   \\u0041\\u0042\\u0043 → " + unicodeList.get(0));
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("11. ARRAYS");

        try {
            JsonLib lib = new JsonLib();
            System.out.println("   int[]    → " + lib.toJson(new int[]{1, 2, 3}));
            System.out.println("   double[] → " + lib.toJson(new double[]{1.1, 2.2}));
            System.out.println("   String[] → " + lib.toJson(new String[]{"a", "b", "c"}));
            System.out.println("   пустой[] → " + lib.toJson(new int[]{}));
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");

        System.out.println("12. ROUND-TRIP");

        try {
            JsonLib lib = new JsonLib(JsonConfig.builder().serializeNulls(true).build());

            User original       = new User();
            original.id         = 999;
            original.name       = "Round Trip";
            original.salary     = 12345.67;
            original.nickname   = "rt";
            original.roles      = new ArrayList<>(List.of("dev", "ops"));
            original.address    = new Address();
            original.address.city   = "Novosibirsk";
            original.address.street = "Mira";

            String json     = lib.toJson(original);
            User   restored = lib.fromJson(json, User.class);

            System.out.println("   id совпадает:           " + (original.id     == restored.id));
            System.out.println("   name совпадает:         " + original.name.equals(restored.name));
            System.out.println("   salary совпадает:       " + (original.salary == restored.salary));
            System.out.println("   nickname совпадает:     " + original.nickname.equals(restored.nickname));
            System.out.println("   address.city совпадает: " + original.address.city.equals(restored.address.city));
            System.out.println("   roles совпадает:        " + original.roles.equals(restored.roles));
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println("\n------------------------------------------------------\n");
        System.out.println("Done.");
    }
}
