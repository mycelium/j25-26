import server.Server;
import server.Server.Response;
import tokenizer.Json;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class Main {

    private static final int PORT = 8080;
    private static final int THREADS = 100;

    private static final Gson gson = new Gson();
    private static final Path FILE = Path.of("data.txt");
    private static final Object FILE_LOCK = new Object();

    public static void main(String[] args) throws IOException {
        boolean useVirtualThreads = args.length > 0 && args[0].equalsIgnoreCase("virtual");
        boolean useGson = args.length > 1 && args[1].equalsIgnoreCase("gson");

        Server server = Server.builder()
                .port(PORT)
                .threads(THREADS)
                .virtualThreads(useVirtualThreads)
                .build();

        server.post("/api/store", request -> {
            try {
                String json = request.bodyAsString();
                Object parsed = parse(json, useGson);

                String data;

                synchronized (FILE_LOCK) {
                    Files.writeString(
                            FILE,
                            stringify(parsed, useGson) + "\n",
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND
                    );

                    data = Files.readString(FILE);
                }

                String responseJson = stringify(Map.of(
                        "status", "stored",
                        "fileSize", data.length()
                ), useGson);

                return new Response(200, "OK", responseJson.getBytes())
                        .header("Content-Type", "application/json");

            } catch (Exception e) {
                e.printStackTrace();
                return Response.internalServerError();
            }
        });

        server.post("/api/compute", request -> {
            try {
                String json = request.bodyAsString();
                Object parsed = parse(json, useGson);

                long result = 0;
                for (int i = 0; i < 100_000; i++) {
                    result += i;
                }

                String responseJson = stringify(Map.of(
                        "result", result,
                        "inputType", parsed.getClass().getSimpleName()
                ), useGson);

                return new Response(200, "OK", responseJson.getBytes())
                        .header("Content-Type", "application/json");

            } catch (Exception e) {
                e.printStackTrace();
                return Response.internalServerError();
            }
        });

        System.out.println("Server started on port " + PORT);
        System.out.println("Threads: " + (useVirtualThreads ? "virtual" : "classic"));
        System.out.println("Parser: " + (useGson ? "gson" : "own"));

        server.start();
    }

    private static Object parse(String json, boolean useGson) {
        if (useGson) {
            return gson.fromJson(json, Object.class);
        } else {
            return Json.parse(json);
        }
    }

    private static String stringify(Object obj, boolean useGson) {
        if (useGson) {
            return gson.toJson(obj);
        } else {
            return Json.stringify(obj);
        }
    }
}