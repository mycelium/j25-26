package perf;

import com.google.gson.Gson;
import http.HttpMethod;
import http.HttpServer;
import jsonlib.Json;
import jsonlib.JsonType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class BenchServer {

    private static final int LISTEN_PORT = 8081;
    private static final int CLASSIC_THREADS = 50;
    private static final int COMPUTE_PASSES = 128;
    private static final Path DATA_DIR = Path.of("storage");

    public static void main(String[] argv) throws Exception {
        if (argv.length < 2) {
            System.err.println("Usage: BenchServer <own|gson> <virtual|classic>");
            System.exit(1);
        }

        final boolean withGson = "gson".equalsIgnoreCase(argv[0]);
        final boolean withVirtual = "virtual".equalsIgnoreCase(argv[1]);

        Files.createDirectories(DATA_DIR);

        final Gson gson = new Gson();
        final Json ownJson = Json.builder().build();

        HttpServer srv = HttpServer.builder()
                .host("0.0.0.0")
                .port(LISTEN_PORT)
                .threads(CLASSIC_THREADS)
                .virtual(withVirtual)
                .build();

        final AtomicReference<HttpServer> serverRef = new AtomicReference<>(srv);

        srv.on(HttpMethod.POST, "/file", (req, resp) -> {
            try {
                Map<String, Object> payload = decode(req.bodyAsString(), withGson, gson, ownJson);
                String data = String.valueOf(payload.get("data"));

                Path target = DATA_DIR.resolve(UUID.randomUUID() + ".txt");
                Files.writeString(target, data);
                String roundtrip = Files.readString(target);
                long checksum = checksum(roundtrip);

                Map<String, Object> answer = new LinkedHashMap<>();
                answer.put("size", roundtrip.length());
                answer.put("checksum", checksum);
                answer.put("ok", roundtrip.equals(data));

                resp.header("Content-Type", "application/json")
                        .writeText(encode(answer, withGson, gson, ownJson));
            } catch (Exception ex) {
                resp.status(500).writeText(String.valueOf(ex.getMessage()));
            }
        });

        srv.on(HttpMethod.POST, "/compute", (req, resp) -> {
            try {
                Map<String, Object> payload = decode(req.bodyAsString(), withGson, gson, ownJson);
                List<?> values = (List<?>) payload.get("numbers");

                double total = 0.0;
                double squareTotal = 0.0;
                double weightedTotal = 0.0;
                for (int pass = 0; pass < COMPUTE_PASSES; pass++) {
                    for (int i = 0; i < values.size(); i++) {
                        double value = ((Number) values.get(i)).doubleValue();
                        total += value;
                        squareTotal += value * value;
                        weightedTotal += value * (i + 1 + pass);
                    }
                }
                double mean = total / (values.size() * COMPUTE_PASSES);

                Map<String, Object> answer = new LinkedHashMap<>();
                answer.put("count", values.size());
                answer.put("sum", total);
                answer.put("squareSum", squareTotal);
                answer.put("weightedSum", weightedTotal);
                answer.put("avg", mean);

                resp.header("Content-Type", "application/json")
                        .writeText(encode(answer, withGson, gson, ownJson));
            } catch (Exception ex) {
                resp.status(500).writeText(String.valueOf(ex.getMessage()));
            }
        });

        srv.on(HttpMethod.GET, "/health", (req, resp) -> resp.writeText("ok"));

        srv.on(HttpMethod.POST, "/shutdown", (req, resp) -> {
            resp.writeText("stopping");
            Thread killer = new Thread(() -> {
                try {
                    Thread.sleep(120);
                    serverRef.get().stop();
                } catch (Exception ignored) {
                } finally {
                    System.exit(0);
                }
            }, "bench-shutdown");
            killer.setDaemon(true);
            killer.start();
        });

        System.out.printf("BenchServer started: parser=%s, threads=%s, port=%d%n",
                argv[0], argv[1], LISTEN_PORT);
        srv.start();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> decode(String raw, boolean useGson, Gson gson, Json json) {
        if (useGson) {
            return gson.fromJson(raw, Map.class);
        }
        return json.fromJson(raw, JsonType.mapOf(String.class, Object.class));
    }

    private static String encode(Object obj, boolean useGson, Gson gson, Json json) {
        return useGson ? gson.toJson(obj) : json.toJson(obj);
    }

    private static long checksum(String text) {
        long result = 1125899906842597L;
        for (int i = 0; i < text.length(); i++) {
            result = 31 * result + text.charAt(i);
        }
        return result;
    }
}
