import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoadTester {

    private static final int THREAD_COUNT = 50;     
    private static final int REQUESTS_PER_THREAD = 20; 

    // здесь меняется  /req1 НА /req2 
    private static final String TARGET_PATH = "/req2";
    private static final int TARGET_PORT = 8082;

    private static final String JSON_PAYLOAD = "{\"payload\":\"test_data_string\", \"limit\": 5000}";

    public static void main(String[] args) throws Exception {
        System.out.println("Starting RAW SOCKET load test on port: " + TARGET_PORT + TARGET_PATH);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Callable<Long>> tasks = new ArrayList<>();

    
        String rawHttpRequest = "POST " + TARGET_PATH + " HTTP/1.1\r\n" +
                "Host: localhost:" + TARGET_PORT + "\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + JSON_PAYLOAD.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n" +
                JSON_PAYLOAD;

        byte[] requestBytes = rawHttpRequest.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < THREAD_COUNT * REQUESTS_PER_THREAD; i++) {
            tasks.add(() -> {
                long start = System.currentTimeMillis();
                boolean isSuccess = false;

                try (Socket socket = new Socket("localhost", TARGET_PORT);
                     OutputStream os = socket.getOutputStream();
                     InputStream is = socket.getInputStream()) {

              
                    os.write(requestBytes);
                    os.flush();

                
                    byte[] responseBytes = is.readAllBytes();
                    String response = new String(responseBytes, StandardCharsets.UTF_8);

                
                    if (response.startsWith("HTTP/1.1 200")) {
                        isSuccess = true;
                    } else {
                        System.out.println("Error response:\n" + response);
                    }
                } catch (Exception e) {
        
                }

                long end = System.currentTimeMillis();
                if (!isSuccess) {
                    throw new RuntimeException("Request failed"); 
                }
                return end - start;
            });
        }

        long totalTestStart = System.currentTimeMillis();
        List<Future<Long>> results = executor.invokeAll(tasks);
        long totalTestEnd = System.currentTimeMillis();
        executor.shutdown();

     
        long totalDuration = 0;
        int successfulRequests = 0;
        for (Future<Long> res : results) {
            try {
                totalDuration += res.get(); 
                successfulRequests++;
            } catch (Exception e) {
          
            }
        }

        System.out.println("\n=== REAL TEST RESULTS ===");
        System.out.println("Successful 200 OK requests: " + successfulRequests + " / " + tasks.size());
        if (successfulRequests > 0) {
            double avgTime = (double) totalDuration / successfulRequests;
            System.out.printf("Average response time: %.2f ms\n", avgTime);
        }
        System.out.println("Total test time: " + (totalTestEnd - totalTestStart) + " ms");
    }
}