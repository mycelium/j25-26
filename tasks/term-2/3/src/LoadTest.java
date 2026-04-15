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

public class LoadTest {
    private static final String BASE_URL = "http://localhost:55555";
    private static final int REQUESTS = 100;
    private static final int CONCURRENCY = 50;

    public static void main(String[] args) throws Exception {
        System.out.println("Load test started");
        System.out.println("Total attempts per endpoint: " + REQUESTS);
        System.out.println("Concurrency: " + CONCURRENCY);

        runTest("/req1", "{\"data\":\"test data\"}");
        runTest("/req2", "{\"a\":123,\"b\":456}");
    }

    private static void runTest(String endpoint, String jsonBody) throws InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch latch = new CountDownLatch(REQUESTS);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalMillis = new AtomicLong(0);

        for (int i = 0; i < REQUESTS; i++) {
            executor.submit(() -> {
                long start = System.nanoTime();
                try {
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + endpoint))
                            .timeout(Duration.ofSeconds(5))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();
                    HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                    long durationNanos = System.nanoTime() - start;
                    if (resp.statusCode() == 200) {
                        successCount.incrementAndGet();
                        totalMillis.addAndGet(durationNanos / 1000000);
                    } else {
                        System.err.println("Bad status: " + resp.statusCode() + " for " + endpoint);
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Request failed: " + e.getMessage());
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();

        double avgMillis = successCount.get() > 0 ? totalMillis.get() / (double) successCount.get() : 0.0;

        System.out.println("\n=== " + endpoint + " ===");
        System.out.println("Successful: " + successCount.get());
        System.out.println("Failed: " + failureCount.get());
        System.out.printf("Average time (successful only): %.2f ms\n", avgMillis);
    }
}