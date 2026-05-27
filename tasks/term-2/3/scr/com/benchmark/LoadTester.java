package com.benchmark;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTester {

    private static final int WARMUP_ITERATIONS = 100;  // уменьшил прогревочные
    private static final int MAX_RETRIES = 3;

    public static record Result(int totalRequests, int errorCount,
                                double avgResponseTimeMs, double throughputRps) {
    }

    public static Result executeTest(String endpointUrl, String requestBody,
                                     int totalRequests, int concurrencyLevel) throws Exception {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest baseRequest = HttpRequest.newBuilder(URI.create(endpointUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Warmup с повторными попытками
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            for (int retry = 0; retry < MAX_RETRIES; retry++) {
                try {
                    httpClient.send(baseRequest, HttpResponse.BodyHandlers.discarding());
                    break;
                } catch (Exception e) {
                    if (retry == MAX_RETRIES - 1) {
                        System.err.println("Warmup request " + i + " failed after " + MAX_RETRIES + " retries");
                    }
                    Thread.sleep(100);
                }
            }
        }

        AtomicLong totalTimeNanos = new AtomicLong();
        AtomicInteger failureCounter = new AtomicInteger();

        long startTime = System.nanoTime();

        ExecutorService threadPool = Executors.newFixedThreadPool(concurrencyLevel);
        for (int i = 0; i < totalRequests; i++) {
            final int requestId = i;
            threadPool.submit(() -> {
                long requestStart = System.nanoTime();
                boolean success = false;

                for (int retry = 0; retry < MAX_RETRIES && !success; retry++) {
                    try {
                        HttpResponse<Void> response = httpClient.send(baseRequest,
                                HttpResponse.BodyHandlers.discarding());
                        if (response.statusCode() >= 400) {
                            failureCounter.incrementAndGet();
                        }
                        success = true;
                    } catch (Exception e) {
                        if (retry == MAX_RETRIES - 1) {
                            failureCounter.incrementAndGet();
                        } else {
                            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                        }
                    }
                }

                totalTimeNanos.addAndGet(System.nanoTime() - requestStart);
            });
        }

        threadPool.shutdown();
        boolean terminated = threadPool.awaitTermination(2, TimeUnit.MINUTES);

        if (!terminated) {
            System.err.println("Warning: Test did not complete within timeout");
            threadPool.shutdownNow();
        }

        double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
        double avgMs = (totalTimeNanos.get() / (double) totalRequests) / 1_000_000.0;
        double throughput = totalRequests / elapsedSeconds;

        return new Result(totalRequests, failureCounter.get(), avgMs, throughput);
    }
}