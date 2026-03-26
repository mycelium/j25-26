import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

public class Main {

    // Флаг выбора парсера: true = собственная библиотека, false = Gson
    private static final boolean USE_OWN_PARSER = false;   // измените на false для теста с Gson

    public static void main(String[] args) throws IOException, InterruptedException {
        // Сервер: host, port, размер пула (не используется при virtual=true), флаг virtual
        Server server = new Server("localhost", 8080, 10, true);  // true – виртуальные потоки, false – классические

        // Обработчик POST /store (I/O нагрузка)
//        server.addHandler(Server.Method.POST, "/store", (req, resp) -> {
          server.addHandler(Server.Method.POST, "/compute", (req, resp) -> {
            try {
                String json = new String(req.getBody(), java.nio.charset.StandardCharsets.UTF_8);
                int id;
                String data;

                if (USE_OWN_PARSER) {
                    // Собственный парсер
                    Map<String, Object> map = JSON.parseToMap(json);
                    id = ((Number) map.get("id")).intValue();
                    data = (String) map.get("data");
                } else {
                    // Gson
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    StoreRequest reqData = gson.fromJson(json, StoreRequest.class);
                    id = reqData.id;
                    data = reqData.data;
                }

                // Имитация работы с хранилищем: запись в файл и чтение
                Files.writeString(Path.of("data.txt"), id + ":" + data + "\n",
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                List<String> lines = Files.readAllLines(Path.of("data.txt"));
                // lines не используется, просто имитация чтения

                // Формируем ответ
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
                resp.setStatus(500);
                resp.setBody("Error: " + e.getMessage());
            }
        });

        // Обработчик GET /compute (CPU нагрузка)
        server.addHandler(Server.Method.GET, "/compute", (req, resp) -> {
            try {
                String json = new String(req.getBody(), java.nio.charset.StandardCharsets.UTF_8);
                int n;

                if (USE_OWN_PARSER) {
                    Map<String, Object> map = JSON.parseToMap(json);
                    n = ((Number) map.get("value")).intValue();
                } else {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    ComputeRequest reqData = gson.fromJson(json, ComputeRequest.class);
                    n = reqData.value;
                }

                // CPU-нагрузка: вычисление факториала (итеративно, чтобы избежать переполнения стека)
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
                resp.setStatus(500);
                resp.setBody("Error: " + e.getMessage());
            }
        });

        server.start();
        System.out.println("Server started at http://localhost:8080");
        System.out.println("Press Enter to stop");
        System.in.read();
        server.stop();
    }

    // Итеративный факториал для стабильности
    private static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    // Вспомогательные классы для Gson
    static class StoreRequest {
        int id;
        String data;
    }

    static class ComputeRequest {
        int value;
    }
}