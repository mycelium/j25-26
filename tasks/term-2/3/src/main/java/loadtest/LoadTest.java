package loadtest;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTest {
    static final String[][] CONFIGS = {
            {"own", "true"},
            {"own", "false"},
            {"gson", "true"},
            {"gson", "false"}
    };

    static final String REQUEST1_URL = "http://127.0.0.1:8080/request1";
    static final String REQUEST2_URL = "http://127.0.0.1:8080/request2";
    static final String PAYLOAD = "{\"test\":123}";
    static final int TOTAL_REQUESTS = 100;
    static final int CONCURRENCY = 20;

    public static void main(String[] args) throws Exception {
        Map<String, Map<String, Double>> results = new LinkedHashMap<>();
        results.put("Request-1", new LinkedHashMap<>());
        results.put("Request-2", new LinkedHashMap<>());

        // Определяем корень проекта (term-2), где лежат settings.gradle и gradlew
        File projectRoot = new File("..").getCanonicalFile();
        System.out.println("Project root: " + projectRoot.getAbsolutePath());

        for (String[] cfg : CONFIGS) {
            String parser = cfg[0];
            String virtual = cfg[1];
            String label = (virtual.equals("true") ? "Virtual" : "Classic") + "+"
                    + (parser.equals("own") ? "own" : parser.toUpperCase());

            System.out.println("Testing: " + label);

            // Используем gradlew.bat (Windows) или gradlew (Linux/Mac) из корня проекта
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            String gradleCmd = isWindows ? "gradlew.bat" : "./gradlew";

            ProcessBuilder pb = new ProcessBuilder(
                    gradleCmd, ":httpserver:run",
                    "--args=--parser=" + parser + " --virtual=" + virtual
            );
            pb.directory(projectRoot);
            pb.redirectErrorStream(true);
            Process serverProcess = pb.start();

            // Ждём запуск сервера
            BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Server started")) break;
            }

            double avg1 = runLoadTest(REQUEST1_URL, TOTAL_REQUESTS, CONCURRENCY);
            double avg2 = runLoadTest(REQUEST2_URL, TOTAL_REQUESTS, CONCURRENCY);

            results.get("Request-1").put(label, avg1);
            results.get("Request-2").put(label, avg2);

            serverProcess.destroyForcibly();
            serverProcess.waitFor();
            Thread.sleep(1000);
        }

        // Вывод таблицы
        System.out.println("\nResults (average ms per request):");
        System.out.println("req\tVirtual+own\tVirtual+GSON\tClassic+own\tClassic+GSON");
        for (String req : new String[]{"Request-1", "Request-2"}) {
            System.out.print(req + "\t");
            System.out.printf("%.2f\t", results.get(req).get("Virtual+own"));
            System.out.printf("%.2f\t", results.get(req).get("Virtual+GSON"));
            System.out.printf("%.2f\t", results.get(req).get("Classic+own"));
            System.out.printf("%.2f\t", results.get(req).get("Classic+GSON"));
            System.out.println();
        }
    }

    static double runLoadTest(String url, int totalRequests, int concurrency) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(PAYLOAD))
                .build();

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            futures.add(pool.submit(() -> {
                long start = System.nanoTime();
                HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
                long end = System.nanoTime();
                if (resp.statusCode() != 200) {
                    throw new RuntimeException("Status: " + resp.statusCode());
                }
                return end - start;
            }));
        }

        long totalNanos = 0;
        for (Future<Long> f : futures) {
            totalNanos += f.get();
        }
        pool.shutdown();
        return (totalNanos / 1_000_000.0) / totalRequests;
    }
}