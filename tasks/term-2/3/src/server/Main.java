package server;

import parser.SimpleJson;
import com.google.gson.Gson;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    private static final String STORAGE_FILE = "data/storage.json";
    private static final Object FILE_LOCK = new Object();
    private static final Gson gson = new Gson();

    public static void startServer(String host, int port, int threads,
                                   boolean isVirtual, boolean useOwnParser) throws IOException {

        HttpServ server = new HttpServ(host, port, threads, isVirtual);

        //REQUEST 1
        server.addListener("POST", "/store", (req, res) -> {
            try {
                String body = req.getBody();

                Map<String, Object> data;
                if (useOwnParser) {
                    data = SimpleJson.fromJson(body);
                } else {
                    data = gson.fromJson(body, Map.class);
                }

                String toWrite = (useOwnParser
                        ? SimpleJson.toJson(data)
                        : gson.toJson(data)) + "\n";

                String lastLine;
                synchronized (FILE_LOCK) {
                    Files.createDirectories(Paths.get("data"));
                    Files.writeString(Paths.get(STORAGE_FILE), toWrite,
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                    List<String> lines = Files.readAllLines(Paths.get(STORAGE_FILE));
                    lastLine = lines.isEmpty() ? "{}" : lines.get(lines.size() - 1);
                }

                res.setStatus(200, "OK");
                res.addHeader("Content-Type", "application/json");
                res.setBody(lastLine);

            } catch (Exception e) {
                res.setStatus(500, "Internal Server Error");
                res.setBody("Error: " + e.getMessage());
            }
        });

        //REQUEST 2
        server.addListener("POST", "/calculate", (req, res) -> {
            try {
                String body = req.getBody();

                Map<String, Object> data;
                if (useOwnParser) {
                    data = SimpleJson.fromJson(body);
                } else {
                    data = gson.fromJson(body, Map.class);
                }

                double sum = 0;
                for (Object val : data.values()) {
                    if (val instanceof Number) {
                        sum += ((Number) val).doubleValue();
                    }
                }

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("sum", sum);
                result.put("fields", data.size());

                String response = useOwnParser
                        ? SimpleJson.toJson(result)
                        : gson.toJson(result);

                res.addHeader("Content-Type", "application/json");
                res.setBody(response);

            } catch (Exception e) {
            	e.printStackTrace();
                res.setStatus(500, "Internal Server Error");
                res.setBody("Error: " + e.getMessage());
            }
        });

        System.out.println("Config: isVirtual=" + isVirtual + ", useOwnParser=" + useOwnParser);
        server.start();
    }
}