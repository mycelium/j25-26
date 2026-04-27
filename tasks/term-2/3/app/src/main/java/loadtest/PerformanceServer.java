package loadtest;

import com.httpserverlib.HttpServer;
import com.httpserverlib.model.HttpResponse;
import ru.derikey.json.JsonMapper;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class PerformanceServer {
    private static final ConcurrentHashMap<Long, Long> cache = new ConcurrentHashMap<>();

    // Модели данных
    public static class Request1Data {
        public String data;
    }
    public static class Response1Data {
        public String stored;
        public String retrieved;
        public Response1Data(String stored, String retrieved) {
            this.stored = stored;
            this.retrieved = retrieved;
        }
        // Для Gson/JsonMapper нужен пустой конструктор
        public Response1Data() {}
    }
    public static class Request2Data {
        public long a;
    }
    public static class Response2Data {
        public long result;
        public Response2Data(long result) {
            this.result = result;
        }
        public Response2Data() {}
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: java PerformanceServer <port> <threads:virtual|classic> <parser:own|gson> <poolSize>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        boolean useVirtual = args[1].equalsIgnoreCase("virtual");
        boolean useOwnParser = args[2].equalsIgnoreCase("own");
        int poolSize = Integer.parseInt(args[3]);

        Object parser;
        if (useOwnParser) {
            parser = JsonMapper.builder().build();
        } else {
            parser = new Gson();
        }

        HttpServer server = HttpServer.create()
                .host("0.0.0.0")
                .port(port)
                .threadPoolSize(poolSize)
                .useVirtualThreads(useVirtual)
                .build();


        // ----- Request 1 (I/O bound) -----
        server.post("/req1", (req) -> {
            try {
                String body = req.getBodyAsString();
                Request1Data data;
                if (useOwnParser) {
                    data = ((JsonMapper) parser).fromJson(body, Request1Data.class);
                } else {
                    data = ((Gson) parser).fromJson(body, Request1Data.class);
                }
                String fileName = "storage_" + System.nanoTime() + ".tmp";
                Path file = Path.of(fileName);
                Files.writeString(file, data.data);
                String retrieved = Files.readString(file);
                Files.deleteIfExists(file);
                Response1Data respData = new Response1Data(data.data, retrieved);
                String jsonResp;
                if (useOwnParser) {
                    jsonResp = ((JsonMapper) parser).toJson(respData);
                } else {
                    jsonResp = ((Gson) parser).toJson(respData);
                }
                return HttpResponse.ok().json(jsonResp);
            } catch (Exception e) {
                e.printStackTrace();
                return HttpResponse.internalServerError().json("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // ----- Request 2 (memory bound: квадраты с кэшем) -----
        server.post("/req2", (req) -> {
            try {
                String body = req.getBodyAsString();
                Request2Data data;
                if (useOwnParser) {
                    data = ((JsonMapper) parser).fromJson(body, Request2Data.class);
                } else {
                    data = ((Gson) parser).fromJson(body, Request2Data.class);
                }
                long a = data.a;
                long result = cache.computeIfAbsent(a, key -> {
                    long sum = 0;
                    for (long i = 1; i <= key; i++) sum += i * i;
                    return sum;
                });
                Response2Data respData = new Response2Data(result);
                String jsonResp;
                if (useOwnParser) {
                    jsonResp = ((JsonMapper) parser).toJson(respData);
                } else {
                    jsonResp = ((Gson) parser).toJson(respData);
                }
                return HttpResponse.ok().json(jsonResp);
            } catch (Exception e) {
                e.printStackTrace();
                return HttpResponse.badRequest().json("{\"error\":\"Invalid input\"}");
            }
        });

        System.out.println("Server started on port " + port);
        System.out.println("Threads: " + (useVirtual ? "virtual" : "classic (pool=" + poolSize + ")"));
        System.out.println("Parser: " + (useOwnParser ? "own (JsonMapper)" : "Gson"));
        server.start();
    }
}