package com.labs.client;

import java.io.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import com.labs.config.TestConfig;

public class LoadTester {
    private final HttpClient httpClient;
    private final String baseUrl;

    public LoadTester(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public TestResult runTest(String endpoint, String payload, int concurrency, int requestsPerThread) {
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        List<Future<Long>> futures = new ArrayList<>();
        long totalBytes = payload.getBytes().length;

        for (int i = 0; i < concurrency; i++) {
            futures.add(executor.submit(() -> {
                long startTime = System.nanoTime();
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        var request = HttpRequest.newBuilder()
                                .uri(java.net.URI.create(baseUrl + endpoint))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(payload))
                                .build();
                        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() != 200) {
                            throw new RuntimeException("HTTP " + response.statusCode());
                        }
                    } catch (Exception e) {

                    }
                }
                return System.nanoTime() - startTime;
            }));
        }

        long totalRequests = (long) concurrency * requestsPerThread;
        long totalElapsedNs = 0;
        int successCount = 0;

        for (var f : futures) {
            try {
                totalElapsedNs += f.get();
                successCount += requestsPerThread;
            } catch (Exception e) {
            }
        }
        executor.shutdown();

        double avgTimeMs = (totalElapsedNs / 1_000_000.0) / totalRequests;
        return new TestResult(endpoint, avgTimeMs, totalRequests, totalBytes * totalRequests);
    }
}