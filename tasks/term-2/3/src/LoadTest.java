import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class LoadTest {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final int THREADS = 50;          
    private static final int REQUESTS_PER_THREAD = 20; 
    private static final int WARMUP = 100;          

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java LoadTest <store|compute>");
            System.exit(1);
        }
        String endpoint = args[0];
        String jsonBody;
        if (endpoint.equals("store")) {
            jsonBody = "{\"id\":12345}";
        } else if (endpoint.equals("compute")) {
            jsonBody = "{\"n\":10}";
        } else {
            System.err.println("Unknown endpoint. Use 'store' or 'compute'");
            return;
        }

        String url = "http://" + HOST + ":" + PORT + "/" + endpoint;

        ExecutorService exec = Executors.newFixedThreadPool(THREADS);
        List<Long> times = Collections.synchronizedList(new ArrayList<>());

        // Прогрев
        System.out.println("Warming up (" + WARMUP + " requests)...");
        for (int i = 0; i < WARMUP; i++) {
            exec.submit(() -> sendRequest(url, jsonBody));
        }
        exec.shutdown();
        exec.awaitTermination(30, TimeUnit.SECONDS);

        
        exec = Executors.newFixedThreadPool(THREADS);
        System.out.println("Starting main test (" + (THREADS * REQUESTS_PER_THREAD) + " requests)...");
        long startOverall = System.nanoTime();

        for (int i = 0; i < THREADS; i++) {
            exec.submit(() -> {
                for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
                    long start = System.nanoTime();
                    boolean ok = sendRequest(url, jsonBody);
                    long end = System.nanoTime();
                    if (ok) {
                        synchronized (times) {
                            times.add(TimeUnit.NANOSECONDS.toMillis(end - start));
                        }
                    }
                }
            });
        }

        exec.shutdown();
        exec.awaitTermination(2, TimeUnit.MINUTES);
        long endOverall = System.nanoTime();

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long totalMs = TimeUnit.NANOSECONDS.toMillis(endOverall - startOverall);
        System.out.println("\nRESULTS");
        System.out.println("Endpoint: /" + endpoint);
        System.out.println("Total requests sent: " + (THREADS * REQUESTS_PER_THREAD));
        System.out.println("Successful responses: " + times.size());
        System.out.printf("Average response time: %.3f ms\n", avg);
        System.out.println("Total test duration: " + totalMs + " ms");
        System.out.printf("Throughput: %.2f req/sec\n", (times.size() * 1000.0 / totalMs));
    }

    private static boolean sendRequest(String urlStr, String jsonBody) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            conn.getInputStream().readAllBytes();
            return code == 200;
        } catch (Exception e) {
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
