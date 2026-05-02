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

public class BenchmarkRunner {
    private static final String URL = "http://localhost:12345";
    private static final int REQUESTS = 100;
    private static final int CONCURRENCY = 50;

    public static void main(String[] args) throws Exception {
        System.out.println("--- Starting Load Test ---");
        
        benchmark("/req1", "{\"data\":\"Performance test payload\"}");
        benchmark("/req2", "{\"a\":1024,\"b\":2048}");
    }

    private static void benchmark(String path, String body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        List<Callable<Long>> tasks = new ArrayList<>();

        for (int i = 0; i < REQUESTS; i++) {
            tasks.add(() -> {
                long start = System.nanoTime();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(URL + path))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) throw new RuntimeException("Server Error");
                
                return (System.nanoTime() - start) / 1_000_000;
            });
        }

        List<Future<Long>> futures = executor.invokeAll(tasks);
        long totalMs = 0;
        int count = 0;

        for (Future<Long> f : futures) {
            try {
                totalMs += f.get();
                count++;
            } catch (Exception e) {
                System.err.println("Request failed: " + e.getMessage());
            }
        }

        System.out.printf("Results for [%s]: Avg = %.2f ms (Completed: %d/%d)\n", 
                          path, (double)totalMs/count, count, REQUESTS);
        executor.shutdown();
    }
}