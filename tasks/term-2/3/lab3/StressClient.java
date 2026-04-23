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

    private static final int CONCURRENCY_LEVEL = 50;
    private static final int BATCH_SIZE = 20;
    private static final String ENDPOINT = "/api/v1/math-task";
    private static final int NODE_PORT = 8083;
    private static final String PAYLOAD = "{\"identifier\":\"test_val_999\", \"cycles\": 4000}";

    public static void main(String[] args) throws Exception {
        ExecutorService workers = Executors.newFixedThreadPool(CONCURRENCY_LEVEL);
        List<Callable<Long>> jobs = new ArrayList<>();

        String httpFrame = "POST " + ENDPOINT + " HTTP/1.1\r\n" +
                "Host: 127.0.0.1:" + NODE_PORT + "\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + PAYLOAD.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n" +
                PAYLOAD;

        byte[] rawFrame = httpFrame.getBytes(StandardCharsets.UTF_8);

        for (int count = 0; count < CONCURRENCY_LEVEL * BATCH_SIZE; count++) {
            jobs.add(() -> {
                long t0 = System.currentTimeMillis();
                boolean passed = false;

                try (Socket conn = new Socket("127.0.0.1", NODE_PORT);
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