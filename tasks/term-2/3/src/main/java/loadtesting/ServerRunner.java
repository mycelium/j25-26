package loadtesting;

import com.google.gson.Gson;
import httpserver.HttpMethod;
import httpserver.HttpServer;
import json.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Map;

/**
 * Starts the lab-2 HTTP server with two endpoints.
 * useOwnJson=true  → lab-1 JSON parser
 * useOwnJson=false → Gson
 */
public class ServerRunner {

    private static final int    WORKER_THREADS = 8;
    private static final String DB_FILE        = "data/storage.db";

    private final HttpServer server;
    private final boolean    useOwnJson;

    // One shared instance each — both are thread-safe
    private final Json ownJson = new Json();
    private final Gson gson    = new Gson();

    public ServerRunner(String host, int port, boolean virtualThreads, boolean useOwnJson)
            throws SQLException, IOException {
        this.useOwnJson = useOwnJson;
        prepareDatabase();
        this.server = new HttpServer.Builder()
                .host(host)
                .port(port)
                .threadCount(WORKER_THREADS)
                .isVirtual(virtualThreads)
                .build();
        addRoutes();
    }

    public void start() throws IOException                       { server.start(); }
    public void stop()  throws IOException, InterruptedException { server.stop();  }

    // ── Routes ─────────────────────────────────────────────────────────────────

    private void addRoutes() {
        // I/O-bound: parse JSON, write to SQLite, read back
        server.addRoute(HttpMethod.POST, "/request1", (req, res) -> {
            try {
                String value     = extractString(req.getBody(), "value");
                long   id        = insertRecord(value);
                String retrieved = selectRecord(id);
                res.status(200, "OK").body(retrieved);
            } catch (Exception e) {
                res.status(500, "Internal Server Error")
                        .body("request1 failed: " + e.getMessage());
            }
        });

        // CPU-bound: parse JSON, compute Fibonacci, return JSON
        server.addRoute(HttpMethod.POST, "/request2", (req, res) -> {
            try {
                int    n      = extractInt(req.getBody(), "n");
                long   fib    = fibonacci(n);
                String result = serializeResult(n, fib);
                res.status(200, "OK")
                        .header("Content-Type", "application/json; charset=utf-8")
                        .body(result);
            } catch (Exception e) {
                res.status(500, "Internal Server Error")
                        .body("request2 failed: " + e.getMessage());
            }
        });
    }

    // ── JSON helpers ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String extractString(String body, String key) {
        if (useOwnJson) {
            return String.valueOf(ownJson.parseToMap(body).get(key));
        } else {
            Map<String, Object> map = gson.fromJson(body, Map.class);
            return String.valueOf(map.get(key));
        }
    }

    @SuppressWarnings("unchecked")
    private int extractInt(String body, String key) {
        if (useOwnJson) {
            return ((Number) ownJson.parseToMap(body).get(key)).intValue();
        } else {
            Map<String, Object> map = gson.fromJson(body, Map.class);
            return ((Number) map.get(key)).intValue();
        }
    }

    private String serializeResult(int n, long result) {
        Map<String, Object> data = Map.of("n", n, "result", result);
        return useOwnJson ? ownJson.toJson(data) : gson.toJson(data);
    }

    // ── SQLite ─────────────────────────────────────────────────────────────────

    private void prepareDatabase() throws SQLException, IOException {
        Files.createDirectories(Path.of("data"));
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("PRAGMA busy_timeout=5000");
            st.execute("""
                CREATE TABLE IF NOT EXISTS records (
                    id    INTEGER PRIMARY KEY AUTOINCREMENT,
                    value TEXT NOT NULL
                )
            """);
        }
    }

    private long insertRecord(String value) throws SQLException {
        String sql = "INSERT INTO records (value) VALUES (?)";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, value);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }
        }
    }

    private String selectRecord(long id) throws SQLException {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT value FROM records WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("value") : "";
            }
        }
    }

    private Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("PRAGMA busy_timeout=5000");
        }
        return c;
    }

    // ── Math ───────────────────────────────────────────────────────────────────

    private long fibonacci(int n) {
        if (n <= 1) return n;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) { long tmp = a + b; a = b; b = tmp; }
        return b;
    }
}