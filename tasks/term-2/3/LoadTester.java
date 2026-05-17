import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class LoadTester {

    public static void main(String[] args) throws Exception {
        int threadCount        = args.length > 0 ? Integer.parseInt(args[0]) : 50;
        int requestsPerThread  = args.length > 1 ? Integer.parseInt(args[1]) : 20;
        String targetPath      = args.length > 2 ? args[2] : "/req2";
        int targetPort         = args.length > 3 ? Integer.parseInt(args[3]) : 8082;
        int warmupRequests     = args.length > 4 ? Integer.parseInt(args[4]) : 50;

        String jsonPayload = "{\"payload\":\"test_data_string\", \"limit\": 5000}";
        String rawHttpRequest = buildRequest(targetPath, targetPort, jsonPayload);
        byte[] requestBytes = rawHttpRequest.getBytes(StandardCharsets.UTF_8);

        System.out.printf("Config: threads=%d, requests/thread=%d, path=%s, port=%d%n",
            threadCount, requestsPerThread, targetPath, targetPort);

        // --- Warmup phase ---
        System.out.printf("Warming up with %d requests...%n", warmupRequests);
        runRequests(warmupRequests, 1, requestBytes, targetPort, true);
        System.out.println("Warmup complete.");

        // --- Measured phase ---
        int totalRequests = threadCount * requestsPerThread;
        System.out.printf("Starting measured test: %d total requests...%n", totalRequests);

        long testStart = System.currentTimeMillis();
        List<Long> latencies = runRequests(totalRequests, threadCount, requestBytes, targetPort, false);
        long testEnd = System.currentTimeMillis();

        long totalMs = testEnd - testStart;
        printResults(latencies, totalMs);
    }

    private static List<Long> runRequests(int total, int threads, byte[] requestBytes,
                                          int port, boolean silent) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<Long>> tasks = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            tasks.add(() -> {
                long start = System.currentTimeMillis();
                try (Socket socket = new Socket("localhost", port);
                     OutputStream os = socket.getOutputStream();
                     InputStream is = socket.getInputStream()) {
                    os.write(requestBytes);
                    os.flush();
                    byte[] resp = is.readAllBytes();
                    String response = new String(resp, StandardCharsets.UTF_8);
                    if (!response.startsWith("HTTP/1.1 200")) {
                        if (!silent) System.out.println("Non-200 response: " + response.split("\r\n")[0]);
                        throw new RuntimeException("Non-200 response");
                    }
                }
                return System.currentTimeMillis() - start;
            });
        }

        List<Future<Long>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        List<Long> latencies = new ArrayList<>();
        for (Future<Long> f : futures) {
            try { latencies.add(f.get()); } catch (Exception ignored) {}
        }
        return latencies;
    }

    private static void printResults(List<Long> latencies, long totalMs) {
        if (latencies.isEmpty()) {
            System.out.println("No successful requests.");
            return;
        }

        Collections.sort(latencies);
        long sum = latencies.stream().mapToLong(Long::longValue).sum();
        double avg = (double) sum / latencies.size();
        long p95 = latencies.get((int) Math.ceil(latencies.size() * 0.95) - 1);
        long p99 = latencies.get((int) Math.ceil(latencies.size() * 0.99) - 1);
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);
        double throughput = latencies.size() / (totalMs / 1000.0);

        System.out.println("\n=== RESULTS ===");
        System.out.printf("Successful requests : %d%n", latencies.size());
        System.out.printf("Total test time     : %d ms%n", totalMs);
        System.out.printf("Throughput          : %.2f req/s%n", throughput);
        System.out.printf("Avg latency         : %.2f ms%n", avg);
        System.out.printf("Min latency         : %d ms%n", min);
        System.out.printf("p95 latency         : %d ms%n", p95);
        System.out.printf("p99 latency         : %d ms%n", p99);
        System.out.printf("Max latency         : %d ms%n", max);
    }

    private static String buildRequest(String path, int port, String body) {
        return "POST " + path + " HTTP/1.1\r\n" +
               "Host: localhost:" + port + "\r\n" +
               "Content-Type: application/json\r\n" +
               "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
               "Connection: close\r\n\r\n" +
               body;
    }
}
