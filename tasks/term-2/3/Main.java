import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Main {

    static final int REQUESTS = 1000; // сколько запросов отправить на каждый маршрут
    static final int THREADS  = 10;   // размер пула для Classic-потоков


    public static void main(String[] args) throws Exception {
        System.out.println("=== Load Testing Report ===\n");

        long[] r1 = new long[4];
        long[] r2 = new long[4];

        String[]  labels   = { "Virtual + Own parser", "Virtual + Gson      ", "Classic + Own parser", "Classic + Gson      " };
        boolean[] virtuals = { true,  true,  false, false };
        boolean[] useGsons = { false, true,  false, true  };
        int[]     ports    = { 8081,  8082,  8083,  8084  };

        for (int i = 0; i < 4; i++) {
            System.out.println("Запускаем: " + labels[i]);

            startServer(ports[i], THREADS, virtuals[i], useGsons[i]);
            Thread.sleep(600);

            r1[i] = runTest(ports[i], "/request1", "{\"id\":" + i + ",\"name\":\"hello\"}");
            r2[i] = runTest(ports[i], "/request2", "{\"value\":42}");

            stopServer(ports[i]);
            Thread.sleep(300);
        }

        printTable(labels, r1, r2);
    }

    // СЕРВЕР

    // запущенные серверы, чтобы потом остановить
    static Map<Integer, SimpleHttpServer> servers = new HashMap<>();

    static void startServer(int port, int threads, boolean virtual, boolean useGson) {
        SimpleHttpServer server = new SimpleHttpServer("localhost", port, threads, virtual);

        // Request-1: принять JSON, сохранить в файл, прочитать обратно
        server.post("/request1", (req, res) -> {
            Map<String, Object> data = parse(req.getBody(), useGson);

            String id   = String.valueOf(data.get("id"));
            String name = String.valueOf(data.get("name"));

            Path file = Path.of("data_" + id + ".txt");
            Files.writeString(file, name);
            String saved = Files.readString(file);

            res.send(toJson(Map.of("status", "ok", "saved", saved), useGson));
        });

        // Request-2: принять JSON, посчитать квадрат числа, вернуть JSON
        server.post("/request2", (req, res) -> {
            Map<String, Object> data = parse(req.getBody(), useGson);
            long value  = ((Number) data.get("value")).longValue();
            long result = value * value;
            res.send(toJson(Map.of("result", result), useGson));
        });

        servers.put(port, server);

        new Thread(() -> {
            try { server.start(); } catch (Exception ignored) {}
        }).start();
    }

    static void stopServer(int port) {
        try { servers.get(port).stop(); } catch (Exception e) { e.printStackTrace(); }
    }

    static Map<String, Object> parse(String json, boolean useGson) {
        if (useGson) return GsonParser.parseToMap(json);
        return JsonParser.parseToMap(json);
    }

    static String toJson(Map<String, Object> map, boolean useGson) {
        if (useGson) return GsonParser.toJson(map);
        return JsonParser.toJson(map);
    }

    // ТЕСТ

    static long runTest(int port, String path, String body) throws Exception {
        long total = 0;
        for (int i = 0; i < REQUESTS; i++) {
            long start = System.nanoTime();
            sendPost(port, path, body);
            total += System.nanoTime() - start;
        }
        return (total / REQUESTS) / 1000;
    }

    static String sendPost(int port, String path, String body) throws Exception {
        URL url = new URI("http://localhost:" + port + path).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        conn.getOutputStream().write(data);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }


    static void printTable(String[] labels, long[] r1, long[] r2) {
        System.out.println();
        System.out.printf("%-12s | %-22s | %-22s | %-22s | %-22s%n",
            "Запрос", labels[0].trim(), labels[1].trim(), labels[2].trim(), labels[3].trim());
        System.out.println("-".repeat(103));
        System.out.printf("%-12s | %-22s | %-22s | %-22s | %-22s%n",
            "Request-1", r1[0]+" мкс", r1[1]+" мкс", r1[2]+" мкс", r1[3]+" мкс");
        System.out.printf("%-12s | %-22s | %-22s | %-22s | %-22s%n",
            "Request-2", r2[0]+" мкс", r2[1]+" мкс", r2[2]+" мкс", r2[3]+" мкс");
        System.out.println("\nВсе значения — среднее время на один запрос (мкс = микросекунды).");
    }
}
