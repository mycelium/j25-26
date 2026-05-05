package load_test;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTest {

    private static final int THREADS = 100;
    private static final int REQUESTS = 1000;

    public static void main(String[] args) throws Exception {

        String endpoint = args.length > 0 ? args[0] : "store";
        String url = endpoint.equals("compute")
                ? "http://localhost:8080/api/compute"
                : "http://localhost:8080/api/store";

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        List<Long> times = new CopyOnWriteArrayList<>();
        AtomicInteger errors = new AtomicInteger(0);

        long start = System.currentTimeMillis();

        for (int i = 0; i < REQUESTS; i++) {
            pool.submit(() -> {
                try {
                    long t1 = System.nanoTime();

                    sendRequest(url);

                    long t2 = System.nanoTime();
                    times.add((t2 - t1) / 1_000_000); // ms

                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.MINUTES);

        long end = System.currentTimeMillis();

        printStats(times, end - start, errors.get());
    }

    private static void sendRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String json = """
            {
              "subject": "java",
              "lab": 3,
              "tasks": {
                "task1": "complete",
                "task2": "complete",
                "task3": "in process"
              }
            }
    
        """;

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        int code = conn.getResponseCode();

        if (code != 200) {
            throw new RuntimeException("HTTP error: " + code);
        }
    }

    private static void printStats(List<Long> times, long totalTime, int errors) {
        if (times.isEmpty()) {
            System.out.println("No successful requests");
            return;
        }

        long sum = 0;

        for (long t : times) {
            sum += t;
        }

        double avg = sum / (double) times.size();

        System.out.println("Requests: " + (times.size() + errors));
        System.out.println("Avg time: " + avg + " ms");
    }
}