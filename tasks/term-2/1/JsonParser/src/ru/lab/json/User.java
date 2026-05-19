package ru.lab.json;

import java.util.List;
import java.util.Arrays;

public class User {
    public String name;         
    public int age;             
    public Long rank;        
    public String[] tags;       
    public List<Integer> scores; 
    public Address address;     

    public User() {} 

    @Override
    public String toString() {
        return "User{\n" +
               "  name='" + name + "',\n" +
               "  age=" + age + ",\n" +
               "  rank=" + rank + ",\n" +
               "  tags=" + Arrays.toString(tags) + ",\n" +
               "  scores=" + scores + ",\n" +
               "  address=" + address + "\n" +
               "}";
    }
}