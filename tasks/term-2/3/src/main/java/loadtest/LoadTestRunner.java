package loadtest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTestRunner {

    public static void main(String[] args) throws Exception {
        Map<String, String> params = parseArgs(args);
        String host      = params.getOrDefault("host", "localhost");
        int port         = Integer.parseInt(params.getOrDefault("port", "8080"));
        int numThreads   = Integer.parseInt(params.getOrDefault("threads", "50"));
        int numRequests  = Integer.parseInt(params.getOrDefault("requests", "1000"));

        System.out.printf("Load test → http://%s:%d  threads=%d  total-requests=%d%n",
                host, port, numThreads, numRequests);
        System.out.println("=".repeat(65));

        TestResult r1 = runTest("Request-1",
                String.format("http://%s:%d/api/request1", host, port),
                "{\"name\":\"benchmark\",\"value\":\"test_data\"}",
                numThreads, numRequests);

        TestResult r2 = runTest("Request-2",
                String.format("http://%s:%d/api/request2", host, port),
                "{\"n\":30}",
                numThreads, numRequests);

        printResults(r1, r2);
    }

    private static TestResult runTest(String name, String url, String body,
                                      int numThreads, int numRequests) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        List<Long> latencies = Collections.synchronizedList(new ArrayList<>(numRequests));
        AtomicLong errors    = new AtomicLong();
        CountDownLatch latch  = new CountDownLatch(numRequests);
        ExecutorService pool  = Executors.newFixedThreadPool(numThreads);

        int perThread  = numRequests / numThreads;
        int remainder  = numRequests % numThreads;
        long startTime = System.currentTimeMillis();

        for (int t = 0; t < numThreads; t++) {
            int count = perThread + (t < remainder ? 1 : 0);
            pool.submit(() -> {
                for (int i = 0; i < count; i++) {
                    long reqStart = System.nanoTime();
                    try {
                        HttpRequest req = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(body))
                                .timeout(Duration.ofSeconds(30))
                                .build();
                        HttpResponse<Void> resp = client.send(req,
                                HttpResponse.BodyHandlers.discarding());
                        if (resp.statusCode() != 200) errors.incrementAndGet();
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    } finally {
                        latencies.add((System.nanoTime() - reqStart) / 1_000_000L);
                        latch.countDown();
                    }
                }
            });
        }

        latch.await(10, TimeUnit.MINUTES);
        pool.shutdown();
        long totalMs = System.currentTimeMillis() - startTime;

        return new TestResult(name, new ArrayList<>(latencies), errors.get(), totalMs);
    }

    record TestResult(String name, List<Long> latencies, long errors, long totalMs) {
        double avgMs() {
            return latencies.stream().mapToLong(l -> l).average().orElse(0.0);
        }

        long p95Ms() {
            if (latencies.isEmpty()) return 0;
            List<Long> sorted = new ArrayList<>(latencies);
            Collections.sort(sorted);
            return sorted.get((int) (sorted.size() * 0.95));
        }

        double throughput() {
            return totalMs > 0 ? latencies.size() * 1000.0 / totalMs : 0;
        }
    }

    private static void printResults(TestResult... results) {
        System.out.println("\n=== RESULTS ===");
        System.out.printf("%-12s %10s %10s %10s %12s%n",
                "Test", "Avg(ms)", "P95(ms)", "Errors", "Req/s");
        System.out.println("-".repeat(58));
        for (TestResult r : results) {
            System.out.printf("%-12s %10.1f %10d %10d %12.1f%n",
                    r.name(), r.avgMs(), r.p95Ms(), r.errors(), r.throughput());
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] kv = arg.substring(2).split("=", 2);
                if (kv.length == 2) map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
