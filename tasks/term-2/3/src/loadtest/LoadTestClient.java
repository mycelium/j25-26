package loadtest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class LoadTestClient {

    private LoadTestClient() { }

    public record TestResult(double avgMs, int errors) {
        public static final TestResult EMPTY = new TestResult(Double.NaN, 0);
    }
    public static TestResult run(String url, String jsonBody, int totalRequests, int concurrency)
            throws InterruptedException {

        var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        var latch = new CountDownLatch(totalRequests);

        var totalSuccessMillis = new AtomicLong(0);
        var successCount = new AtomicInteger(0);
        var errorCount = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                long start = System.nanoTime();
                try {
                    var request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .timeout(Duration.ofSeconds(30))
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();

                    HttpResponse<String> response = client.send(request,
                            HttpResponse.BodyHandlers.ofString());

                    long durationMs = (System.nanoTime() - start) / 1_000_000;

                    if (response.statusCode() == 200) {
                        totalSuccessMillis.addAndGet(durationMs);
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        if (successCount.get() == 0) {
            return new TestResult(Double.NaN, errorCount.get());
        }
        double avg = (double) totalSuccessMillis.get() / successCount.get();
        return new TestResult(avg, errorCount.get());
    }
}