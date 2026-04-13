package loadtesting;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class LoadTester {

    private final int    totalRequests;
    private final int    concurrentThreads;
    private final String body;

    public LoadTester(int totalRequests, int concurrentThreads, String body) {
        this.totalRequests     = totalRequests;
        this.concurrentThreads = concurrentThreads;
        this.body              = body;
    }

    public record Result(
            long avgMs,
            long minMs,
            long maxMs,
            long totalMs,
            int  errors,
            int  total
    ) {
        @Override
        public String toString() {
            return String.format(
                    "avg=%dms  min=%dms  max=%dms  total=%dms  errors=%d/%d",
                    avgMs, minMs, maxMs, totalMs, errors, total);
        }
    }

    public Result run(String url) throws InterruptedException {
        // One client shared across all tasks — do NOT close it inside tasks
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        ExecutorService pool     = Executors.newFixedThreadPool(concurrentThreads);
        List<Long>      latencies = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger   errors   = new AtomicInteger(0);
        AtomicInteger   logged   = new AtomicInteger(0); // log first 3 errors only
        CountDownLatch  latch    = new CountDownLatch(totalRequests);

        long testStart = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            pool.submit(() -> {
                try {
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .timeout(Duration.ofSeconds(10))
                            .build();

                    long start = System.currentTimeMillis();
                    HttpResponse<String> resp =
                            client.send(req, HttpResponse.BodyHandlers.ofString());
                    long elapsed = System.currentTimeMillis() - start;

                    latencies.add(elapsed);

                    if (resp.statusCode() != 200) {
                        errors.incrementAndGet();
                        if (logged.incrementAndGet() <= 3) {
                            System.err.println("[ERROR] HTTP " + resp.statusCode()
                                    + " | body: " + resp.body());
                        }
                    }

                } catch (Exception e) {
                    errors.incrementAndGet();
                    latencies.add(0L); // count as 0ms so avg is meaningful
                    if (logged.incrementAndGet() <= 3) {
                        System.err.println("[EXCEPTION] " + e.getClass().getSimpleName()
                                + ": " + e.getMessage());
                        if (e.getCause() != null) {
                            System.err.println("  caused by: " + e.getCause());
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        long totalMs = System.currentTimeMillis() - testStart;

        List<Long> valid = latencies.stream().filter(l -> l > 0).toList();
        long avg = valid.isEmpty() ? 0
                : (long) valid.stream().mapToLong(Long::longValue).average().orElse(0);
        long min = valid.isEmpty() ? 0 : Collections.min(valid);
        long max = valid.isEmpty() ? 0 : Collections.max(valid);

        return new Result(avg, min, max, totalMs, errors.get(), totalRequests);
    }
}