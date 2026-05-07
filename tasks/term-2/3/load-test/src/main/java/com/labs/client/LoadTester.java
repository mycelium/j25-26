package com.labs.client;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTester {
    public static double run(String url, String body, int threads, int reqsPerThread) throws Exception {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        AtomicLong totalNanos = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(threads * reqsPerThread);

        for (int t = 0; t < threads; t++) {
            for (int r = 0; r < reqsPerThread; r++) {
                pool.submit(() -> {
                    try {
                        long start = System.nanoTime();
                        client.send(req, HttpResponse.BodyHandlers.discarding());
                        long end = System.nanoTime();
                        totalNanos.addAndGet(end - start);
                    } catch (Exception ignored) {} finally { latch.countDown(); }
                });
            }
        }
        latch.await(); pool.shutdown(); pool.awaitTermination(10, TimeUnit.SECONDS);

        double totalMs = totalNanos.get() / 1_000_000.0;
        return totalMs / (threads * reqsPerThread);
    }
}