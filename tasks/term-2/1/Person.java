import java.util.Arrays;
import java.util.List;

class Person {
    public String name;
    public int age;
    public double height;
    public boolean isStudent;
    public List<String> hobbies;
    public Address address;
    public int[] scores;

    public Person() {
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age +
                ", height=" + height + ", isStudent=" + isStudent +
                ", hobbies=" + hobbies + ", address=" + address +
                ", scores=" + Arrays.toString(scores) + "}";
    }
}
