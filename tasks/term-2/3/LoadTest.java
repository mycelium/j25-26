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

    public static void main(String[] args) throws Exception {
        int requests = 500;
        int threads = 20;
        int warmup = 50;
        String base = "http://localhost:8080";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--requests" -> requests = Integer.parseInt(args[++i]);
                case "--threads" -> threads = Integer.parseInt(args[++i]);
                case "--warmup" -> warmup = Integer.parseInt(args[++i]);
                case "--host" -> base = args[++i];
            }
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        System.out.println("Warmup (" + warmup + " requests)...");
        for (int i = 0; i < warmup; i++) {
            sendPost(client, base + "/compute", "{\"n\":1000}");
        }

        System.out.println("\n--- I/O-bound: POST /io (" + requests + " req, " + threads + " threads) ---");
        runTest(client, base + "/io", "{\"data\":\"load test payload\",\"id\":1}", requests, threads);

        System.out.println("\n--- CPU-bound: POST /compute (" + requests + " req, " + threads + " threads) ---");
        runTest(client, base + "/compute", "{\"n\":100000}", requests, threads);
    }

    static void runTest(HttpClient client, String url, String body, int total, int threads) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Callable<Long>> tasks = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            tasks.add(() -> {
                long start = System.nanoTime();
                sendPost(client, url, body);
                return (System.nanoTime() - start) / 1_000_000;
            });
        }

        long testStart = System.nanoTime();
        List<Future<Long>> results = pool.invokeAll(tasks);
        long testTime = (System.nanoTime() - testStart) / 1_000_000;
        pool.shutdown();

        long sum = 0;
        long min = Long.MAX_VALUE;
        long max = 0;
        int errors = 0;

        for (Future<Long> f : results) {
            try {
                long ms = f.get();
                sum += ms;
                if (ms < min) min = ms;
                if (ms > max) max = ms;
            } catch (Exception e) {
                errors++;
            }
        }

        int ok = total - errors;
        double avg = ok > 0 ? (double) sum / ok : 0;
        double throughput = ok > 0 ? (double) ok / testTime * 1000 : 0;
        System.out.printf("Success: %d, Errors: %d%n", ok, errors);
        System.out.printf("Avg: %.2f ms, Min: %d ms, Max: %d ms%n", avg, min, max);
        System.out.printf("Throughput: %.1f req/s%n", throughput);
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
