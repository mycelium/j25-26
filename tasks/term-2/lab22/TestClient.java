
public class TestClient { 
    public static void main(String[] args) {
        HttpClient client = new HttpClient("localhost", 8080);
        client.get("/api/users");
        client.post("/api/users", "{\"new\":\"Info\"}"); 
        client.get("/api/users");  
        client.put("/api/users/update", "{\"id\":1234,\"some\":\"info\"}");   
        client.delete("/api/users/delete?id=1234");   
        client.get("/api/users");
    }
}