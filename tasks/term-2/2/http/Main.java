package http;

import http.HttpServer;
import http.HttpResponse;

public class Main {
    public static void main(String[] args) {
       
        HttpServer server = new HttpServer("localhost", 8081, 10, true);

      
        server.addHandler("GET", "/hello", request -> {
            return new HttpResponse(200, "OK", "Hello from GET!");
        });

    
        server.addHandler("POST", "/data", request -> {
            String body = request.getBody();
            System.out.println("Received body: " + body);
            return new HttpResponse(201, "Created", "Data received: " + body);
        });

      
        server.addHandler("POST", "/upload", request -> {
            System.out.println("Parsed Form Data: " + request.getFormData());
            String responseBody = "Received fields: ";
            if (request.getFormData().containsKey("username")) {
                responseBody += "Username = " + request.getFormData().get("username") + "; ";
            }
            if (request.getFormData().containsKey("document")) {
                responseBody += "Document text = " + request.getFormData().get("document");
            }
            return new HttpResponse(200, "OK", responseBody);
        });

   
        server.addHandler("DELETE", "/delete", request -> {
            return new HttpResponse(200, "OK", "Item deleted");
        });

       
        server.start();
    }
}