package lab1;

import java.util.List;

public class User {
    public long id;
    public String name;
    public int age;
    public Double rating;
    public Boolean active;
    public Address address;
    public List<String> tags;
    public List<Address> previousAddresses;
    public int[] scores;
    public transient String temporaryComment;

    public User() {
    }

    public User(long id,
                String name,
                int age,
                Double rating,
                Boolean active,
                Address address,
                List<String> tags,
                List<Address> previousAddresses,
                int[] scores) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.rating = rating;
        this.active = active;
        this.address = address;
        this.tags = tags;
        this.previousAddresses = previousAddresses;
        this.scores = scores;
    }
}
