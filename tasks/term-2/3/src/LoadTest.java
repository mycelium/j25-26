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
    private static final int WARMUP = 100;

    private static String endpoint;
    private static String jsonBody;

    public static void main(String[] args) throws InterruptedException {
        // проверка аргументов
        if (args.length == 0) {
            System.err.println("Usage: java LoadTest <mode>");
            System.err.println("  mode: compute or store");
            System.exit(1);
        }

        String mode = args[0].toLowerCase();
        switch (mode) {
            case "compute":
                endpoint = "/compute";
                jsonBody = "{\"value\":1000}";
                break;
            case "store":
                endpoint = "/store";
                jsonBody = "{\"id\":123,\"data\":\"some data for load testing\"}";
                break;
            default:
                System.err.println("Unknown mode: " + args[0]);
                System.err.println("Valid modes: compute, store");
                System.exit(1);
        }

        System.out.println("Running load test for endpoint: " + endpoint);

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<Long> times = Collections.synchronizedList(new ArrayList<>());

        System.out.println("Warm-up...");
        for (int i = 0; i < WARMUP; i++) {
            executor.submit(() -> sendRequest());
        }
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        executor = Executors.newFixedThreadPool(THREADS);
        System.out.println("Starting main test...");
        long startOverall = System.nanoTime();

        for (int i = 0; i < REQUESTS; i++) {
            executor.submit(() -> {
                long start = System.nanoTime();
                sendRequest();
                long end = System.nanoTime();
                times.add(TimeUnit.NANOSECONDS.toMillis(end - start));
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        long endOverall = System.nanoTime();

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

    private static void sendRequest() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://" + HOST + ":" + PORT + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(input);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Unexpected response: " + responseCode);
            }
            conn.getInputStream().readAllBytes(); // освобождаем соединение
        } catch (Exception e) {
            System.err.println("Request failed: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
