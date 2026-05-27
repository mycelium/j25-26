package loadtest;

import com.httpserver.HttpResponse;
import com.httpserver.HttpServer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * HTTP server pre-configured for the load-test.
 *
 * Request-1  POST /store
 *   - Parses JSON body  → extracts a "value" field
 *   - Appends it to a flat file (data/store.txt)
 *   - Reads back the last 5 lines and returns them as JSON
 *
 * Request-2  POST /compute
 *   - Parses JSON body  → extracts "a" and "b" fields
 *   - Computes fibonacci(a % 30) + b (pure in-memory work)
 *   - Returns result as JSON
 */
public class LoadTestServer {

    private static final Logger logger = Logger.getLogger(LoadTestServer.class.getName());

    private final HttpServer server;
    private final Path storeFile;
    private final JsonAdapter jsonAdapter;
    private final AtomicLong requestCounter = new AtomicLong(0);

    // -------------------------------------------------------------------------
    // Simple POJOs used for JSON (de)serialisation
    // -------------------------------------------------------------------------

    public static class StoreRequest {
        public String value = "";
        public int    id    = 0;
        public StoreRequest() {}
    }

    public static class StoreResponse {
        public boolean success;
        public long    totalStored;
        public String  lastLines;
        public StoreResponse() {}
    }

    public static class ComputeRequest {
        public int    a = 10;
        public long   b = 0;
        public String label = "";
        public ComputeRequest() {}
    }

    public static class ComputeResponse {
        public long   result;
        public String label;
        public long   requestId;
        public ComputeResponse() {}
    }

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public LoadTestServer(String host, int port, int threadCount,
                          boolean virtualThreads, JsonAdapter adapter) throws IOException {
        this.jsonAdapter = adapter;
        this.server = HttpServer.create(host, port);

        // Ensure data directory exists
        Path dataDir = Paths.get("data");
        Files.createDirectories(dataDir);
        this.storeFile = dataDir.resolve("store.txt");

        registerRoutes();
        server.start(threadCount, virtualThreads);
        logger.info("LoadTestServer ready on http://" + host + ":" + port
                    + " [threads=" + threadCount + ", virtual=" + virtualThreads
                    + ", parser=" + adapter + "]");
    }

    public void stop() throws IOException, InterruptedException {
        server.stop();
    }

    // -------------------------------------------------------------------------
    // Routes
    // -------------------------------------------------------------------------

    private void registerRoutes() {

        // ── GET /health ───────────────────────────────────────────────────────
        server.get("/health", req -> HttpResponse.ok("{\"status\":\"ok\"}"));

        // ── POST /store  (Request-1) ──────────────────────────────────────────
        // Parse JSON → write to file → read back → return JSON
        server.post("/store", req -> {
            try {
                String body = req.getBodyAsString();

                // Parse with configured adapter
                StoreRequest payload = jsonAdapter.fromJson(body, StoreRequest.class);

                // Append to file (synchronized to avoid interleaved writes)
                String line = payload.id + ":" + payload.value + "\n";
                synchronized (storeFile) {
                    Files.write(storeFile,
                                line.getBytes(StandardCharsets.UTF_8),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.APPEND);
                }

                // Read back last 5 lines
                String lastLines = readLastLines(storeFile, 5);

                // Build response
                StoreResponse resp = new StoreResponse();
                resp.success     = true;
                resp.totalStored = requestCounter.incrementAndGet();
                resp.lastLines   = lastLines;

                return HttpResponse.ok(jsonAdapter.toJson(resp));

            } catch (Exception e) {
                logger.warning("POST /store error: " + e.getMessage());
                return HttpResponse.internalServerError(
                    "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ── POST /compute  (Request-2) ────────────────────────────────────────
        // Parse JSON → in-memory computation → return JSON
        server.post("/compute", req -> {
            try {
                String body = req.getBodyAsString();

                ComputeRequest payload = jsonAdapter.fromJson(body, ComputeRequest.class);

                // Pure in-memory: fibonacci + addition
                long fibResult = fibonacci(Math.abs(payload.a) % 30);
                long result    = fibResult + payload.b;

                ComputeResponse resp = new ComputeResponse();
                resp.result    = result;
                resp.label     = payload.label + "_processed";
                resp.requestId = requestCounter.incrementAndGet();

                return HttpResponse.ok(jsonAdapter.toJson(resp));

            } catch (Exception e) {
                logger.warning("POST /compute error: " + e.getMessage());
                return HttpResponse.internalServerError(
                    "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Read the last {@code n} lines of a file. */
    private static String readLastLines(Path file, int n) throws IOException {
        if (!Files.exists(file)) return "";
        java.util.List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int from = Math.max(0, lines.size() - n);
        return String.join("|", lines.subList(from, lines.size()));
    }

    /** Iterative Fibonacci (O(n), avoids stack overflow). */
    private static long fibonacci(int n) {
        if (n <= 1) return n;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long tmp = a + b;
            a = b;
            b = tmp;
        }
        return b;
    }
}
