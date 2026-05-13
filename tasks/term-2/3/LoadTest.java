import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoadTest {

    static final int WARMUP = 50;
    static final int REQUESTS = 500;
    static final int THREADS = 20;

    public static void main(String[] args) throws Exception {
        String base = "http://localhost:8080";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        System.out.println("Warmup (" + WARMUP + " requests)...");
        for (int i = 0; i < WARMUP; i++) {
            sendPost(client, base + "/compute", "{\"n\":1000}");
        }

        System.out.println("\n--- I/O-bound: POST /io (" + REQUESTS + " requests, " + THREADS + " threads) ---");
        runTest(client, base + "/io", "{\"data\":\"load test payload\",\"id\":1}");

        System.out.println("\n--- CPU-bound: POST /compute (" + REQUESTS + " requests, " + THREADS + " threads) ---");
        runTest(client, base + "/compute", "{\"n\":100000}");
    }

    static void runTest(HttpClient client, String url, String body) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        List<Callable<Long>> tasks = new ArrayList<>();

        for (int i = 0; i < REQUESTS; i++) {
            tasks.add(() -> {
                long start = System.nanoTime();
                sendPost(client, url, body);
                return (System.nanoTime() - start) / 1_000_000;
            });
        }

        List<Future<Long>> results = pool.invokeAll(tasks);
        pool.shutdown();

        long total = 0;
        long min = Long.MAX_VALUE;
        long max = 0;
        int errors = 0;

        for (Future<Long> f : results) {
            try {
                long ms = f.get();
                total += ms;
                if (ms < min) min = ms;
                if (ms > max) max = ms;
            } catch (Exception e) {
                errors++;
            }
        }

        int ok = REQUESTS - errors;
        double avg = ok > 0 ? (double) total / ok : 0;
        System.out.printf("Success: %d, Errors: %d%n", ok, errors);
        System.out.printf("Avg: %.2f ms, Min: %d ms, Max: %d ms%n", avg, min, max);
    }

    static void sendPost(HttpClient client, String url, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
