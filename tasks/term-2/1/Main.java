import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import jsonparser.Json;

public class Main {
    
    public static void main(String[] args) throws IOException {

        Example example = Json.parseToObject(Files.readString(Paths.get("./example.json")), Example.class);

        System.out.println(example.str);
        System.out.println(example.integer);
        System.out.println(example.bool);
        for (Object item : example.inner.array) System.out.println(item);
        System.out.println(example.inner.nullable);
    
    }

    public static class Example {
    
        public String str;
        public int integer;
        public boolean bool;
        public Inner inner;
    
    }

    public static class Inner {
        
       public ArrayList<Object> array;
       public Object nullable;
    
    }

}
