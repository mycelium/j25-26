package com.loadtest;

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

public class LoadTest {

    private static final int WARMUP = 200;

    public static final class Result {
        public final int total;
        public final int errors;
        public final double avgMs;
        public final double totalSeconds;
        public final double throughput;

        public Result(int total, int errors, double avgMs,
                      double totalSeconds, double throughput) {
            this.total = total;
            this.errors = errors;
            this.avgMs = avgMs;
            this.totalSeconds = totalSeconds;
            this.throughput = throughput;
        }
    }

    public static Result runTest(String url, String body, int total, int concurrency)
            throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        HttpRequest tpl = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        for (int i = 0; i < WARMUP; i++) {
            client.send(tpl, HttpResponse.BodyHandlers.discarding());
        }

        AtomicLong totalNanos = new AtomicLong();
        AtomicInteger errors  = new AtomicInteger();

        long t0 = System.nanoTime();
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        for (int i = 0; i < total; i++) {
            pool.submit(() -> {
                long s = System.nanoTime();
                try {
                    HttpResponse<Void> r = client.send(tpl,
                            HttpResponse.BodyHandlers.discarding());
                    if (r.statusCode() >= 400) errors.incrementAndGet();
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
                totalNanos.addAndGet(System.nanoTime() - s);
            });
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.MINUTES);
        long elapsed = System.nanoTime() - t0;

        double avgMs = (totalNanos.get() / (double) total) / 1_000_000.0;
        double rps   = total / (elapsed / 1e9);
        return new Result(total, errors.get(), avgMs, elapsed / 1e9, rps);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: LoadTest <url> <body> <total_requests> <concurrency>");
            System.exit(1);
        }
        Result r = runTest(args[0], args[1],
                Integer.parseInt(args[2]),
                Integer.parseInt(args[3]));

        System.out.printf("requests=%d  concurrency=%s  errors=%d%n",
                r.total, args[3], r.errors);
        System.out.printf("total_time=%.2f s  throughput=%.1f req/s%n",
                r.totalSeconds, r.throughput);
        System.out.printf("AVG_MS=%.3f%n", r.avgMs);
    }
}