package loadtest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class LoadTestClient {
    private LoadTestClient() {
    }

    public static void main(String[] args) throws Exception {
        CliOptions options = CliOptions.parse(args);
        String host = options.get("host", BenchmarkDefaults.HOST);
        int port = options.getInt("port", BenchmarkDefaults.BASE_PORT);
        int threads = options.getInt("threads", BenchmarkDefaults.CLIENT_THREADS);
        int warmupRequests = options.getInt("warmup-requests", BenchmarkDefaults.WARMUP_REQUESTS);
        int requests = options.getInt("requests", BenchmarkDefaults.REQUESTS);
        BenchmarkEndpoint endpoint = BenchmarkEndpoint.fromValue(options.get("endpoint", BenchmarkEndpoint.REQUEST_1.displayName()));

        if (warmupRequests > 0) {
            run(host, port, endpoint, warmupRequests, threads);
        }

        ClientRunResult result = run(host, port, endpoint, requests, threads);
        RunMetric metric = RunMetric.from(BenchmarkVariant.VIRTUAL_OWN, endpoint, 1, result);
        System.out.println("endpoint=" + endpoint.displayName()
                + ", requests=" + requests
                + ", threads=" + threads
                + ", avg_ms=" + BenchmarkResultsWriter.format(metric.avgMillis())
                + ", p95_ms=" + BenchmarkResultsWriter.format(metric.p95Millis())
                + ", throughput_rps=" + BenchmarkResultsWriter.format(metric.throughputRps())
                + ", errors=" + metric.errors());
    }

    static ClientRunResult run(String host, int port, BenchmarkEndpoint endpoint, int requestCount, int threadCount)
            throws InterruptedException {
        URI uri = URI.create("http://" + host + ":" + port + endpoint.path());
        long[] latencies = new long[requestCount];
        AtomicInteger nextRequest = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();
        AtomicReference<String> firstError = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        HttpClient client = newClient();
        long started = System.nanoTime();

        for (int worker = 0; worker < threadCount; worker++) {
            executor.submit(() -> {
                int index;
                while ((index = nextRequest.getAndIncrement()) < requestCount) {
                    long requestStarted = System.nanoTime();
                    try {
                        send(client, uri, JsonPayloads.createRequestBody(index));
                        latencies[index] = System.nanoTime() - requestStarted;
                    } catch (Exception exception) {
                        latencies[index] = System.nanoTime() - requestStarted;
                        errors.incrementAndGet();
                        firstError.compareAndSet(null, exception.getClass().getSimpleName() + ": " + exception.getMessage());
                    }
                }
            });
        }

        executor.shutdown();
        if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
            executor.shutdownNow();
            throw new IllegalStateException("Load test client did not finish in time");
        }
        long elapsed = System.nanoTime() - started;
        return new ClientRunResult(requestCount, errors.get(), elapsed, latencies, firstError.get());
    }

    private static HttpClient newClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    private static void send(HttpClient client, URI uri, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", BenchmarkDefaults.CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 || !response.body().trim().startsWith("{")) {
            throw new IOException("Unexpected response: status=" + response.statusCode() + ", body=" + response.body());
        }
    }

    record ClientRunResult(int requests, int errors, long totalNanos, long[] latenciesNanos, String firstError) {
        ClientRunResult {
            latenciesNanos = Arrays.copyOf(latenciesNanos, latenciesNanos.length);
        }
    }
}
