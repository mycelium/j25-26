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

		public User() {
		}

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

		public Address() {
		}

		public String toString() {
			return "Address{city=" + city + ", street=" + street + ", zip=" + zip + "}";
		}
	}

	public static void main(String[] args) {

		System.out.println("Testing:\n");

		Address addr = new Address("Kaliningrad", "Zelenaya", 101000);
		User user = new User("Ivan", 25, true, Arrays.asList("programmer", "java"), addr);

		System.out.println("Объект в JSON:");
		String json = JSON.toJson(user);
		System.out.println(json);
		System.out.println();

		System.out.println("JSON в Map:");
		Map<String, Object> map = JSON.parseToMap(json);
		for (Map.Entry<String, Object> e : map.entrySet()) {
			System.out.println(e.getKey() + " = " + e.getValue() + " ("
					+ (e.getValue() == null ? "null" : e.getValue().getClass().getSimpleName()) + ")");
		}
		System.out.println();

		System.out.println("JSON в User:");
		User parsedUser = JSON.parse(json, User.class);
		System.out.println(parsedUser);
		System.out.println();


	}
}