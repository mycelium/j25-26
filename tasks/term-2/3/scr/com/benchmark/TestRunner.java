package com.benchmark;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestRunner {
    private static final int SERVER_PORT = 8081;
    private static final String BASE_URL = "http://localhost:" + SERVER_PORT;

    private static final String PAYLOAD_FILE = "{\"data\":\"load test message\"}";
    private static final String PAYLOAD_COMPUTE = buildNumberArray(50);

    public static void main(String[] args) throws Exception {
        int totalRequests = args.length >= 1 ? Integer.parseInt(args[0]) : 1000;  // уменьшил с 2000 до 1000
        int concurrencyLevel = args.length >= 2 ? Integer.parseInt(args[1]) : 20;  // уменьшил с 50 до 20

        System.out.printf("=== Load Test ===%nRequests: %d, Concurrency: %d%n%n",
                totalRequests, concurrencyLevel);

        String[][] configs = {
                {"custom", "virtual"},
                {"gson",   "virtual"},
                {"custom", "classic"},
                {"gson",   "classic"}
        };

        Map<String, Double> results = new LinkedHashMap<>();

        for (String[] cfg : configs) {
            String parser = cfg[0];
            String threads = cfg[1];
            String label = threads + "_" + parser;

            System.out.println(">>> " + label);

            cleanStorage();

            Path logFile = Path.of("server_" + label + ".log");
            Process server = startServer(parser, threads, logFile);

            try {
                if (!waitForServer(server, 30000)) {  // увеличил таймаут до 30 сек
                    dumpLog(logFile);
                    throw new RuntimeException("Server " + label + " failed to start");
                }

                // Даём серверу время полностью инициализироваться
                Thread.sleep(1000);

                LoadTester.Result r1 = LoadTester.executeTest(
                        BASE_URL + "/file", PAYLOAD_FILE, totalRequests, concurrencyLevel);
                results.put("file_" + label, r1.avgResponseTimeMs());
                System.out.printf("    /file    avg=%.3f ms (%.0f req/s, errors=%d)%n",
                        r1.avgResponseTimeMs(), r1.throughputRps(), r1.errorCount());

                // Небольшая пауза между тестами
                Thread.sleep(500);

                LoadTester.Result r2 = LoadTester.executeTest(
                        BASE_URL + "/compute", PAYLOAD_COMPUTE, totalRequests, concurrencyLevel);
                results.put("compute_" + label, r2.avgResponseTimeMs());
                System.out.printf("    /compute avg=%.3f ms (%.0f req/s, errors=%d)%n",
                        r2.avgResponseTimeMs(), r2.throughputRps(), r2.errorCount());

            } finally {
                stopServer(server);
            }
            Thread.sleep(2000);  // пауза между конфигурациями
        }

        printTable(results);
    }

    private static Process startServer(String parser, String threads, Path logFile) throws Exception {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            javaBin += ".exe";
        }
        String classpath = System.getProperty("java.class.path");

        ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", classpath,
                "com.benchmark.WebServer", parser, threads);
        pb.redirectErrorStream(true);
        pb.redirectOutput(logFile.toFile());
        return pb.start();
    }

    private static void stopServer(Process server) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + "/shutdown"))
                    .timeout(Duration.ofSeconds(2))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            client.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {}

        try {
            if (!server.waitFor(5, TimeUnit.SECONDS)) {
                server.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean waitForServer(Process server, long timeoutMs) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(500))
                .build();
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + "/health"))
                .timeout(Duration.ofSeconds(1))
                .GET()
                .build();

        long deadline = System.currentTimeMillis() + timeoutMs;
        int attempts = 0;

        while (System.currentTimeMillis() < deadline) {
            if (!server.isAlive()) {
                System.err.println("Server process died");
                return false;
            }
            try {
                Thread.sleep(500);  // ждём перед первой попыткой
                HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    System.out.println("    Server ready after " + (attempts * 500) + "ms");
                    return true;
                }
            } catch (Exception e) {
                // сервер ещё не готов
                attempts++;
            }
        }
        System.err.println("Server did not become ready within " + timeoutMs + "ms");
        return false;
    }

    private static void cleanStorage() throws Exception {
        Path storage = Path.of("storage");
        if (!Files.exists(storage)) return;
        try (var walk = Files.walk(storage)) {
            walk.sorted(Comparator.reverseOrder()).forEach(f -> {
                try { Files.deleteIfExists(f); } catch (Exception ignored) {}
            });
        }
    }

    private static void dumpLog(Path logFile) {
        if (!Files.exists(logFile)) return;
        System.err.println("--- " + logFile + " ---");
        try {
            Files.readAllLines(logFile).forEach(System.err::println);
        } catch (Exception ignored) {}
    }

    private static String buildNumberArray(int n) {
        StringBuilder sb = new StringBuilder("{\"values\":[");
        for (int i = 1; i <= n; i++) {
            if (i > 1) sb.append(',');
            sb.append(i);
        }
        sb.append("]}");
        return sb.toString();
    }

    private static void printTable(Map<String, Double> r) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println("AVERAGE RESPONSE TIME (ms)");
        System.out.println("============================================================");
        System.out.println("| req       | Virtual+custom | Virtual+GSON | Classic+custom | Classic+GSON |");
        System.out.println("|-----------|----------------|--------------|----------------|--------------|");
        System.out.printf("| Request-1 | %-14s | %-12s | %-14s | %-12s |%n",
                fmt(r.get("file_virtual_custom")), fmt(r.get("file_virtual_gson")),
                fmt(r.get("file_classic_custom")), fmt(r.get("file_classic_gson")));
        System.out.printf("| Request-2 | %-14s | %-12s | %-14s | %-12s |%n",
                fmt(r.get("compute_virtual_custom")), fmt(r.get("compute_virtual_gson")),
                fmt(r.get("compute_classic_custom")), fmt(r.get("compute_classic_gson")));
        System.out.println();
    }

    private static String fmt(Double v) {
        return v == null ? "---" : String.format("%.3f", v);
    }
}