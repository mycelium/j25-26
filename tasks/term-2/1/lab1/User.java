package lab1;

import java.util.List;

public class User {
    public String name;
    public int age;
    public Boolean active;
    public Address address;
    public List<String> tags;
    public int[] scores;

    public User() {
    }

    public User(String name, int age, Boolean active, Address address, List<String> tags, int[] scores) {
        this.name = name;
        this.age = age;
        this.active = active;
        this.address = address;
        this.tags = tags;
        this.scores = scores;
    }
}