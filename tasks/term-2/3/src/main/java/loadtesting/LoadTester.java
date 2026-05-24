package loadtesting;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sends HTTP POST requests concurrently and returns latency statistics.
 * One shared HttpClient is used for all requests (efficient connection reuse).
 */
public class LoadTester {

    private static final Duration CONNECT_TIMEOUT  = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT  = Duration.ofSeconds(10);
    private static final int      MAX_ERROR_LOGS   = 3;

    private final int    totalRequests;
    private final int    concurrency;
    private final String jsonBody;

    public LoadTester(int totalRequests, int concurrency, String jsonBody) {
        this.totalRequests = totalRequests;
        this.concurrency   = concurrency;
        this.jsonBody      = jsonBody;
    }

    public StatsCollector.Summary run(String url) throws InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(CONNECT_TIMEOUT)
                .build();

        ExecutorService pool    = Executors.newFixedThreadPool(concurrency);
        StatsCollector  stats   = new StatsCollector();
        AtomicInteger   errors  = new AtomicInteger(0);
        AtomicInteger   logged  = new AtomicInteger(0);
        CountDownLatch  latch   = new CountDownLatch(totalRequests);

        long wallStart = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            pool.submit(() -> {
                try {
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .timeout(REQUEST_TIMEOUT)
                            .build();

                    long t0   = System.currentTimeMillis();
                    var  resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                    stats.record(System.currentTimeMillis() - t0);

                    if (resp.statusCode() != 200) {
                        errors.incrementAndGet();
                        if (logged.incrementAndGet() <= MAX_ERROR_LOGS) {
                            System.err.printf("[ERROR] HTTP %d — %s%n",
                                    resp.statusCode(), resp.body());
                        }
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                    stats.record(0); // failed request: 0 ms, excluded from avg
                    if (logged.incrementAndGet() <= MAX_ERROR_LOGS) {
                        System.err.printf("[EXCEPTION] %s: %s%n",
                                e.getClass().getSimpleName(), e.getMessage());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        long wallMs = System.currentTimeMillis() - wallStart;
        return stats.summarize(totalRequests, errors.get(), wallMs);
    }
}