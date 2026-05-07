package loadtest;

import httpserver.HttpMethod;
import httpserver.HttpResponse;
import httpserver.HttpServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

final class BenchmarkHttpApp {
    private BenchmarkHttpApp() {
    }

    static HttpServer create(String host, int port, int threadCount, BenchmarkVariant variant, Path runtimeDir)
            throws IOException {
        JsonCodec codec = JsonCodecs.create(variant.backend());
        FileBackedStore store = new FileBackedStore(runtimeDir.resolve("store-" + variant.fileName() + ".jsonl"));
        MemoryCatalog memoryCatalog = MemoryCatalog.createDefault();

        HttpServer server = new HttpServer(host, port, threadCount, variant.isVirtual());
        server.addListener(HttpMethod.POST, BenchmarkEndpoint.REQUEST_1.path(), request -> {
            Map<String, Object> payload = codec.parseObject(request.getBodyAsString());
            Map<String, Object> stored = createStoredEvent(payload, variant);
            String storedJson = codec.toJson(stored);
            String retrievedJson = store.appendAndReadLast(storedJson);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("ok", true);
            response.put("request", BenchmarkEndpoint.REQUEST_1.displayName());
            response.put("variant", variant.displayName());
            response.put("storedBytes", storedJson.length());
            response.put("retrievedBytes", retrievedJson.length());
            response.put("retrievedChecksum", checksum(retrievedJson));
            response.put("source", "file");
            return jsonResponse(codec.toJson(response));
        });

        server.addListener(HttpMethod.POST, BenchmarkEndpoint.REQUEST_2.path(), request -> {
            Map<String, Object> payload = codec.parseObject(request.getBodyAsString());
            List<?> numbers = JsonValues.list(payload.get("numbers"));
            long userId = JsonValues.longValue(payload.get("userId"), 0L);
            Map<String, Object> memoryItem = memoryCatalog.byUserId(userId);
            long multiplier = JsonValues.longValue(memoryItem.get("multiplier"), 1L);
            long bias = JsonValues.longValue(memoryItem.get("bias"), 0L);

            long sum = 0L;
            long weighted = 0L;
            for (Object number : numbers) {
                long value = JsonValues.longValue(number, 0L);
                sum += value;
                weighted += (value * multiplier) + bias;
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("ok", true);
            response.put("request", BenchmarkEndpoint.REQUEST_2.displayName());
            response.put("variant", variant.displayName());
            response.put("source", "memory");
            response.put("memoryKey", memoryItem.get("key"));
            response.put("numbers", numbers.size());
            response.put("sum", sum);
            response.put("weightedScore", weighted);
            response.put("average", numbers.isEmpty() ? 0.0 : (double) sum / numbers.size());
            return jsonResponse(codec.toJson(response));
        });

        return server;
    }

    private static Map<String, Object> createStoredEvent(Map<String, Object> payload, BenchmarkVariant variant) {
        List<?> numbers = JsonValues.list(payload.get("numbers"));
        long sum = 0L;
        for (Object number : numbers) {
            sum += JsonValues.longValue(number, 0L);
        }

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("requestId", JsonValues.stringValue(payload.get("requestId"), "unknown"));
        event.put("userId", JsonValues.longValue(payload.get("userId"), 0L));
        event.put("category", JsonValues.stringValue(payload.get("category"), "none"));
        event.put("variant", variant.fileName());
        event.put("numbersCount", numbers.size());
        event.put("numbersSum", sum);
        event.put("payloadChecksum", checksum(payload.toString()));
        event.put("storedAt", System.currentTimeMillis());
        return event;
    }

    private static HttpResponse jsonResponse(String json) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", BenchmarkDefaults.CONTENT_TYPE);
        return new HttpResponse(200, headers, json.getBytes(StandardCharsets.UTF_8));
    }

    private static long checksum(String value) {
        CRC32 crc32 = new CRC32();
        crc32.update(value.getBytes(StandardCharsets.UTF_8));
        return crc32.getValue();
    }
}
