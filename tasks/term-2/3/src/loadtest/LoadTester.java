package loadtest;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadTester {
    
    private static Stats statsReq1 = new Stats();
    private static Stats statsReq2 = new Stats();
    
    public static void main(String[] args) throws Exception {
        
        if (args.length >= 2) {
            Config.USE_VIRTUAL_THREADS = Boolean.parseBoolean(args[0]);
            Config.USE_GSON = Boolean.parseBoolean(args[1]);
        }
        
        System.out.println("========================================");
        System.out.println("LOAD TESTING");
        System.out.println("========================================");
        System.out.println("Virtual threads: " + Config.USE_VIRTUAL_THREADS);
        System.out.println("JSON parser: " + (Config.USE_GSON ? "Gson" : "Own"));
        System.out.println("Port: " + Config.PORT);
        System.out.println("Concurrent clients: " + Config.CONCURRENT_REQUESTS);
        System.out.println("Warmup requests: " + Config.WARMUP_REQUESTS);
        System.out.println("========================================\n");
        
        if (!checkServer()) {
            System.err.println("ERROR: Server not responding on port " + Config.PORT);
            System.err.println("Start TestServer with: java TestServer <useVirtual> <useGson>");
            return;
        }
        
        System.out.println("Warmup phase...");
        warmup();
        System.out.println("Warmup complete.\n");
        
        statsReq1 = new Stats();
        statsReq2 = new Stats();
        
        System.out.println("Starting main test...\n");
        
        ExecutorService executor = Executors.newFixedThreadPool(Config.CONCURRENT_REQUESTS);
        CountDownLatch latch = new CountDownLatch(Config.CONCURRENT_REQUESTS);
        
        long globalStart = System.currentTimeMillis();
        
        for (int i = 0; i < Config.CONCURRENT_REQUESTS; i++) {
            final int clientId = i;
            executor.submit(() -> {
                for (int j = 0; j < Config.REQUESTS_PER_CLIENT; j++) {
                    testRequest1(clientId, j);
                    testRequest2(clientId, j);
                }
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        long globalEnd = System.currentTimeMillis();
        
        System.out.println("\n========================================");
        System.out.println("TEST RESULTS");
        System.out.println("========================================");
        statsReq1.print("REQUEST 1 (I/O)");
        statsReq2.print("REQUEST 2 (CPU)");
        System.out.println("\nTotal test time: " + (globalEnd - globalStart) + " ms");
    }
    
    private static void warmup() {
        for (int i = 0; i < Config.WARMUP_REQUESTS; i++) {
            try {
                sendPost("/store", "{\"id\":\"warmup\",\"data\":\"warmup\"}");
                sendPost("/calculate", "{\"n\":10}");
            } catch (Exception e) {}
        }
    }
    
    private static boolean checkServer() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(Config.HOST, Config.PORT), 3000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    private static void testRequest1(int clientId, int reqNum) {
        long start = System.nanoTime();
        boolean ok = false;
        try {
            String json = "{\"id\":\"client_" + clientId + "\",\"data\":\"data_" + reqNum + "\"}";
            String resp = sendPost("/store", json);
            if (resp != null && resp.contains("ok")) ok = true;
        } catch (Exception e) {}
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        statsReq1.add(elapsed, ok);
    }
    
    private static void testRequest2(int clientId, int reqNum) {
        long start = System.nanoTime();
        boolean ok = false;
        try {
            int n = 20 + (clientId * reqNum) % 10;
            String json = "{\"n\":" + n + "}";
            String resp = sendPost("/calculate", json);
            if (resp != null && resp.contains("result")) ok = true;
        } catch (Exception e) {}
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        statsReq2.add(elapsed, ok);
    }
    
    private static String sendPost(String path, String body) throws IOException {
        URL url = new URL("http://" + Config.HOST + ":" + Config.PORT + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
        
        int code = conn.getResponseCode();
        StringBuilder resp = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) resp.append(line);
        }
        conn.disconnect();
        return code >= 200 && code < 300 ? resp.toString() : null;
    }
}
