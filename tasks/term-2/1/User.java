public class User {
    public String name;
    public int age;
    public boolean active;

    public Address address;

    public java.util.List<String> skills;

    public User() {}
}

class Address {
    public String city;
    public String country;

    public Address() {}
}