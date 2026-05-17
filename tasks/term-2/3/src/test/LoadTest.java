package test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class LoadTest {

    private static final int WARMUP_REQUESTS = 1000;
    private static final int MEASURED_REQUESTS = 5000;
    private static final int CONCURRENT_THREADS = 50;

    private static final int  PAYLOAD_BYTES = 512;

    private static final long COMPUTE_A = 10000L;

    private static final String BASE_PAYLOAD = "x".repeat(PAYLOAD_BYTES);

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: LoadTest <host> <port>");
            System.err.println("Example: LoadTest http://localhost 8081");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String base = host + ":" + port;

        HttpClient client = HttpClient.newBuilder().executor(Executors.newVirtualThreadPerTaskExecutor()).build();

        System.out.println("Target: " + base);
        System.out.printf("Parameters: warmup=%d  measured=%d  concurrency=%d%n%n",
                WARMUP_REQUESTS, MEASURED_REQUESTS, CONCURRENT_THREADS);

        System.out.println("=== Warmup phase (" + WARMUP_REQUESTS + " req each endpoint) ===");
        runPhase(client, base + "/io",      WARMUP_REQUESTS, LoadTest::makeIoBody);
        runPhase(client, base + "/compute", WARMUP_REQUESTS, LoadTest::makeComputeBody);
        System.out.println("Warmup complete.\n");

        System.out.println("=== Measured phase (" + MEASURED_REQUESTS + " req each endpoint) ===");
        double ioAvgMs = runPhase(client, base + "/io",      MEASURED_REQUESTS, LoadTest::makeIoBody);
        double computeAvgMs = runPhase(client, base + "/compute", MEASURED_REQUESTS, LoadTest::makeComputeBody);

        System.out.printf("%n--- Results ---%n");
        System.out.printf("Request-1 (I/O-bound):     avg %.2f ms%n", ioAvgMs);
        System.out.printf("Request-2 (compute-bound): avg %.2f ms%n", computeAvgMs);
    }

    private static double runPhase(HttpClient client,
                                   String url,
                                   int count,
                                   Supplier<String> bodySupplier) throws InterruptedException {

        ExecutorService exec  = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        AtomicLong total = new AtomicLong(0L);
        CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            String body = bodySupplier.get();
            exec.submit(() -> {
                try {
                    long t0 = System.nanoTime();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();
                    client.send(request, HttpResponse.BodyHandlers.discarding());
                    total.addAndGet(System.nanoTime() - t0);
                } catch (Exception e) {
                    System.err.println("Request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        exec.shutdown();

        double avgMs = total.get() / (double) count / 1_000_000.0;
        System.out.printf("  %-40s  avg = %.2f ms%n", url, avgMs);
        return avgMs;
    }

    private static String makeIoBody() {
        String unique = UUID.randomUUID().toString().replace("-", "");
        String data   = (BASE_PAYLOAD + unique).substring(0, PAYLOAD_BYTES + 32);
        return "{\"data\":\"" + data + "\"}";
    }

    private static String makeComputeBody() {
        return "{\"a\":" + COMPUTE_A + "}";
    }
}
