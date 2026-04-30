package com.loadtest;

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

public class RunTests {
    private static final int    PORT      = 8081;
    private static final String BASE_URL  = "http://localhost:" + PORT;
    private static final String FILE_BODY = "{\"data\":\"hello world from the load testing client\"}";
    private static final String COMPUTE_BODY = buildNumbers(50);

    public static void main(String[] args) throws Exception {
        int requests    = args.length >= 1 ? Integer.parseInt(args[0]) : 2000;
        int concurrency = args.length >= 2 ? Integer.parseInt(args[1]) : 50;

        System.out.printf("Running with requests=%d, concurrency=%d%n%n",
                requests, concurrency);

        String[][] configs = {
                {"own",  "virtual"},
                {"gson", "virtual"},
                {"own",  "classic"},
                {"gson", "classic"}
        };

        Map<String, Double> results = new LinkedHashMap<>();

        for (String[] cfg : configs) {
            String parser  = cfg[0];
            String threads = cfg[1];
            String label   = threads + "+" + parser;
            System.out.println(">>> " + label);

            deleteRecursive(Path.of("storage"));

            Path logFile = Path.of("server-" + label + ".log");
            Process server = startServer(parser, threads, logFile);
            try {
                if (!waitForHealth(server, 20_000)) {
                    System.err.println();
                    if (!server.isAlive()) {
                        System.err.println("ERROR: Server '" + label
                                + "' exited with code " + server.exitValue());
                    } else {
                        System.err.println("ERROR: Server '" + label
                                + "' did not respond on /health within 20s.");
                    }
                    dumpLog(logFile);
                    throw new RuntimeException("Server " + label + " did not start");
                }

                LoadTest.Result r1 = LoadTest.runTest(
                        BASE_URL + "/file", FILE_BODY, requests, concurrency);
                results.put("file:" + label, r1.avgMs);
                System.out.printf("    /file     avg=%.3f ms  (%.0f req/s, errors=%d)%n",
                        r1.avgMs, r1.throughput, r1.errors);

                LoadTest.Result r2 = LoadTest.runTest(
                        BASE_URL + "/compute", COMPUTE_BODY, requests, concurrency);
                results.put("compute:" + label, r2.avgMs);
                System.out.printf("    /compute  avg=%.3f ms  (%.0f req/s, errors=%d)%n",
                        r2.avgMs, r2.throughput, r2.errors);
            } finally {
                stopServer(server);
            }

            Thread.sleep(1000);
        }

        printTable(results);
    }

    private static Process startServer(String parser, String threads, Path logFile) throws Exception {
        String javaBin = System.getProperty("java.home") + File.separator
                + "bin" + File.separator + "java";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            javaBin += ".exe";
        }
        String classpath = System.getProperty("java.class.path");

        ProcessBuilder pb = new ProcessBuilder(
                javaBin, "-cp", classpath,
                "com.loadtest.Server", parser, threads);
        pb.redirectErrorStream(true);
        pb.redirectOutput(logFile.toFile());
        return pb.start();
    }

    private static void stopServer(Process server) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2)).build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + "/shutdown"))
                    .timeout(Duration.ofSeconds(2))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            client.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
            // сервер походу того самого
        }
        try {
            if (!server.waitFor(5, TimeUnit.SECONDS)) {
                server.destroyForcibly();
                server.waitFor(2, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean waitForHealth(Process server, long timeoutMs) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(500)).build();
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL + "/health"))
                .timeout(Duration.ofSeconds(1))
                .GET().build();
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (!server.isAlive()) {
                return false;
            }
            try {
                HttpResponse<Void> r = client.send(req,
                        HttpResponse.BodyHandlers.discarding());
                if (r.statusCode() == 200) return true;
            } catch (Exception ignored) {
                // сервер еще не поднялся
            }
            Thread.sleep(200);
        }
        return false;
    }


    private static void deleteRecursive(Path p) throws Exception {
        if (!Files.exists(p)) return;
        try (var s = Files.walk(p)) {
            s.sorted(Comparator.reverseOrder()).forEach(f -> {
                try { Files.deleteIfExists(f); } catch (Exception ignored) {}
            });
        }
    }

    private static void dumpLog(Path logFile) {
        if (!Files.exists(logFile)) {
            System.err.println("(log file " + logFile + " was not created)");
            return;
        }
        System.err.println("--- " + logFile.toAbsolutePath() + " ---");
        try {
            Files.readAllLines(logFile).forEach(System.err::println);
        } catch (Exception e) {
            System.err.println("(could not read log: " + e.getMessage() + ")");
        }
        System.err.println("---");
    }

    private static String buildNumbers(int n) {
        StringBuilder sb = new StringBuilder("{\"numbers\":[");
        for (int i = 1; i <= n; i++) {
            if (i > 1) sb.append(',');
            sb.append(i);
        }
        sb.append("]}");
        return sb.toString();
    }

    private static void printTable(Map<String, Double> r) {
        System.out.println();
        System.out.println("==================== RESULTS (avg ms per request) ====================");
        System.out.println();
        System.out.println("| req       | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |");
        System.out.println("|-----------|----------------------|----------------|----------------------|----------------|");
        System.out.printf("| Request-1 | %-20s | %-14s | %-20s | %-14s |%n",
                fmt(r.get("file:virtual+own")),  fmt(r.get("file:virtual+gson")),
                fmt(r.get("file:classic+own")),  fmt(r.get("file:classic+gson")));
        System.out.printf("| Request-2 | %-20s | %-14s | %-20s | %-14s |%n",
                fmt(r.get("compute:virtual+own")),  fmt(r.get("compute:virtual+gson")),
                fmt(r.get("compute:classic+own")),  fmt(r.get("compute:classic+gson")));
        System.out.println();
    }

    private static String fmt(Double v) {
        return v == null ? "—" : String.format("%.3f", v);
    }
}