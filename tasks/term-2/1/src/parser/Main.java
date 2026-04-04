package parser;
import java.util.*;

public class Main {

	static class User {
		private String name;
		private int age;
		private boolean works;
		private List<String> tags;
		private Address address;

		public User(String name, int age, boolean works, List<String> tags, Address address) {
			this.name = name;
			this.age = age;
			this.works = works;
			this.tags = tags;
			this.address = address;
		}

		public User() {}

		public String toString() {
			return "User{name=" + name + ", age=" + age + ", works=" + works + ", tags=" + tags + ", address=" + address
					+ "}";
		}
	}

	static class Address {
		private String city;
		private String street;
		private int zip;

		public Address(String city, String street, int zip) {
			this.city = city;
			this.street = street;
			this.zip = zip;
		}

		public Address() {}

		public String toString() {
			return "Address{city=" + city + ", street=" + street + ", zip=" + zip + "}";
		}
	}

	public static void main(String[] args) {

		System.out.println("Testing:\n");

		// 1. примитивы и обёртки
        int intVal = 42;
        double doubleVal = 3.14;
        boolean boolVal = true;
        String strVal = "Hello";
        System.out.println("  int -> " + JSON.toJson(intVal));
        System.out.println("  double -> " + JSON.toJson(doubleVal));
        System.out.println("  boolean -> " + JSON.toJson(boolVal));
        System.out.println("  String -> " + JSON.toJson(strVal));
        System.out.println();

        // 2. массивы (примитивные и объектные)
        int[] intArray = {1, 2, 3};
        String[] strArray = {"a", "b", "c"};
        System.out.println("  int[] -> " + JSON.toJson(intArray));
        System.out.println("  String[] -> " + JSON.toJson(strArray));
        int[] parsedIntArray = JSON.parse(JSON.toJson(intArray), int[].class);
        System.out.println("  parsed int[] -> " + Arrays.toString(parsedIntArray));
        System.out.println();

        // 3. коллекции
        List<String> list = Arrays.asList("one", "two", "three");
        System.out.println("  List -> " + JSON.toJson(list));
        List<?> parsedList = JSON.parse(JSON.toJson(list), List.class);
        System.out.println("  parsed List -> " + parsedList);
        System.out.println();

        // 4. Map
        Map<String, Object> map = new HashMap<>();
        map.put("key1", 100);
        map.put("key2", "value");
        System.out.println("  Map -> " + JSON.toJson(map));
        Map<String, Object> parsedMap = JSON.parseToMap(JSON.toJson(map));
        System.out.println("  parsed Map -> " + parsedMap);
        System.out.println();

		// 5. вложенные объекты
		Address addr = new Address("Kaliningrad", "Zelenaya", 101000);
		User user = new User("Ivan", 25, true, Arrays.asList("programmer", "java"), addr);
		String userJson = JSON.toJson(user);
        System.out.println("  User JSON: " + userJson);
        User parsedUser = JSON.parse(userJson, User.class);
        System.out.println("  Parsed User: " + parsedUser);
        System.out.println();

		// 6. null
        System.out.println("  null -> " + JSON.toJson(null));
        System.out.println();

        // 7. массив объектов
        User[] users = {user, new User("Petr", 30, false, List.of("c++"), addr)};
        String usersJson = JSON.toJson(users);
        System.out.println("  User[] JSON: " + usersJson);
        User[] parsedUsers = JSON.parse(usersJson, User[].class);
        System.out.println("  Parsed User[]: " + Arrays.toString(parsedUsers));


	}
}