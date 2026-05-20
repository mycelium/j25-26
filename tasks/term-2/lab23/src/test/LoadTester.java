package test;

import lab2.HttpServer;
import lab2.HttpClient;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTester {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final int TOTAL_REQUESTS = 500;
    private static final int CONCURRENCY = 20;   

    public static void main(String[] args) throws Exception {
        

        String testJson = "{\"name\":\"Aleximiy\",\"age\":20,\"city\":\"Saint-Petersburg\",\"scores\":[4,5,6]}";

        runConfiguration("Virtual + own", true, false, testJson);
        runConfiguration("Virtual + GSON",     true, true,  testJson);
        runConfiguration("Classic + own", false, false, testJson);
        runConfiguration("Classic + GSON",     false, true,  testJson);
        
    }

    private static void runConfiguration(String name, boolean isVirtual, boolean useGson, String payload) throws Exception {
        System.out.println("\n--- Running: " + name + " ---");
        
        HttpServer server = new HttpServer(HOST, PORT);
        server.setThreadPoolSize(50);
        server.setVirtualThreads(isVirtual);
        
        TestEndpoints.setupServer(server, useGson);
        
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        

        Thread.sleep(1500);

        long startTime = System.currentTimeMillis();
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong totalTime = new AtomicLong(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            executor.submit(() -> {

                HttpClient client = new HttpClient(HOST, PORT);
                try {
                    long reqStart = System.nanoTime();
                    

                    String response = client.post("/api/memory", payload);
                    
                    long reqEnd = System.nanoTime();
                    
                    if (response != null && !response.isEmpty()) {
                        successCount.incrementAndGet();
                        totalTime.addAndGet(reqEnd - reqStart);
                    }
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long endTime = System.currentTimeMillis();
        
        executor.shutdown();

        long totalDurationMs = endTime - startTime;
        double avgTimePerReqMs = (successCount.get() > 0) 
                ? (totalTime.get() / 1_000_000.0) / successCount.get() 
                : 0;
        double rps = (successCount.get() * 1000.0) / totalDurationMs;

        System.out.printf("Config: %-30s | Time: %d ms | Avg: %.2f ms | RPS: %.0f | OK: %d/%d%n",
                name, totalDurationMs, avgTimePerReqMs, rps, successCount.get(), TOTAL_REQUESTS);


        server.stop();
        Thread.sleep(500);
    }
}
