package perf;

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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class BenchmarkRunner {

    private static final int SERVER_PORT = 8081;
    private static final String ROOT_URL = "http://localhost:" + SERVER_PORT;

    private static final String FILE_PAYLOAD = makeFilePayload(4096);
    private static final String COMPUTE_PAYLOAD = makeNumbersPayload(1000);

    public static void main(String[] args) throws Exception {
        int requests = (args.length >= 1) ? Integer.parseInt(args[0]) : 2000;
        int concurrency = (args.length >= 2) ? Integer.parseInt(args[1]) : 50;

        System.out.printf("Running with requests=%d, concurrency=%d%n%n",
                requests, concurrency);

        String[][] matrix = {
                {"own",  "virtual"},
                {"gson", "virtual"},
                {"own",  "classic"},
                {"gson", "classic"}
        };

        Map<String, Double> table = new LinkedHashMap<>();

        for (String[] entry : matrix) {
            String parserKind = entry[0];
            String threadKind = entry[1];
            String tag = threadKind + "+" + parserKind;
            System.out.println(">>> " + tag);

            wipeDirectory(Path.of("storage"));

            Path log = Path.of("server-" + tag + ".log");
            Process proc = spawnServer(parserKind, threadKind, log);
            try {
                if (!awaitHealth(proc, 20_000)) {
                    System.err.println();
                    if (!proc.isAlive()) {
                        System.err.println("ERROR: Server '" + tag
                                + "' exited with code " + proc.exitValue());
                    } else {
                        System.err.println("ERROR: Server '" + tag
                                + "' did not respond on /health within 20s.");
                    }
                    showLog(log);
                    throw new RuntimeException("Server " + tag + " did not start");
                }

                HttpLoadClient.Summary fileRes = HttpLoadClient.execute(
                        ROOT_URL + "/file", FILE_PAYLOAD, requests, concurrency);
                table.put("file:" + tag, fileRes.avgMs());
                System.out.printf(Locale.US, "    /file     avg=%.3f ms  (%.0f req/s, errors=%d)%n",
                        fileRes.avgMs(), fileRes.rps(), fileRes.failed());

                HttpLoadClient.Summary computeRes = HttpLoadClient.execute(
                        ROOT_URL + "/compute", COMPUTE_PAYLOAD, requests, concurrency);
                table.put("compute:" + tag, computeRes.avgMs());
                System.out.printf(Locale.US, "    /compute  avg=%.3f ms  (%.0f req/s, errors=%d)%n",
                        computeRes.avgMs(), computeRes.rps(), computeRes.failed());
            } finally {
                terminateServer(proc);
            }

            Thread.sleep(1000);
        }

        renderTable(table);
    }

    private static Process spawnServer(String parser, String threads, Path log) throws Exception {
        String java = System.getProperty("java.home") + File.separator
                + "bin" + File.separator + "java";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            java += ".exe";
        }
        String cp = System.getProperty("java.class.path");

        ProcessBuilder pb = new ProcessBuilder(
                java, "-cp", cp,
                "perf.BenchServer", parser, threads);
        pb.redirectErrorStream(true);
        pb.redirectOutput(log.toFile());
        return pb.start();
    }

    private static void terminateServer(Process proc) {
        try {
            HttpClient cli = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2)).build();
            HttpRequest stopReq = HttpRequest.newBuilder(URI.create(ROOT_URL + "/shutdown"))
                    .timeout(Duration.ofSeconds(2))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            cli.send(stopReq, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
        }
        try {
            if (!proc.waitFor(5, TimeUnit.SECONDS)) {
                proc.destroyForcibly();
                proc.waitFor(2, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean awaitHealth(Process proc, long maxMillis) throws Exception {
        HttpClient cli = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(500)).build();
        HttpRequest probe = HttpRequest.newBuilder(URI.create(ROOT_URL + "/health"))
                .timeout(Duration.ofSeconds(1))
                .GET().build();
        long stopAt = System.currentTimeMillis() + maxMillis;
        while (System.currentTimeMillis() < stopAt) {
            if (!proc.isAlive()) {
                return false;
            }
            try {
                HttpResponse<Void> r = cli.send(probe,
                        HttpResponse.BodyHandlers.discarding());
                if (r.statusCode() == 200) {
                    return true;
                }
            } catch (Exception ignored) {
            }
            Thread.sleep(200);
        }
        return false;
    }

    private static void wipeDirectory(Path dir) throws Exception {
        if (!Files.exists(dir)) {
            return;
        }
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (Exception ignored) {
                }
            });
        }
    }

    private static void showLog(Path log) {
        if (!Files.exists(log)) {
            System.err.println("(log file " + log + " was not created)");
            return;
        }
        System.err.println("--- " + log.toAbsolutePath() + " ---");
        try {
            Files.readAllLines(log).forEach(System.err::println);
        } catch (Exception e) {
            System.err.println("(could not read log: " + e.getMessage() + ")");
        }
        System.err.println("---");
    }

    private static String makeNumbersPayload(int n) {
        StringBuilder b = new StringBuilder("{\"numbers\":[");
        for (int i = 1; i <= n; i++) {
            if (i > 1) {
                b.append(',');
            }
            b.append(i);
        }
        b.append("]}");
        return b.toString();
    }

    private static String makeFilePayload(int size) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder data = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            data.append(alphabet.charAt(i % alphabet.length()));
        }
        return "{\"data\":\"" + data + "\"}";
    }

    private static void renderTable(Map<String, Double> r) {
        System.out.println();
        System.out.println("==================== RESULTS (avg ms per request) ====================");
        System.out.println();
        System.out.println("| req       | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |");
        System.out.println("|-----------|----------------------|----------------|----------------------|----------------|");
        System.out.printf("| Request-1 | %-20s | %-14s | %-20s | %-14s |%n",
                cell(r.get("file:virtual+own")),  cell(r.get("file:virtual+gson")),
                cell(r.get("file:classic+own")),  cell(r.get("file:classic+gson")));
        System.out.printf("| Request-2 | %-20s | %-14s | %-20s | %-14s |%n",
                cell(r.get("compute:virtual+own")),  cell(r.get("compute:virtual+gson")),
                cell(r.get("compute:classic+own")),  cell(r.get("compute:classic+gson")));
        System.out.println();
    }

    private static String cell(Double v) {
        return (v == null) ? "-" : String.format(Locale.US, "%.3f", v);
    }
}
