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
    private static final String URL = "http://localhost:9876";
    private static final int REQUESTS = 100;
    private static final int CONCURRENCY = 50;

    public static void main(String[] args) throws Exception {
        System.out.println("Requests per endpoint: " + REQUESTS);
        System.out.println("Concurrent threads: " + CONCURRENCY + "\n");

        test("/req1", "{\"data\":\"test IO\"}");
        test("/req2", "{\"a\":1000,\"b\":2000}");
    }

    private static void test(String endpoint, String body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch latch = new CountDownLatch(REQUESTS);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicLong totalTime = new AtomicLong(0);

        for (int i = 0; i < REQUESTS; i++) {
            executor.submit(() -> {
                long start = System.nanoTime();
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(URL + endpoint))
                            .timeout(Duration.ofSeconds(5))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    long time = (System.nanoTime() - start) / 1_000_000;

                    if (response.statusCode() == 200) {
                        success.incrementAndGet();
                        totalTime.addAndGet(time);
                    } else {
                        fail.incrementAndGet();
                    }
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        double avg = success.get() > 0 ? (double) totalTime.get() / success.get() : 0;
        System.out.printf("Endpoint: %s | Success: %d | Failed: %d | Avg: %.2f ms\n",
                endpoint, success.get(), fail.get(), avg);
    }
}