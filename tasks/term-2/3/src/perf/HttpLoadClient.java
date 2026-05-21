package perf;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class HttpLoadClient {

    private static final int WARMUP_HITS = 200;

    public record Summary(int total,
                          int failed,
                          double avgMs,
                          double elapsedSeconds,
                          double rps) {
    }

    public static Summary execute(String endpoint,
                                  String payload,
                                  int totalRequests,
                                  int workers) throws Exception {

        URI uri = URI.create(endpoint);
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < WARMUP_HITS; i++) {
            try {
                sendPost(uri, payloadBytes);
            } catch (Exception ignored) {
            }
        }

        AtomicLong cumulativeNanos = new AtomicLong();
        AtomicInteger failedCount = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(workers);
        long startedAt = System.nanoTime();

        for (int i = 0; i < totalRequests; i++) {
            pool.submit(() -> {
                long localStart = System.nanoTime();
                try {
                    int status = sendPost(uri, payloadBytes);
                    if (status >= 400) {
                        failedCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                    failedCount.incrementAndGet();
                }
                cumulativeNanos.addAndGet(System.nanoTime() - localStart);
            });
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.MINUTES);

        long elapsedNanos = System.nanoTime() - startedAt;
        double avgMs = (cumulativeNanos.get() / (double) totalRequests) / 1_000_000.0;
        double seconds = elapsedNanos / 1e9;
        double rps = totalRequests / seconds;

        return new Summary(totalRequests, failedCount.get(), avgMs, seconds, rps);
    }

    private static int sendPost(URI uri, byte[] payload) throws Exception {
        String path = uri.getRawPath();
        if (path == null || path.isBlank()) {
            path = "/";
        }
        if (uri.getRawQuery() != null) {
            path += "?" + uri.getRawQuery();
        }

        int port = uri.getPort() == -1 ? 80 : uri.getPort();
        String hostHeader = uri.getHost() + ":" + port;
        String header = "POST " + path + " HTTP/1.1\r\n"
                + "Host: " + hostHeader + "\r\n"
                + "Content-Type: application/json\r\n"
                + "Content-Length: " + payload.length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(uri.getHost(), port), 5000);
            socket.setSoTimeout(30000);
            socket.getOutputStream().write(header.getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().write(payload);
            socket.getOutputStream().flush();

            byte[] reply = socket.getInputStream().readAllBytes();
            String firstLine = new String(reply, 0, Math.min(reply.length, 64),
                    StandardCharsets.US_ASCII);
            String[] parts = firstLine.split(" ", 3);
            if (parts.length < 2) {
                throw new IllegalStateException("Invalid HTTP response");
            }
            return Integer.parseInt(parts[1]);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: HttpLoadClient <url> <body> <total> <concurrency>");
            System.exit(1);
        }
        Summary s = execute(args[0], args[1],
                Integer.parseInt(args[2]),
                Integer.parseInt(args[3]));

        System.out.printf("requests=%d  concurrency=%s  errors=%d%n",
                s.total(), args[3], s.failed());
        System.out.printf(Locale.US, "total_time=%.2f s  throughput=%.1f req/s%n",
                s.elapsedSeconds(), s.rps());
        System.out.printf(Locale.US, "AVG_MS=%.3f%n", s.avgMs());
    }
}
