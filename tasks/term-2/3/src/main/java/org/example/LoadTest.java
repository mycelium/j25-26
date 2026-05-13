package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadTest {

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Usage: LoadTest <host> <port> <concurrency> <warmupRequests> <measuredRequests>");
            System.exit(1);
        }

        String host             = args[0];
        int    port             = Integer.parseInt(args[1]);
        int    concurrency      = Integer.parseInt(args[2]);
        int    warmupCount      = Integer.parseInt(args[3]);
        int    measuredCount    = Integer.parseInt(args[4]);

        String baseUrl = "http://" + host + ":" + port;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();

        System.out.println("=== Load Test ===");
        System.out.printf("Target: %s | Concurrency: %d | Warmup: %d | Measured: %d%n%n",
                baseUrl, concurrency, warmupCount, measuredCount);

        System.out.println("--- Warmup phase (" + warmupCount + " requests per endpoint) ---");
        runPhase(client, baseUrl, concurrency, warmupCount, false);
        System.out.println("Warmup complete.\n");

        System.out.println("--- Measured phase (" + measuredCount + " requests per endpoint) ---");

        Stats stats1 = new Stats();
        Stats stats2 = new Stats();

        runPhase(client, baseUrl, concurrency, measuredCount, true, stats1, stats2);

        System.out.println("\n=== Results ===");
        System.out.println("Request-1 (I/O):      " + stats1);
        System.out.println("Request-2 (compute):  " + stats2);
    }

    private static void runPhase(HttpClient client, String baseUrl,
                                  int concurrency, int total,
                                  boolean record) throws Exception {
        runPhase(client, baseUrl, concurrency, total, record, new Stats(), new Stats());
    }

    private static void runPhase(HttpClient client, String baseUrl,
                                  int concurrency, int total,
                                  boolean record,
                                  Stats stats1, Stats stats2) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        CountDownLatch  done = new CountDownLatch(total * 2);
        List<Exception> errors = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            final String payload1 = buildPayload1();
            final String payload2 = buildPayload2();

            pool.submit(() -> {
                try {
                    long start = System.currentTimeMillis();
                    boolean ok = sendPost(client, baseUrl + "/request1", payload1);
                    long elapsed = System.currentTimeMillis() - start;
                    if (record) stats1.record(elapsed, ok);
                } catch (Exception e) {
                    if (record) stats1.record(0, false);
                    synchronized (errors) { errors.add(e); }
                } finally {
                    done.countDown();
                }
            });

            pool.submit(() -> {
                try {
                    long start = System.currentTimeMillis();
                    boolean ok = sendPost(client, baseUrl + "/request2", payload2);
                    long elapsed = System.currentTimeMillis() - start;
                    if (record) stats2.record(elapsed, ok);
                } catch (Exception e) {
                    if (record) stats2.record(0, false);
                    synchronized (errors) { errors.add(e); }
                } finally {
                    done.countDown();
                }
            });
        }

        done.await();
        pool.shutdown();

        if (!errors.isEmpty()) {
            System.err.println("Errors during phase: " + errors.size());
        }
    }

    private static boolean sendPost(HttpClient client, String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    private static String buildPayload1() {
        String data = UUID.randomUUID().toString().repeat(10);
        return "{\"data\": \"" + data + "\", \"n\": 0}";
    }

    private static String buildPayload2() {
        int n = 20 + (int)(Math.random() * 11);
        return "{\"data\": \"\", \"n\": " + n + "}";
    }
}
