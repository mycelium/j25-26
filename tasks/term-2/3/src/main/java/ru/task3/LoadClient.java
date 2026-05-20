package ru.task3;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

public class LoadClient {

    public static long benchmark(
            String endpoint,
            int requests,
            int threads
    ) throws Exception {

        CountDownLatch latch =
                new CountDownLatch(requests);

        long start = System.currentTimeMillis();

        for (int i = 0; i < requests; i++) {

            new Thread(() -> {

                try {

                    URL url =
                            new URL(
                                    "http://localhost:8080"
                                            + endpoint
                            );

                    HttpURLConnection conn =
                            (HttpURLConnection)
                                    url.openConnection();

                    conn.setRequestMethod("POST");

                    conn.setDoOutput(true);

                    String body =
                            "{\"name\":\"Anna\",\"value\":10}";

                    OutputStream os =
                            conn.getOutputStream();

                    os.write(body.getBytes());

                    conn.getInputStream().readAllBytes();

                    conn.disconnect();

                } catch (Exception ignored) {
                }

                latch.countDown();

            }).start();
        }

        latch.await();

        return System.currentTimeMillis() - start;
    }
}
