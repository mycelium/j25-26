import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    private static final String HOST = "http://localhost:8082";

    private static final int THREADS = 20;
    private static final int REQUESTS = 100;

    public static void main(String[] args) throws Exception {
        runTests();
    }

    public static void runTests() throws Exception {
        //прогрев
        testRoute("/request1");
        testRoute("/request2");
        double r1 = testRoute("/request1");
        double r2 = testRoute("/request2");

        System.out.printf("Request-1 avg: %.4f ms%n", r1);
        System.out.printf("Request-2 avg: %.4f ms%n", r2);
        System.out.println();
    }


private static double testRoute(String route) throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    List<Future<Long>> futures = new ArrayList<>();
    for (int t = 0; t < THREADS; t++) {
        for (int i = 0; i < REQUESTS; i++) {
            int id = i;
            futures.add(executor.submit(() -> {
                long start = System.nanoTime();
                if (route.equals("/request1")) {
                    sendRequest(route, "{\"id\":" + id + ",\"value\":\"test\"}");
                } else {
                    sendRequest(route, "{\"number\":" + id + "}");
                }

                long end = System.nanoTime();
                return end - start;
            }));
        }
    }
    executor.shutdown();
    executor.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES);
    long total = 0;
    for (Future<Long> f : futures) {
        total += f.get();
    }
    return (total / 1_000_000.0) / (THREADS * REQUESTS);
}

    private static void sendRequest(String route, String json) throws Exception {
        URL url = new URL(HOST + route);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        try (var is = conn.getInputStream()) { 
            while (is.read() != -1) {} 
        }
        conn.disconnect();
    }
}