package myhttpserver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Main {
    public static void main(String[] args) throws IOException {
        
        Server server = new Server("localhost", 8085, 4, true);

     
        server.addHandler(Server.Method.GET, "/", (req, resp) -> {
            resp.setBody("Hello world from server!");
        });

        
        server.addHandler(Server.Method.GET, "/hello", (req, resp) -> {
            String name = req.getQueryParams().getOrDefault("name", "user_name");
            resp.setBody("Hello, " + name + "!");
        });

        
        server.addHandler(Server.Method.POST, "/register", (req, resp) -> {
            var params = req.getFormData();
            String name = params.get("name");
            String login = params.get("login");
            String password = params.get("password");
            if (name == null || login == null || password == null) {
                resp.setStatus(400);
                resp.setBody("Missing fields");
                return;
            }
            Files.writeString(Path.of("users.txt"),
                    name + " " + login + " " + password + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            resp.setBody("User registered: " + name);
        });

      
        server.addHandler(Server.Method.PUT, "/update", (req, resp) -> {
            String bodyText = new String(req.getBody(), StandardCharsets.UTF_8);
            int len = req.getBody().length;
            if (len == 0) {
                resp.setBody("Nothing to update");
            } else {
                resp.setBody("Updated with body (length " + len + "): " + bodyText);
            }
        });

        server.addHandler(Server.Method.PATCH, "/patch", (req, resp) -> {
            resp.setBody("Patched");
        });

        server.addHandler(Server.Method.DELETE, "/delete", (req, resp) -> {
            resp.setBody("Deleted");
        });

        server.start();
        System.out.println("Server started at http://localhost:8085");
        System.out.println("Press Enter to stop the server...");
        System.in.read();         
        server.stop();
        System.out.println("Server stopped.");
    }
}