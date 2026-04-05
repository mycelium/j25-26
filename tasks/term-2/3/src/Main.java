
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

public class Main {

    // флаги переопределяются через аргументы командной строки
    private static boolean USE_VIRTUAL = true;
    private static boolean USE_OWN_PARSER = true;

    public static void main(String[] args) throws IOException, InterruptedException {
        // разбор аргументов: первый - virtual/classic, второй - own/gson
        if (args.length >= 1) {
            USE_VIRTUAL = args[0].equalsIgnoreCase("virtual");
        }
        if (args.length >= 2) {
            USE_OWN_PARSER = args[1].equalsIgnoreCase("own");
        }

        System.out.println("Threads: " + (USE_VIRTUAL ? "Virtual" : "Classic"));
        System.out.println("Parser:  " + (USE_OWN_PARSER ? "Own" : "Gson"));
        System.out.println("");

        Server server = new Server("localhost", 8080, 10, USE_VIRTUAL);

        // эндпоинт 1 (I/O нагрузка) – POST /store
        server.addHandler(Server.Method.POST, "/store", (req, resp) -> {
            try {
                byte[] bodyBytes = req.getBody();
                if (bodyBytes == null || bodyBytes.length == 0) {
                    resp.setStatus(400);
                    resp.setBody("{\"error\":\"Missing body\"}");
                    resp.setHeader("Content-Type", "application/json");
                    return;
                }
                String json = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);

                int id;
                String data;
                if (USE_OWN_PARSER) {
                    Map<String, Object> map = JSON.parseToMap(json);
                    id = ((Number) map.get("id")).intValue();
                    data = (String) map.get("data");
                } else {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    StoreRequest reqData = gson.fromJson(json, StoreRequest.class);
                    id = reqData.id;
                    data = reqData.data;
                }

                // запись и чтение файла (мое)
                Files.writeString(Path.of("data.txt"), id + ":" + data + "\n",
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                List<String> lines = Files.readAllLines(Path.of("data.txt"));

                String responseJson;
                if (USE_OWN_PARSER) {
                    responseJson = JSON.toJson(Map.of("status", "ok", "id", id));
                } else {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    responseJson = gson.toJson(Map.of("status", "ok", "id", id));
                }
                resp.setBody(responseJson);
                resp.setHeader("Content-Type", "application/json");
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(500);
                resp.setBody("{\"error\":\"" + e.getMessage() + "\"}");
                resp.setHeader("Content-Type", "application/json");
            }
        });

        // эндпоинт 2 (CPU нагрузка) – POST /compute
        server.addHandler(Server.Method.POST, "/compute", (req, resp) -> {
            try {
                byte[] bodyBytes = req.getBody();
                if (bodyBytes == null || bodyBytes.length == 0) {
                    resp.setStatus(400);
                    resp.setBody("{\"error\":\"Missing body\"}");
                    resp.setHeader("Content-Type", "application/json");
                    return;
                }
                String json = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);

                int n;
                if (USE_OWN_PARSER) {
                    Map<String, Object> map = JSON.parseToMap(json);
                    Object val = map.get("value");
                    if (val == null) {
                        resp.setStatus(400);
                        resp.setBody("{\"error\":\"Missing 'value' field\"}");
                        resp.setHeader("Content-Type", "application/json");
                        return;
                    }
                    n = ((Number) val).intValue();
                } else {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    ComputeRequest reqData = gson.fromJson(json, ComputeRequest.class);
                    n = reqData.value;
                }

                // CPU-нагрузка вычисление факториала (итеративно)
                long result = factorial(n);
                String responseJson;
                if (USE_OWN_PARSER) {
                    responseJson = JSON.toJson(Map.of("result", result));
                } else {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    responseJson = gson.toJson(Map.of("result", result));
                }
                resp.setBody(responseJson);
                resp.setHeader("Content-Type", "application/json");
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(500);
                resp.setBody("{\"error\":\"" + e.getMessage() + "\"}");
                resp.setHeader("Content-Type", "application/json");
            }
        });

        server.start();
        System.out.println("Server started at http://localhost:8080");
        System.out.println("Press Enter to stop");
        System.in.read();
        server.stop();
    }

    private static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }

    // вспомогательные классы для Gson
    static class StoreRequest { int id; String data; }
    static class ComputeRequest { int value; }
}
