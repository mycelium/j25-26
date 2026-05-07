package loadtest;

import httpserver.HttpServer;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public final class LoadTestServer {
    private LoadTestServer() {
    }

    public static void main(String[] args) throws Exception {
        CliOptions options = CliOptions.parse(args);
        String host = options.get("host", BenchmarkDefaults.HOST);
        int port = options.getInt("port", BenchmarkDefaults.BASE_PORT);
        int threads = options.getInt("threads", BenchmarkDefaults.SERVER_THREADS);
        Path runtimeDir = options.getPath("runtime-dir", Path.of("3", "runtime"));

        BenchmarkVariant variant;
        if (options.has("variant")) {
            variant = BenchmarkVariant.fromValue(options.get("variant", ""));
        } else {
            boolean isVirtual = options.getBoolean("virtual", true);
            JsonBackend backend = JsonBackend.fromValue(options.get("parser", "own"));
            variant = BenchmarkVariant.fromParts(isVirtual, backend);
        }

        HttpServer server = BenchmarkHttpApp.create(host, port, threads, variant, runtimeDir);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (Exception ignored) {
                // Shutdown is best-effort here.
            }
        }));
        server.start();

        System.out.println("Load test server is running.");
        System.out.println("Variant: " + variant.displayName());
        System.out.println("URL: http://" + host + ":" + port);
        System.out.println("Endpoints: POST /request-1, POST /request-2");

        if (options.getBoolean("wait-forever", false)) {
            new CountDownLatch(1).await();
        } else {
            System.out.println("Press Enter to stop.");
            System.in.read();
            server.stop();
        }
    }
}
