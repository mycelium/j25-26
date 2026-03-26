import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoadTest {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final int THREADS = 50;
    private static final int REQUESTS = 1000;
    private static final int WARMUP = 100;   // количество запросов, не учитываемых в статистике
    private static final String ENDPOINT = "/store";   // или "/compute"
    private static final String JSON_BODY = "{\"id\":123,\"data\":\"some data for load testing\"}";
    // для /compute: "{\"value\":1000}"

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<Long> times = Collections.synchronizedList(new ArrayList<>());

        // Прогрев (warm-up)
        System.out.println("Warm-up...");
        for (int i = 0; i < WARMUP; i++) {
            executor.submit(() -> sendRequest(ENDPOINT, JSON_BODY));
        }
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // Основной тест
        executor = Executors.newFixedThreadPool(THREADS);
        System.out.println("Starting main test...");
        long startOverall = System.nanoTime();

        for (int i = 0; i < REQUESTS; i++) {
            executor.submit(() -> {
                long start = System.nanoTime();
                sendRequest(ENDPOINT, JSON_BODY);
                long end = System.nanoTime();
                times.add(TimeUnit.NANOSECONDS.toMillis(end - start));
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        long endOverall = System.nanoTime();

        // Статистика
        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long min = times.stream().mapToLong(Long::longValue).min().orElse(0);
        long max = times.stream().mapToLong(Long::longValue).max().orElse(0);
        long totalTime = TimeUnit.NANOSECONDS.toMillis(endOverall - startOverall);

        System.out.println("\n=== Results ===");
        System.out.println("Total requests: " + REQUESTS);
        System.out.println("Total time (ms): " + totalTime);
        System.out.println("Average time per request (ms): " + avg);
        System.out.println("Min time (ms): " + min);
        System.out.println("Max time (ms): " + max);
        System.out.println("Throughput (req/sec): " + (REQUESTS * 1000.0 / totalTime));
    }

    private static void sendRequest(String endpoint, String jsonBody) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://" + HOST + ":" + PORT + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(endpoint.equals("/store") ? "POST" : "GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Unexpected response: " + responseCode);
            }
            // читаем ответ, чтобы освободить соединение
            conn.getInputStream().readAllBytes();
        } catch (Exception e) {
            System.err.println("Request failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}