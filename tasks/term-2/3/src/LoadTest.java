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
    
    // Параметры нагрузки
    private static final int THREADS = 50;          // Количество одновременных потоков
    private static final int REQUESTS_PER_THREAD = 20; // Запросов от каждого потока
    private static final int WARMUP_REQUESTS = 100;    // Прогревочные запросы

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java LoadTest <store|compute>");
            System.exit(1);
        }

        String endpoint = args[0];
        String jsonBody;
        
        // Формируем тело запроса в зависимости от эндпоинта
        if ("store".equals(endpoint)) {
            jsonBody = "{\"id\":12345}";
        } else if ("compute".equals(endpoint)) {
            jsonBody = "{\"n\":10}";
        } else {
            System.err.println("Unknown endpoint. Use 'store' or 'compute'");
            return;
        }

        String urlStr = "http://" + HOST + ":" + PORT + "/" + endpoint;

        System.out.println("Starting Load Test for: /" + endpoint);
        System.out.println("Configuration: " + THREADS + " threads, " + REQUESTS_PER_THREAD + " reqs/thread");

        ExecutorService exec = Executors.newFixedThreadPool(THREADS);
        List<Long> times = Collections.synchronizedList(new ArrayList<>());

        // 1. Прогрев (Warmup)
        System.out.println("Warming up (" + WARMUP_REQUESTS + " requests)...");
        for (int i = 0; i < WARMUP_REQUESTS; i++) {
            exec.submit(() -> sendRequest(urlStr, jsonBody));
        }
        exec.shutdown();
        exec.awaitTermination(30, TimeUnit.SECONDS);

        // 2. Основной тест
        exec = Executors.newFixedThreadPool(THREADS);
        System.out.println("Running main test...");
        long startOverall = System.nanoTime();

        for (int i = 0; i < THREADS; i++) {
            exec.submit(() -> {
                for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
                    long start = System.nanoTime();
                    boolean success = sendRequest(urlStr, jsonBody);
                    long end = System.nanoTime();
                    
                    if (success) {
                        times.add(TimeUnit.NANOSECONDS.toMillis(end - start));
                    }
                }
            });
        }

        exec.shutdown();
        exec.awaitTermination(2, TimeUnit.MINUTES);
        long endOverall = System.nanoTime();

        // 3. Вывод результатов
        double avgTime = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long totalDurationMs = TimeUnit.NANOSECONDS.toMillis(endOverall - startOverall);
        int successfulRequests = times.size();
        double throughput = successfulRequests * 1000.0 / totalDurationMs;

        System.out.println("\n--- RESULTS ---");
        System.out.println("Endpoint:       /" + endpoint);
        System.out.println("Total Requests: " + (THREADS * REQUESTS_PER_THREAD));
        System.out.println("Successful:     " + successfulRequests);
        System.out.printf("Avg Time:       %.3f ms\n", avgTime);
        System.out.println("Duration:       " + totalDurationMs + " ms");
        System.out.printf("Throughput:     %.2f req/sec\n", throughput);
        System.out.println("---------------\n");
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
            // Обязательно читаем ответ, чтобы соединение закрылось корректно
            conn.getInputStream().readAllBytes(); 
            
            return code == 200;
        } catch (Exception e) {
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}