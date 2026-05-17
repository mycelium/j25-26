package http;

public class Main {
    public static void main(String[] args) {
        HttpServer server = new HttpServer("localhost", 8081, 10, true);

        // GET /hello
        server.addHandler("GET", "/hello", request -> {
            String name = request.getQueryParams().getOrDefault("name", "World");
            return new HttpResponse(200, "OK", "Hello, " + name + "!");
        });

        // POST /data
        server.addHandler("POST", "/data", request -> {
            String body = request.getBody();
            return new HttpResponse(201, "Created", "Data received: " + body);
        });

        // PUT /update
        server.addHandler("PUT", "/update", request ->
            new HttpResponse(200, "OK", "Item completely updated with: " + request.getBody()));

        // PATCH /patch
        server.addHandler("PATCH", "/patch", request ->
            new HttpResponse(200, "OK", "Item partially patched with: " + request.getBody()));

        // DELETE /delete
        server.addHandler("DELETE", "/delete", request ->
            new HttpResponse(200, "OK", "Item deleted"));

        // POST /upload — multipart form data
        server.addHandler("POST", "/upload", request -> {
            var form = request.getFormData();
            String response = "Received fields: ";
            if (form.containsKey("username")) response += "Username = " + form.get("username") + "; ";
            if (form.containsKey("document")) response += "Document text = " + form.get("document");
            return new HttpResponse(200, "OK", response);
        });

        server.start();
    }
}
