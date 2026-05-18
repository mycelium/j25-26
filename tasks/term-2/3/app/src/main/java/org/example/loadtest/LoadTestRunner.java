package org.example.loadtest;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTestRunner {

    public static double run(Config cfg, String path, int numThreads, int requestsPerThread) throws Exception {
        String requestBody = buildBody(path);
        byte[] requestBytes = buildHttpRequest(cfg.serverPort, path, requestBody);

        List<Future<List<Long>>> futures = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        AtomicInteger errors = new AtomicInteger(0);

        for (int t = 0; t < numThreads; t++) {
            futures.add(pool.submit(() -> {
                List<Long> times = new ArrayList<>();
                for (int r = 0; r < requestsPerThread; r++) {
                    long start = System.nanoTime();
                    try (Socket socket = new Socket("localhost", cfg.serverPort)) {
                        socket.setSoTimeout(10000);
                        OutputStream out = socket.getOutputStream();
                        out.write(requestBytes);
                        out.flush();

                        InputStream in = socket.getInputStream();
                        byte[] buf = new byte[4096];
                        StringBuilder sb = new StringBuilder();
                        int read;
                        while ((read = in.read(buf)) != -1) {
                            sb.append(new String(buf, 0, read, StandardCharsets.UTF_8));
                            if (sb.toString().contains("\r\n\r\n")) {
                                // Check content-length to know when body is done
                                String resp = sb.toString();
                                int clIdx = resp.toLowerCase().indexOf("content-length:");
                                if (clIdx != -1) {
                                    int nlIdx = resp.indexOf("\r\n", clIdx);
                                    int cl = Integer.parseInt(resp.substring(clIdx + 15, nlIdx).trim());
                                    int bodyStart = resp.indexOf("\r\n\r\n") + 4;
                                    if (resp.length() - bodyStart >= cl) break;
                                } else {
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                    long elapsed = System.nanoTime() - start;
                    times.add(elapsed);
                }
                return times;
            }));
        }

        pool.shutdown();
        pool.awaitTermination(120, TimeUnit.SECONDS);

        List<Long> allTimes = new ArrayList<>();
        for (Future<List<Long>> f : futures) {
            allTimes.addAll(f.get());
        }

        if (!allTimes.isEmpty()) {
            double avgNs = allTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            if (errors.get() > 0) {
                System.out.printf("    Errors: %d/%d%n", errors.get(), allTimes.size() + errors.get());
            }
            return avgNs / 1_000_000.0; 
        }
        return 0;
    }

    private static String buildBody(String path) {
        if (path.equals("/request1")) {
            return "{\"id\":\"test-123\",\"data\":\"Hello load test world!\"}";
        } else {
            return "{\"values\":[1,2,3,4,5,6,7,8,9,10]}";
        }
    }

    private static byte[] buildHttpRequest(int port, String path, String body) {
        String request = "POST " + path + " HTTP/1.1\r\n"
                + "Host: localhost:" + port + "\r\n"
                + "Content-Type: application/json\r\n"
                + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n"
                + body;
        return request.getBytes(StandardCharsets.UTF_8);
    }
}
