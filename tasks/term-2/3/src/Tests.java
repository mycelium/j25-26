import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Tests {

    private static final String URL = "http://localhost:8083";
    private static final int REQ_Count = 300;
    private static final int THREADS = 60;

    public static void main(String[] args) throws Exception {
        System.out.println(REQ_Count);
        System.out.println(THREADS);
        System.out.println();

        runTest("/request1", "{\"data\":\"tetsss\"}", "Request1");
        runTest("/request2", "{\"numbers\":[1,2,4,6,8]}", "Request2");
    }

    private static void runTest(String endpoint, String body, String label) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch latch = new CountDownLatch(REQ_Count);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicLong totalTimeMs = new AtomicLong(0);

        long testStart = System.nanoTime();

        for (int i = 0; i < REQ_Count; i++) {
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
                    long timeMs = (System.nanoTime() - start) / 1_000_000;

                    if (response.statusCode() == 200) {
                        success.incrementAndGet();
                        totalTimeMs.addAndGet(timeMs);
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

        double testDurationSec = (System.nanoTime() - testStart) / 1_000_000_000.0;
        double avgMs = success.get() > 0 ? (double) totalTimeMs.get() / success.get() : 0;

        System.out.printf("%-18s | Success: %5d | Failed: %5d | time: %8.2f ms | Duration: %.2f s\n",
                label, success.get(), fail.get(), avgMs, testDurationSec);
    }
}