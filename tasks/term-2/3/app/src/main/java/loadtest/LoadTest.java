package loadtest;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTest {
    // Настройки теста (можно вынести в параметры)
    private static final int WARMUP_REQUESTS = 1000;
    private static final int TOTAL_REQUESTS = 5000;
    private static final int CONCURRENCY = 50;
    private static final int PAYLOAD_SIZE = 512;       // байт для поля data (для Request-1)
    private static final long COMPUTE_A = 10000L;      // аргумент для Request-2 (сумма квадратов)

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: LoadTest <serverUrl> <port>");
            System.err.println("Example: LoadTest http://localhost 8081");
            System.exit(1);
        }
        String baseUrl = args[0] + ":" + args[1];

        System.out.println("=== Load Test ===");
        System.out.println("Server: " + baseUrl);
        System.out.println("Warmup: " + WARMUP_REQUESTS + ", Measured: " + TOTAL_REQUESTS);
        System.out.println("Concurrency: " + CONCURRENCY);
        System.out.println("Payload size (Request-1): " + PAYLOAD_SIZE + " bytes");
        System.out.println("Compute argument (Request-2): " + COMPUTE_A);
        System.out.println();

        // Прогрев
        System.out.println("Warming up...");
        runRequests(baseUrl, "/req1", true);
        runRequests(baseUrl, "/req2", true);

        // Основной тест
        System.out.println("\n=== Running measured tests ===");
        runRequests(baseUrl, "/req1", false);
        runRequests(baseUrl, "/req2", false);
    }

    private static void runRequests(String baseUrl, String endpoint, boolean isWarmup) throws InterruptedException {
        int count = isWarmup ? WARMUP_REQUESTS : TOTAL_REQUESTS;
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch latch = new CountDownLatch(count);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);
        AtomicLong totalMillis = new AtomicLong(0);
        HttpClient client = HttpClient.newHttpClient();

        for (int i = 0; i < count; i++) {
            final String uniqueData = generateUniquePayload(PAYLOAD_SIZE);
            executor.submit(() -> {
                long start = System.nanoTime();
                try {
                    String jsonBody;
                    Gson gson = new Gson();
                    if (endpoint.equals("/req1")) {
                        // Для /req1 передаём {"data": "уникальная_строка_размера PAYLOAD_SIZE"}
                        jsonBody = gson.toJson(Map.of("data", uniqueData));
                    } else {
                        // Для /req2 передаём {"a": значение}
                        jsonBody = gson.toJson(Map.of("a", COMPUTE_A));
                    }
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(baseUrl + endpoint))
                            .timeout(Duration.ofSeconds(5))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    long duration = System.nanoTime() - start;
                    if (response.statusCode() == 200) {
                        success.incrementAndGet();
                        totalMillis.addAndGet(duration / 1_000_000);
                    } else {
                        failure.incrementAndGet();
                    }
                } catch (Exception e) {
                    failure.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        if (!isWarmup) {
            double avgMs = success.get() > 0 ? totalMillis.get() / (double) success.get() : 0.0;
            System.out.printf("Endpoint %s: success=%d, failed=%d, avg_time=%.2f ms%n",
                    endpoint, success.get(), failure.get(), avgMs);
        }
    }

    // Генерация уникальной строки заданной длины (например, символ 'X' повторяется)
    private static String generateUniquePayload(int size) {
        // Добавляем UUID в начало, чтобы гарантировать уникальность, затем добиваем до размера
        String uuid = UUID.randomUUID().toString();
        if (size <= uuid.length()) return uuid.substring(0, size);
        return uuid + "x".repeat(size - uuid.length());
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}