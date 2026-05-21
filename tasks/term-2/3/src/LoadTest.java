import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class LoadTest {


    static final int    REQUESTS    = 100;          // запросов на каждый endpoint
    static final int    CONCURRENCY = 50;           // параллельных клиентов
    static final String HOST        = "localhost";
    static final int    PORT        = 8080;
    static final String LABEL       = "Classic+GSON"; // метка для CSV


    private static final String STORE_BODY = "{\"data\":\"test data\"}";
    private static final String CALC_BODY  = "{\"a\":123,\"b\":456}";
    private static final String RESULTS_CSV = "results.csv";

    private final HttpClient client;

    public LoadTest() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private long sendRequest(String path, String body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + HOST + ":" + PORT + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            long t0 = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long elapsed = System.currentTimeMillis() - t0;

            return response.statusCode() == 200 ? elapsed : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public double runTest(String path, String body) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch latch = new CountDownLatch(REQUESTS);

        AtomicLong totalMs = new AtomicLong(0);
        AtomicLong success = new AtomicLong(0);
        AtomicLong errors  = new AtomicLong(0);

        for (int i = 0; i < REQUESTS; i++) {
            pool.submit(() -> {
                try {
                    long ms = sendRequest(path, body);
                    if (ms >= 0) {
                        totalMs.addAndGet(ms);
                        success.incrementAndGet();
                    } else {
                        errors.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.MINUTES);
        pool.shutdown();

        long s = success.get();
        long e = errors.get();
        double avg = s > 0 ? (double) totalMs.get() / s : 0.0;

        System.out.printf("  %-8s success=%-5d errors=%-4d avg=%.2f ms%n", path, s, e, avg);
        return avg;
    }

    public static void main(String[] args) throws Exception {
        System.out.printf("%n=== LoadTest | label=%-25s | concurrency=%d | requests=%d ===%n",
                LABEL, CONCURRENCY, REQUESTS);

        LoadTest tester = new LoadTest();

        System.out.println("Warming up...");
        tester.runTest("/store", STORE_BODY);
        tester.runTest("/calc",  CALC_BODY);
        Thread.sleep(500);

        System.out.println("Running tests...");
        double avgStore = tester.runTest("/store", STORE_BODY);
        double avgCalc  = tester.runTest("/calc",  CALC_BODY);

        boolean fileExists = new java.io.File(RESULTS_CSV).exists();
        try (PrintWriter pw = new PrintWriter(new FileWriter(RESULTS_CSV, true))) {
            if (!fileExists) {
                pw.println("label,endpoint,requests,concurrency,avg_ms");
            }
            pw.printf("%s,/store,%d,%d,%.2f%n", LABEL, REQUESTS, CONCURRENCY, avgStore);
            pw.printf("%s,/calc,%d,%d,%.2f%n",  LABEL, REQUESTS, CONCURRENCY, avgCalc);
        }

        System.out.printf("%nResults appended to %s%n", RESULTS_CSV);
        System.out.printf("  /store avg: %.2f ms%n", avgStore);
        System.out.printf("  /calc  avg: %.2f ms%n", avgCalc);
    }
}
