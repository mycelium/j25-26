package loadtest;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;
import java.util.stream.Collectors;

/**
 * Sends concurrent HTTP requests against a running LoadTestServer,
 * measures per-request latency, and produces a Markdown report.
 */
public class LoadTestRunner {

    private static final Logger logger = Logger.getLogger(LoadTestRunner.class.getName());

    // ── Static payloads built once ────────────────────────────────────────────
    private static final String STORE_PAYLOAD_TEMPLATE =
        "{\"id\":%d,\"value\":\"load-test-entry-%d-padding-xxxxxxxxxxxxxxxxxxxxxxxxxx\"}";

    private static final String COMPUTE_PAYLOAD_TEMPLATE =
        "{\"a\":%d,\"b\":%d,\"label\":\"bench-%d\"}";

    // ─────────────────────────────────────────────────────────────────────────

    private final LoadTestConfig cfg;

    public LoadTestRunner(LoadTestConfig cfg) {
        this.cfg = cfg;
    }

    // -------------------------------------------------------------------------
    // Run both request types and return their results
    // -------------------------------------------------------------------------

    public List<LoadTestResult> run() throws InterruptedException {
        logger.info("Starting scenario: " + cfg);

        LoadTestResult r1 = benchmark("Request-1", "/store",  "POST",
                                      i -> buildStorePayload(i));
        LoadTestResult r2 = benchmark("Request-2", "/compute", "POST",
                                      i -> buildComputePayload(i));

        return List.of(r1, r2);
    }

    // -------------------------------------------------------------------------
    // Core benchmark loop
    // -------------------------------------------------------------------------



private LoadTestResult benchmark(String label, String path, String method,
                                 java.util.function.IntFunction<String> bodyFn) throws InterruptedException {
    return benchmarkWithBody(label, path, method, bodyFn);
}

    private LoadTestResult benchmarkWithBody(String label, String path, String method,
                                             java.util.function.IntFunction<String> bodyFn)
            throws InterruptedException {

        long[] latencies = new long[cfg.totalRequests];
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger errors  = new AtomicInteger(0);

        // Create thread pool for the client side (always classic threads for the client)
        ExecutorService clientPool = Executors.newFixedThreadPool(cfg.threadCount);
        CountDownLatch latch = new CountDownLatch(cfg.totalRequests);

        long wallStart = System.currentTimeMillis();

        for (int i = 0; i < cfg.totalRequests; i++) {
            final int idx = i;
            clientPool.submit(() -> {
                try {
                    String body = bodyFn.apply(idx);
                    long t0 = System.currentTimeMillis();
                    boolean ok = sendRequest(path, method, body);
                    latencies[idx] = System.currentTimeMillis() - t0;
                    if (ok) success.incrementAndGet();
                    else    errors.incrementAndGet();
                } catch (Exception e) {
                    latencies[idx] = -1;
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.MINUTES);
        long wallEnd = System.currentTimeMillis();

        clientPool.shutdown();
        clientPool.awaitTermination(10, TimeUnit.SECONDS);

        // ── Compute statistics ─────────────────────────────────────────────
        long[] valid = Arrays.stream(latencies)
                             .filter(l -> l >= 0)
                             .sorted()
                             .toArray();

        double avg = valid.length == 0 ? 0 :
                     Arrays.stream(valid).average().orElse(0);

        double p95 = valid.length == 0 ? 0 :
                     valid[(int) Math.min(valid.length - 1, valid.length * 0.95)];

        long totalMs = wallEnd - wallStart;

        logger.info(String.format("[%s] %s done in %d ms — avg=%.1f ms, p95=%.1f ms, ok=%d, err=%d",
            cfg.scenarioName, label, totalMs, avg, p95, success.get(), errors.get()));

        return new LoadTestResult(cfg.scenarioName, label, cfg.totalRequests,
                                  success.get(), errors.get(), totalMs, avg, p95);
    }

    // -------------------------------------------------------------------------
    // HTTP client (raw, no external libs)
    // -------------------------------------------------------------------------

    private boolean sendRequest(String path, String method, String body) throws IOException {
        URL url = new URL("http://" + cfg.targetHost + ":" + cfg.targetPort + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(5_000);
        conn.setReadTimeout(10_000);

        if (body != null && !body.isEmpty()) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }
        }

        int code = conn.getResponseCode();
        conn.disconnect();
        return code >= 200 && code < 300;
    }

    // -------------------------------------------------------------------------
    // Payload builders
    // -------------------------------------------------------------------------

    private static String buildStorePayload(int i) {
        return String.format(STORE_PAYLOAD_TEMPLATE, i, i);
    }

    private static String buildComputePayload(int i) {
        return String.format(COMPUTE_PAYLOAD_TEMPLATE, (i % 29) + 1, (long) i * 7, i);
    }

    // =========================================================================
    // Main — runs all 4 scenario combinations and writes the report
    // =========================================================================

    public static void main(String[] args) throws Exception {
        configureLogging();

        int port          = 8765;
        int threadCount   = Integer.parseInt(System.getProperty("threads", "20"));
        int totalRequests = Integer.parseInt(System.getProperty("requests", "1000"));
        String host       = System.getProperty("host", "127.0.0.1");

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              Lab 3 — HTTP + JSON Load Test                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.printf("  host=%s  threads=%d  requests=%d%n%n", host, threadCount, totalRequests);

        // Hardware description
        printHardwareInfo();

        // ── 4 scenarios ──────────────────────────────────────────────────────
        record Scenario(String name, boolean vt, JsonAdapter parser) {}

        List<Scenario> scenarios = List.of(
            new Scenario("Virtual + OwnParser",  true,  JsonAdapter.OWN),
            new Scenario("Virtual + Gson",        true,  JsonAdapter.GSON),
            new Scenario("Classic + OwnParser",   false, JsonAdapter.OWN),
            new Scenario("Classic + Gson",        false, JsonAdapter.GSON)
        );

        // results[scenarioIdx] = [result_req1, result_req2]
        List<List<LoadTestResult>> allResults = new ArrayList<>();

        for (Scenario sc : scenarios) {
            System.out.println("\n▶ Running: " + sc.name());

            // Start a fresh server for each scenario
            LoadTestServer srv = new LoadTestServer(host, port, threadCount,
                                                    sc.vt(), sc.parser());
            Thread.sleep(300); // let server settle

            LoadTestConfig cfg = LoadTestConfig.builder()
                .scenarioName(sc.name())
                .virtualThreads(sc.vt())
                .jsonAdapter(sc.parser())
                .threadCount(threadCount)
                .totalRequests(totalRequests)
                .targetHost(host)
                .targetPort(port)
                .build();

            LoadTestRunner runner = new LoadTestRunner(cfg);
            List<LoadTestResult> results = runner.run();

            for (LoadTestResult r : results) {
                System.out.println("  " + r.summary());
            }

            allResults.add(results);
            srv.stop();
            Thread.sleep(500); // cool-down between scenarios
        }

        // ── Generate report ───────────────────────────────────────────────────
        String report = buildReport(scenarios.stream()
                                             .map(Scenario::name)
                                             .collect(Collectors.toList()),
                                    allResults, threadCount, totalRequests);

        Path reportPath = Paths.get("results", "report.md");
        Files.writeString(reportPath, report);
        System.out.println("\n✅  Report written to: " + reportPath.toAbsolutePath());
        System.out.println("\n" + report);
    }

    // -------------------------------------------------------------------------
    // Report generation
    // -------------------------------------------------------------------------

    private static String buildReport(List<String> names,
                                      List<List<LoadTestResult>> allResults,
                                      int threadCount, int totalRequests) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Lab 3 — Load Testing Report\n\n");

        // Hardware
        sb.append("## Hardware Description\n\n");
        sb.append("| Property | Value |\n|---|---|\n");
        sb.append("| OS | ").append(System.getProperty("os.name")).append(" ")
          .append(System.getProperty("os.version")).append(" |\n");
        sb.append("| JVM | ").append(System.getProperty("java.version")).append(" |\n");
        sb.append("| CPU cores | ").append(Runtime.getRuntime().availableProcessors()).append(" |\n");
        sb.append("| Max heap | ")
          .append(Runtime.getRuntime().maxMemory() / 1024 / 1024).append(" MB |\n\n");

        // Experiment parameters
        sb.append("## Experiment Parameters\n\n");
        sb.append("| Parameter | Value |\n|---|---|\n");
        sb.append("| Client threads | ").append(threadCount).append(" |\n");
        sb.append("| Server threads | ").append(threadCount).append(" |\n");
        sb.append("| Total requests per endpoint | ").append(totalRequests).append(" |\n");
        sb.append("| Request-1 endpoint | POST /store (JSON parse + file write/read) |\n");
        sb.append("| Request-2 endpoint | POST /compute (JSON parse + in-memory fibonacci) |\n");
        sb.append("| Payload size | ~80 bytes |\n\n");

        // Experiment description
        sb.append("## Experiment Description\n\n");
        sb.append("Each scenario runs two endpoints sequentially:\n\n");
        sb.append("- **Request-1** (`POST /store`): the server parses the JSON body, ");
        sb.append("appends a record to `data/store.txt`, reads back the last 5 lines, ");
        sb.append("and returns them as JSON. Measures I/O-bound performance.\n");
        sb.append("- **Request-2** (`POST /compute`): the server parses the JSON body, ");
        sb.append("computes `fibonacci(a % 30) + b` in memory, and returns the result as JSON. ");
        sb.append("Measures CPU-bound + JSON throughput.\n\n");
        sb.append("Four combinations are tested:\n\n");
        sb.append("| Combination | Thread type | JSON parser |\n|---|---|---|\n");
        sb.append("| Virtual + OwnParser | Virtual (JDK 21) | Lab-1 custom parser |\n");
        sb.append("| Virtual + Gson      | Virtual (JDK 21) | Gson 2.x |\n");
        sb.append("| Classic + OwnParser | Fixed thread pool | Lab-1 custom parser |\n");
        sb.append("| Classic + Gson      | Fixed thread pool | Gson 2.x |\n\n");

        // How to configure and launch
        sb.append("## How to Configure and Launch\n\n");
        sb.append("```bash\n");
        sb.append("# 1. Compile everything\n");
        sb.append("./compile.sh\n\n");
        sb.append("# 2. Run with defaults (20 threads, 1000 requests)\n");
        sb.append("./run.sh\n\n");
        sb.append("# 3. Custom parameters\n");
        sb.append("java -cp out:lib/gson.jar \\\n");
        sb.append("     -Dthreads=50 -Drequests=5000 -Dhost=192.168.1.10 \\\n");
        sb.append("     loadtest.LoadTestRunner\n");
        sb.append("```\n\n");
        sb.append("> **Gson**: download `gson-2.x.jar` and place it in `lib/`.\n");
        sb.append("> Run without Gson to test only `OwnParser` combinations.\n\n");

        // Results table
        sb.append("## Results\n\n");
        sb.append("| Request | Virtual + OwnParser | Virtual + Gson | Classic + OwnParser | Classic + Gson |\n");
        sb.append("|---|---|---|---|---|\n");

        for (int reqIdx = 0; reqIdx < 2; reqIdx++) {
            String reqLabel = "Request-" + (reqIdx + 1);
            sb.append("| ").append(reqLabel).append(" |");
            for (int scIdx = 0; scIdx < names.size(); scIdx++) {
                LoadTestResult r = allResults.get(scIdx).get(reqIdx);
                sb.append(String.format(" %.1f ms avg (p95: %.1f ms) |", r.avgMs, r.p95Ms));
            }
            sb.append("\n");
        }

        sb.append("\n### Throughput (req/s)\n\n");
        sb.append("| Request | Virtual + OwnParser | Virtual + Gson | Classic + OwnParser | Classic + Gson |\n");
        sb.append("|---|---|---|---|---|\n");

        for (int reqIdx = 0; reqIdx < 2; reqIdx++) {
            String reqLabel = "Request-" + (reqIdx + 1);
            sb.append("| ").append(reqLabel).append(" |");
            for (int scIdx = 0; scIdx < names.size(); scIdx++) {
                LoadTestResult r = allResults.get(scIdx).get(reqIdx);
                sb.append(String.format(" %.1f req/s |", r.throughput));
            }
            sb.append("\n");
        }

        sb.append("\n### Error rates\n\n");
        sb.append("| Request | Scenario | Total | OK | Errors |\n|---|---|---|---|---|\n");
        for (int scIdx = 0; scIdx < names.size(); scIdx++) {
            for (LoadTestResult r : allResults.get(scIdx)) {
                sb.append(String.format("| %s | %s | %d | %d | %d |\n",
                    r.requestLabel, names.get(scIdx),
                    r.totalRequests, r.successCount, r.errorCount));
            }
        }

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private static void printHardwareInfo() {
        System.out.println("── Hardware ─────────────────────────────────────────");
        System.out.println("  OS    : " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("  JVM   : " + System.getProperty("java.version"));
        System.out.println("  Cores : " + Runtime.getRuntime().availableProcessors());
        System.out.println("  Heap  : " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB");
        System.out.println("─────────────────────────────────────────────────────\n");
    }

    private static void configureLogging() {
        Logger root = Logger.getLogger("");
        root.setLevel(Level.INFO);
        for (var h : root.getHandlers()) {
            h.setFormatter(new SimpleFormatter() {
                @Override public String format(LogRecord r) {
                    return String.format("[%s] %s%n",
                        r.getLevel().getLocalizedName(), r.getMessage());
                }
            });
        }
    }
// Add to LoadTestRunner class
public void warmUp(String endpoint) {
    // Simple warm-up: send 10 requests
    for (int i = 0; i < 10; i++) {
        try {
            if (endpoint.contains("request-1")) {
                sendRequest("/store", "POST", buildStorePayload(i));
            } else {
                sendRequest("/compute", "POST", buildComputePayload(i));
            }
        } catch (IOException e) {
            // ignore warm-up errors
        }
    }
}

public LoadTestResult run(String endpoint, String label) {
    try {
        if (endpoint.contains("request-1")) {
            return benchmarkWithBody(label, "/store", "POST",
                i -> buildStorePayload(i));
        } else {
            return benchmarkWithBody(label, "/compute", "POST",
                i -> buildComputePayload(i));
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return new LoadTestResult(label, endpoint, 0, 0, 0, 0, 0, 0);
    }
}
}
