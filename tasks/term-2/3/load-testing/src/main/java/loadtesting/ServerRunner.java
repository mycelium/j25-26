package loadtesting;

import com.google.gson.Gson;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpServer;
import json.Json;
import json.JsonConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Map;

public class ServerRunner {

    private static final int    THREAD_COUNT = 8;
    private static final String DB_PATH      = "data/storage.db";

    private static final JsonConfig JSON_CFG =
            new JsonConfig(false, true, false);

    private final HttpServer server;
    private final boolean    useOwnParser;
    private final Gson       gson = new Gson();   // Gson is thread-safe

    public ServerRunner(String host, int port, boolean isVirtual, boolean useOwnParser)
            throws SQLException, IOException {
        this.useOwnParser = useOwnParser;

        initDatabase();

        this.server = new HttpServer(host, port, THREAD_COUNT, isVirtual);
        registerHandlers();
    }

    public void start() throws IOException  { server.start(); }
    public void stop()  throws IOException, InterruptedException { server.stop(); }

    private void registerHandlers() {
        server.addHandler(HttpMethod.POST, "/request1", this::handleRequest1);
        server.addHandler(HttpMethod.POST, "/request2", this::handleRequest2);
    }


    private HttpResponse handleRequest1(HttpRequest request) {
        try {
            String body  = request.getBody();
            String value = parseValue(body);

            long   id        = writeToDb(value);
            String retrieved = readFromDb(id);

            return HttpResponse.ok(retrieved);

        } catch (Exception e) {
            return HttpResponse.internalServerError("Request1 error: " + e.getMessage());
        }
    }

    private HttpResponse handleRequest2(HttpRequest request) {
        try {
            String body = request.getBody();
            int    n    = parseN(body);

            long   result       = fibonacci(n);
            String responseJson = buildJson(n, result);

            return new HttpResponse(200, "OK",
                    Map.of("Content-Type", "application/json; charset=UTF-8"),
                    responseJson);

        } catch (Exception e) {
            return HttpResponse.internalServerError("Request2 error: " + e.getMessage());
        }
    }

    private String parseValue(String body) {
        if (useOwnParser) {
            Json json = new Json(JSON_CFG);           // new instance per call!
            Map<String, Object> map = json.toMap(body);
            return String.valueOf(map.get("value"));
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = gson.fromJson(body, Map.class);
            return String.valueOf(map.get("value"));
        }
    }

    private int parseN(String body) {
        if (useOwnParser) {
            Json json = new Json(JSON_CFG);           // new instance per call!
            Map<String, Object> map = json.toMap(body);
            return Integer.parseInt(String.valueOf(map.get("n")));
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = gson.fromJson(body, Map.class);
            return ((Number) map.get("n")).intValue();
        }
    }

    private String buildJson(int n, long result) {
        if (useOwnParser) {
            Json json = new Json(JSON_CFG);
            return json.toJson(Map.of("n", n, "result", result));
        } else {
            return gson.toJson(Map.of("n", n, "result", result));
        }
    }

    private void initDatabase() throws SQLException, IOException {
        Files.createDirectories(Path.of("data"));
        try (Connection conn = getConnection();
             Statement  st   = conn.createStatement()) {
            // WAL allows concurrent readers + one writer without "database is locked"
            st.execute("PRAGMA journal_mode=WAL");
            // Wait up to 5 s before giving up on a locked DB
            st.execute("PRAGMA busy_timeout=5000");
            st.execute("""
                CREATE TABLE IF NOT EXISTS records (
                    id    INTEGER PRIMARY KEY AUTOINCREMENT,
                    value TEXT NOT NULL
                )
            """);
        }
    }

    private long writeToDb(String value) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO records (value) VALUES (?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, value);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }
        }
    }

    private String readFromDb(long id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT value FROM records WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("value") : "";
            }
        }
    }

    private Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        // Apply busy_timeout on every new connection
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA busy_timeout=5000");
            st.execute("PRAGMA journal_mode=WAL");
        }
        return conn;
    }

    private long fibonacci(int n) {
        if (n <= 1) return n;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) { long c = a + b; a = b; b = c; }
        return b;
    }
}