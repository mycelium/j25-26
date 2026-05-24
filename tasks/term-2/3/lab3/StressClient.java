package lab3;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StressClient {

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8083;
        String endpoint = args.length > 2 ? args[2] : "/api/v1/math-task";
        int concurrency = args.length > 3 ? Integer.parseInt(args[3]) : 50;
        int batchSize = args.length > 4 ? Integer.parseInt(args[4]) : 20;
        int warmupRuns = args.length > 5 ? Integer.parseInt(args[5]) : 50;
        String payload = "{\"identifier\":\"test_val_999\", \"cycles\": 4000}";

        System.out.println("Config: Host=" + host + ", Port=" + port + ", Endpoint=" + endpoint + 
                           ", Concurrency=" + concurrency + ", Batch=" + batchSize + ", Warmup=" + warmupRuns);

        String httpFrame = "POST " + endpoint + " HTTP/1.1\r\n" +
                "Host: " + host + ":" + port + "\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + payload.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n" +
                payload;

        byte[] rawFrame = httpFrame.getBytes(StandardCharsets.UTF_8);

        // Warmup phase
        System.out.println("Starting warmup phase...");
        for (int i = 0; i < warmupRuns; i++) {
            try (Socket conn = new Socket(host, port);
                 OutputStream out = conn.getOutputStream();
                 InputStream in = conn.getInputStream()) {
                out.write(rawFrame);
                out.flush();
                in.readAllBytes();
            } catch (Exception ignored) {}
        }
        System.out.println("Warmup finished. Starting load test...");

        ExecutorService workers = Executors.newFixedThreadPool(concurrency);
        List<Callable<Long>> jobs = new ArrayList<>();

        for (int count = 0; count < concurrency * batchSize; count++) {
            jobs.add(() -> {
                long t0 = System.currentTimeMillis();
                boolean passed = false;

                try (Socket conn = new Socket(host, port);
                     OutputStream out = conn.getOutputStream();
                     InputStream in = conn.getInputStream()) {

                    out.write(rawFrame);
                    out.flush();

                    byte[] inbound = in.readAllBytes();
                    String reply = new String(inbound, StandardCharsets.UTF_8);

                    if (reply.contains("HTTP/1.1 200")) {
                        passed = true;
                    }
                } catch (Exception ignored) {
                }

                long t1 = System.currentTimeMillis();
                if (!passed) {
                    throw new RuntimeException("Dropped");
                }
                return t1 - t0;
            });
        }

        long globalStart = System.currentTimeMillis();
        List<Future<Long>> metrics = workers.invokeAll(jobs);
        long globalEnd = System.currentTimeMillis();
        workers.shutdown();

        long aggregatedTime = 0;
        int validResponses = 0;

        for (Future<Long> metric : metrics) {
            try {
                aggregatedTime += metric.get();
                validResponses++;
            } catch (Exception ignored) {
            }
        }

        System.out.println("Success count: " + validResponses + " / " + jobs.size());
        if (validResponses > 0) {
            System.out.printf("Avg duration: %.2f ms\n", (double) aggregatedTime / validResponses);
        }
        System.out.println("Total span: " + (globalEnd - globalStart) + " ms");
    }
}