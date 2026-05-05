import server.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = Server.builder()
                .host("localhost")
                .port(8080)
                .threads(4)
                .virtualThreads(false)
                .build();

        server.get("/health", request ->
                Server.Response.ok("I'm healthy!")
        );

        server.post("/message", request ->
                Server.Response.ok("POST: " + request.bodyAsString())
        );

        server.put("/message", request ->
                Server.Response.ok("PUT: " + request.bodyAsString())
        );

        server.patch("/message", request ->
                Server.Response.ok("PATCH: " + request.bodyAsString())
        );

        server.delete("/message", request ->
                Server.Response.ok("DELETE: message removed")
        );

        server.get("/query", request ->
                Server.Response.ok("Query: " + request.query())
        );

        server.start();
    }
}