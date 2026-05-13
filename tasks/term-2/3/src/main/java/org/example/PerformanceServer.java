package org.example;

import com.google.gson.Gson;
import org.api.HttpResponse;
import org.api.HttpServer;
import org.api.ServerConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PerformanceServer {

    private static final Map<String, Integer> CACHE = new ConcurrentHashMap<>();
    private static final JsonLib              JSON  = new JsonLib();
    private static final Gson                 GSON  = new Gson();

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: PerformanceServer <port> <virtual|classic> <own|gson> <poolSize>");
            System.exit(1);
        }

        int     port      = Integer.parseInt(args[0]);
        boolean isVirtual = args[1].equalsIgnoreCase("virtual");
        boolean useGson   = args[2].equalsIgnoreCase("gson");
        int     poolSize  = Integer.parseInt(args[3]);

        ServerConfig config = ServerConfig.builder()
                .host("0.0.0.0")
                .port(port)
                .threadCount(poolSize)
                .isVirtual(isVirtual)
                .build();

        HttpServer server = new HttpServer(config);

        server.post("/request1", req -> {
            try {
                String  body    = req.bodyAsString();
                Payload payload = useGson
                        ? GSON.fromJson(body, Payload.class)
                        : JSON.fromJson(body, Payload.class);

                String filename = "perf_" + UUID.randomUUID() + ".tmp";
                Path   file     = Path.of(System.getProperty("java.io.tmpdir"), filename);

                Files.writeString(file, payload.data());
                String readBack = Files.readString(file);
                Files.delete(file);

                Result result   = new Result("ok", readBack.length());
                String response = useGson
                        ? GSON.toJson(result)
                        : JSON.toJson(result);

                return HttpResponse.ok().json(response);
            } catch (Exception e) {
                return HttpResponse.internalServerError().body(e.getMessage());
            }
        });

        server.post("/request2", req -> {
            try {
                String  body    = req.bodyAsString();
                Payload payload = useGson
                        ? GSON.fromJson(body, Payload.class)
                        : JSON.fromJson(body, Payload.class);

                int    n        = payload.n();
                String key      = "fib_" + n;
                int    value    = CACHE.computeIfAbsent(key, k -> fibonacci(n));

                Result res      = new Result("ok", value);
                String response = useGson
                        ? GSON.toJson(res)
                        : JSON.toJson(res);

                return HttpResponse.ok().json(response);
            } catch (Exception e) {
                return HttpResponse.internalServerError().body(e.getMessage());
            }
        });

        server.start();
        System.out.printf("PerformanceServer started: port=%d threads=%s parser=%s poolSize=%d%n",
                port, isVirtual ? "virtual" : "classic", useGson ? "gson" : "own", poolSize);
        System.out.println("Press Enter to stop.");
        System.in.read();
        server.stop();
    }

    private static int fibonacci(int n) {
        if (n <= 1) return n;
        int a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            int tmp = a + b;
            a = b;
            b = tmp;
        }
        return b;
    }
}
