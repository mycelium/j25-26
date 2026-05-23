package loadtest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class LoadTester {

    private static final int WARMUP = 500;
    private static final int REQUESTS = 5000;
    private static final int CONCURRENCY = 50;
    private static final int PAYLOAD_SIZE = 512;
    private static final long COMPUTE_ARG = 10_000L;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java -jar tester.jar <host> <port>");
            System.err.println("Example: java -jar tester.jar http://localhost 8080");
            System.exit(1);
        }

        String baseUrl = args[0] + ":" + args[1];

        System.out.println("=== Load Test ===");
        System.out.println("Server:      " + baseUrl);
        System.out.println("Warmup:      " + WARMUP);
        System.out.println("Requests:    " + REQUESTS);
        System.out.println("Concurrency: " + CONCURRENCY);
        System.out.println();

        System.out.println("Warming up...");
        run(baseUrl, "/req1", true);
        run(baseUrl, "/req2", true);

        System.out.println("Running tests...");
        System.out.println();
        run(baseUrl, "/req1", false);
        run(baseUrl, "/req2", false);
    }

    private static void run(String baseUrl, String endpoint, boolean warmup) throws InterruptedException {
        int count = warmup ? WARMUP : REQUESTS;
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        ExecutorService exec = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch latch = new CountDownLatch(count);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        AtomicLong totalMs = new AtomicLong();

        for (int i = 0; i < count; i++) {
            exec.submit(() -> {
                try {
                    String body = endpoint.equals("/req1")
                            ? buildReq1Body()
                            : "{\"a\":" + COMPUTE_ARG + "}";

                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(baseUrl + endpoint))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();

                    long start = System.nanoTime();
                    HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                    long ms = (System.nanoTime() - start) / 1_000_000;

                    if (resp.statusCode() == 200) {
                        success.incrementAndGet();
                        totalMs.addAndGet(ms);
                    } else {
                        failed.incrementAndGet();
                    }
                } catch (Exception e) {
                    failed.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        exec.shutdown();

        if (!warmup) {
            double avg = success.get() > 0 ? totalMs.get() / (double) success.get() : 0;
            System.out.printf("%-8s success=%-5d failed=%-4d avg=%.2f ms%n",
                    endpoint, success.get(), failed.get(), avg);
        }
    }

    private static String buildReq1Body() {
        String uuid = UUID.randomUUID().toString();
        String payload = uuid.length() >= PAYLOAD_SIZE
                ? uuid.substring(0, PAYLOAD_SIZE)
                : uuid + "x".repeat(PAYLOAD_SIZE - uuid.length());
        return "{\"data\":\"" + payload + "\"}";
    }
}
