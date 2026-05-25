import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class LoadTest {

    // ── Настройки ─────────────────────────────────────────────────────────────
    static final int    REQUESTS    = 10_000; // количество запросов
    static final int    CONCURRENCY = 50;     // количество параллельных потоков

    static final String BASE_URL    = "http://localhost:8080";

    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(55));
        System.out.println("  Load Test");
        System.out.printf("  Requests: %d   Concurrency: %d%n", REQUESTS, CONCURRENCY);
        System.out.println("=".repeat(55));

        // Прогрев
        System.out.println("Warming up...");
        runTest("POST", BASE_URL + "/users", 100, 10, true);
        Thread.sleep(300);

        // Request-1: POST /users (file I/O)
        System.out.println("\n--- Request-1: POST /users (parse JSON → save to file) ---");
        Result r1 = runTest("POST", BASE_URL + "/users", REQUESTS, CONCURRENCY, false);
        r1.print();

        // Request-2: GET /stats (in-memory computation)
        System.out.println("\n--- Request-2: GET /stats (in-memory sum/avg) ---");
        Result r2 = runTest("GET", BASE_URL + "/stats", REQUESTS, CONCURRENCY, false);
        r2.print();

        // Итоговая таблица
        System.out.println("\n" + "=".repeat(55));
        System.out.println("SUMMARY (avg ms per request):");
        System.out.printf("  Request-1 (POST /users): %.2f ms%n", r1.avgMs);
        System.out.printf("  Request-2 (GET /stats):  %.2f ms%n", r2.avgMs);
        System.out.println("=".repeat(55));
    }

    static Result runTest(String method, String url,
                          int total, int concurrency,
                          boolean silent) throws Exception {

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .executor(Executors.newFixedThreadPool(concurrency))
                .build();

        AtomicInteger success  = new AtomicInteger();
        AtomicInteger errors   = new AtomicInteger();
        AtomicLong    totalMs  = new AtomicLong();
        AtomicLong    minMs    = new AtomicLong(Long.MAX_VALUE);
        AtomicLong    maxMs    = new AtomicLong();
        List<Long>    latencies = Collections.synchronizedList(new ArrayList<>(total));

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        CountDownLatch latch = new CountDownLatch(total);

        long wallStart = System.currentTimeMillis();

        for (int i = 0; i < total; i++) {
            final int n = i;
            pool.submit(() -> {
                try {
                    HttpRequest.Builder b = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(30));

                    if ("POST".equals(method)) {
                        String body = "{\"name\":\"User" + n + "\",\"age\":" + (20 + n % 60) + "}";
                        b.POST(HttpRequest.BodyPublishers.ofString(body))
                         .header("Content-Type", "application/json");
                    } else {
                        b.GET();
                    }

                    long t0 = System.currentTimeMillis();
                    HttpResponse<String> resp = client.send(b.build(),
                            HttpResponse.BodyHandlers.ofString());
                    long elapsed = System.currentTimeMillis() - t0;

                    latencies.add(elapsed);
                    totalMs.addAndGet(elapsed);
                    minMs.updateAndGet(v -> Math.min(v, elapsed));
                    maxMs.updateAndGet(v -> Math.max(v, elapsed));

                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        success.incrementAndGet();
                    } else {
                        errors.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long wallMs = System.currentTimeMillis() - wallStart;
        pool.shutdown();

        Collections.sort(latencies);
        double avg = total == 0 ? 0 : (double) totalMs.get() / total;
        double rps = wallMs == 0 ? 0 : total * 1000.0 / wallMs;

        return new Result(total, success.get(), errors.get(),
                wallMs, avg, rps,
                minMs.get(), maxMs.get(),
                percentile(latencies, 50),
                percentile(latencies, 95),
                percentile(latencies, 99),
                silent);
    }

    static long percentile(List<Long> sorted, int pct) {
        if (sorted.isEmpty()) return 0;
        int idx = (int) Math.ceil(pct / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }

    record Result(int total, int success, int errors,
                  long wallMs, double avgMs, double rps,
                  long minMs, long maxMs,
                  long p50, long p95, long p99,
                  boolean silent) {

        void print() {
            if (silent) return;
            System.out.printf("  Total:      %d (success=%d errors=%d)%n", total, success, errors);
            System.out.printf("  Wall time:  %d ms%n", wallMs);
            System.out.printf("  Throughput: %.1f req/s%n", rps);
            System.out.printf("  Avg:  %.2f ms  Min: %d ms  Max: %d ms%n", avgMs, minMs, maxMs);
            System.out.printf("  p50:  %d ms   p95: %d ms   p99: %d ms%n", p50, p95, p99);
        }
    }
}
