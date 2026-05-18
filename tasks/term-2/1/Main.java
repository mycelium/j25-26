import json.JsonLib;
import java.util.*;

public class Main {

    static class Product {
        private String name;
        private double price;
        private List<String> tags;
        private boolean inStock;

        public Product() {}

        // getters/setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public boolean isInStock() { return inStock; }
        public void setInStock(boolean inStock) { this.inStock = inStock; }

        @Override
        public String toString() {
            return "Product{name='%s', price=%.2f, tags=%s, inStock=%b}".formatted(name, price, tags, inStock);
        }
    }

    public static void main(String[] args) {
        String input = """
            {
                "name": "Keyboard",
                "price": 89.99,
                "tags": ["electronics", "input"],
                "inStock": true
            }
            """;

        Product p = JsonLib.deserialize(input, Product.class);
        System.out.println("Loaded: " + p);

        String output = JsonLib.serialize(p);
        System.out.println("Serialized: " + output);

        Map<String, Object> map = JsonLib.deserializeToMap("""
            {"ok": true, "count": 5, "items": ["a", "b"]}
            """);
        System.out.println("Map: " + map);
    }
}