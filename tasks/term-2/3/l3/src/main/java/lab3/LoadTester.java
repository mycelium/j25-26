package lab3;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Пример:
 *   ./gradlew runLoadTest --args="store 1000 50"
 *   ./gradlew runLoadTest --args="compute 1000 50 192.168.1.10 8080"
 */
public class LoadTester {

    public static void main(String[] args) throws Exception {
        String endpoint      = args.length > 0 ? args[0] : "store";
        int totalRequests    = args.length > 1 ? Integer.parseInt(args[1]) : 1000;
        int concurrency      = args.length > 2 ? Integer.parseInt(args[2]) : 50;
        String host          = args.length > 3 ? args[3] : "localhost";
        int port             = args.length > 4 ? Integer.parseInt(args[4]) : 8080;

        String url  = "http://" + host + ":" + port + "/" + endpoint;
        String body = buildBody(endpoint);

        System.out.printf("=== Load Tester ===%n");
        System.out.printf("  URL           : POST %s%n", url);
        System.out.printf("  Total requests: %d%n", totalRequests);
        System.out.printf("  Concurrency   : %d%n%n", concurrency);

        ExecutorService clientExecutor = Executors.newVirtualThreadPerTaskExecutor();

        HttpClient http = HttpClient.newBuilder()
                .executor(clientExecutor)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        List<Long> latencies = Collections.synchronizedList(new ArrayList<>(totalRequests));
        AtomicInteger errors = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(totalRequests);

        long startAll = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            clientExecutor.submit(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .timeout(Duration.ofSeconds(15))
                            .build();

                    long t0 = System.nanoTime();
                    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                    long elapsed = System.nanoTime() - t0;

                    if (response.statusCode() != 200) {
                        errors.incrementAndGet();
                        System.err.println("Non-200: " + response.statusCode() + " | " + response.body());
                    } else {
                        latencies.add(elapsed);
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                    System.err.println("Request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long wallMs = System.currentTimeMillis() - startAll;
        clientExecutor.shutdownNow();

        printStats(latencies, errors.get(), totalRequests, wallMs);
    }

    private static String buildBody(String endpoint) {
        return switch (endpoint) {
            case "store"   -> "{\"value\":\"load-test-sample-data\"}";
            case "compute" -> buildComputeBody(100);
            default        -> "{}";
        };
    }
    private static String buildComputeBody(int n) {
        StringBuilder sb = new StringBuilder("{\"numbers\":[");
        for (int i = 1; i <= n; i++) {
            sb.append(i);
            if (i < n) sb.append(',');
        }
        sb.append("]}");
        return sb.toString();
    }

    private static void printStats(List<Long> latencies, int errors, int total, long wallMs) {
        if (latencies.isEmpty()) {
            System.out.println("no success requests");
            return;
        }

        List<Long> sorted = new ArrayList<>(latencies);
        Collections.sort(sorted);

        long sum = 0;
        for (long v : sorted) sum += v;

        double avgMs = (sum / (double) sorted.size()) / 1_000_000.0;

        System.out.println("=== results ===");
        System.out.printf("  success requests : %d / %d%n", sorted.size(), total);
        System.out.printf("  errors : %d%n", errors);
        System.out.printf("  all time: %d ms%n", wallMs);
        System.out.printf("  avg : %.2f ms%n", avgMs);
    }
}
