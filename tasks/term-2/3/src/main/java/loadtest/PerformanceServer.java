package loadtest;

import com.google.gson.Gson;
import jsonparser.Json;
import server.HttpMethod;
import server.HttpServer;
import server.HttpServerBuilder;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerformanceServer {

    private static final ConcurrentHashMap<Long, Long> cache = new ConcurrentHashMap<>();
    private static volatile Connection dbConnection;

    public static class Req1Body { public String data; }
    public static class Req2Body { public long a; }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: java -jar server.jar <port> <virtual|classic> <own|gson>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        boolean useVirtual = args[1].equalsIgnoreCase("virtual");
        boolean useOwnParser = args[2].equalsIgnoreCase("own");

        initDb();

        HttpServer server = new HttpServerBuilder()
                .host("0.0.0.0")
                .port(port)
                .isVirtual(useVirtual)
                .threadCount(200)
                .build();

        Gson gson = new Gson();

        server.addListener("/req1", HttpMethod.POST, (req, resp) -> {
            try {
                String body = req.getBodyAsString();
                String data;
                if (useOwnParser) {
                    data = Json.parse(body, Req1Body.class).data;
                } else {
                    data = gson.fromJson(body, Req1Body.class).data;
                }

                storeInDb(data);
                String retrieved = retrieveFromDb();

                String json;
                if (useOwnParser) {
                    json = Json.toJson(Map.of("stored", data, "retrieved", retrieved));
                } else {
                    json = gson.toJson(Map.of("stored", data, "retrieved", retrieved));
                }
                resp.setStatus(200, "OK")
                    .addHeader("Content-Type", "application/json")
                    .setBody(json);
            } catch (Exception e) {
                resp.setStatus(500, "Internal Server Error").setBody(e.getMessage());
            }
        });

        server.addListener("/req2", HttpMethod.POST, (req, resp) -> {
            try {
                String body = req.getBodyAsString();
                long a;
                if (useOwnParser) {
                    a = Json.parse(body, Req2Body.class).a;
                } else {
                    a = gson.fromJson(body, Req2Body.class).a;
                }

                long result = cache.computeIfAbsent(a, key -> {
                    long sum = 0;
                    for (long i = 1; i <= key; i++) sum += i * i;
                    return sum;
                });

                String json;
                if (useOwnParser) {
                    json = Json.toJson(Map.of("result", result));
                } else {
                    json = gson.toJson(Map.of("result", result));
                }
                resp.setStatus(200, "OK")
                    .addHeader("Content-Type", "application/json")
                    .setBody(json);
            } catch (Exception e) {
                resp.setStatus(500, "Internal Server Error").setBody(e.getMessage());
            }
        });

        server.start();
        System.out.println("Port:    " + port);
        System.out.println("Threads: " + (useVirtual ? "virtual" : "classic (pool=200)"));
        System.out.println("Parser:  " + (useOwnParser ? "own (lab-1)" : "Gson"));

        Thread.currentThread().join();
    }

    private static synchronized void initDb() throws SQLException {
        dbConnection = DriverManager.getConnection("jdbc:sqlite:loadtest.db");
        try (Statement stmt = dbConnection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS entries " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT NOT NULL)"
            );
        }
    }

    private static synchronized void storeInDb(String data) throws SQLException {
        try (PreparedStatement ps = dbConnection.prepareStatement(
                "INSERT INTO entries (data) VALUES (?)")) {
            ps.setString(1, data);
            ps.executeUpdate();
        }
    }

    private static synchronized String retrieveFromDb() throws SQLException {
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT data FROM entries ORDER BY id DESC LIMIT 1")) {
            return rs.next() ? rs.getString("data") : "";
        }
    }
}
