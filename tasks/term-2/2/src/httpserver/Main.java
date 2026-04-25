package httpserver;

import java.util.Map;

public class Main {
    
    public static void main(String[] args) throws Exception {
        
        Server webServer = Server.configure()
                .address("localhost", 8800)
                .workers(6)
                .virtual(false)
                .build();
        
        // GET
        webServer.on("GET", "/welcome", req -> {
            String name = req.getQueryParams().getOrDefault("name", "Guest");
            String response = String.format("Hello, %s! Your method: %s", name, req.getMethod());
            return new Response().status(200).text(response);
        });
        
        // POST
        webServer.on("POST", "/receive", req -> {
            String received = req.getBodyText();
            System.out.println("Received: " + received);
            return new Response().status(201).text("Accepted: " + received);
        });
        
        // PUT
        webServer.on("PUT", "/replace", req -> {
            String data = req.getBodyText();
            return new Response().status(200).text("Replaced with: " + data);
        });
        
        // PATCH
        webServer.on("PATCH", "/modify", req -> {
            String changes = req.getBodyText();
            return new Response().status(200).text("Modified with: " + changes);
        });
        
        // DELETE
        webServer.on("DELETE", "/remove", req -> {
            return new Response().status(204).text("");
        });
        
        // Headers access
        webServer.on("GET", "/info", req -> {
            Map<String, String> headers = req.getHeaders();
            StringBuilder sb = new StringBuilder("Request headers:\n");
            for (Map.Entry<String, String> h : headers.entrySet()) {
                sb.append("  ").append(h.getKey()).append(": ").append(h.getValue()).append("\n");
            }
            return new Response().status(200).text(sb.toString());
        });
        
        // Multipart bonus
        webServer.on("POST", "/upload", req -> {
            StringBuilder report = new StringBuilder("Multipart data:\n\n");
            
            for (FormPart part : req.getFormParts()) {
                if (part.isFile()) {
                    report.append("File: ").append(part.getFilename())
                          .append(" (").append(part.getBytes().length).append(" bytes)\n");
                } else {
                    report.append("Field: ").append(part.getName())
                          .append(" = ").append(part.getText()).append("\n");
                }
            }
            
            return new Response().status(200).text(report.toString());
        });
        
        webServer.ignite();
    }
}
