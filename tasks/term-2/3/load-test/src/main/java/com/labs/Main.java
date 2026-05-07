package com.labs;
import com.labs.client.LoadTester;
import com.labs.db.SqliteDb;
import httpserver.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        int threads = 50;
        int reqs = 100;
        String body1 = "{\"key\":\"k1\",\"value\":\"v1\"}";
        String body2 = "{\"number\":42}";

        System.out.println("Запуск нагрузочных тестов...");
        Map<String, Double> results = runAllVariants(port, threads, reqs, body1, body2);
        printTable(results);
    }

    private static Map<String, Double> runAllVariants(int port, int t, int r, String b1, String b2) throws Exception {
        var res = new LinkedHashMap<String, Double>();
        String[][] names = {{"Virtual", "true"}, {"Classic", "false"}};
        String[][] parsers = {{"own parser", "OwnJsonParser"}, {"GSON", "GsonParser"}};

        for (var threadSet : names) {
            for (var parserSet : parsers) {
                String variant = threadSet[0] + " + " + parserSet[0];
                boolean isVirtual = Boolean.parseBoolean(threadSet[1]);
                JsonParser parser = parserSet[1].equals("OwnJsonParser") ? new OwnJsonParser() : new GsonParser();

                System.out.printf("\nТест: %s%n", variant);
                runVariant(res, variant, isVirtual, parser, port, t, r, b1, b2);
            }
        }
        return res;
    }

    private static void runVariant(Map<String, Double> res, String name, boolean isVirtual, JsonParser parser,
                                   int port, int threads, int reqs, String b1, String b2) throws Exception {
        File dbFile = new File("test.db");
        if (dbFile.exists()) dbFile.delete();
        SqliteDb db = new SqliteDb("test.db");

        HttpServer server = new HttpServer("localhost", port, 200, isVirtual);
        server.addRoute(HttpMethod.POST, "/request1", req -> handleReq1(req, db, parser));
        server.addRoute(HttpMethod.POST, "/request2", req -> handleReq2(req, parser));

        server.start();
        Thread.sleep(800); // прогрев

        double avg1 = LoadTester.run("http://localhost:" + port + "/request1", b1, threads, reqs);
        double avg2 = LoadTester.run("http://localhost:" + port + "/request2", b2, threads, reqs);

        server.stop();
        db.close();
        res.put(name + "-req1", avg1);
        res.put(name + "-req2", avg2);
        System.out.printf("Request 1: %.2f ms | Request 2: %.2f ms%n", avg1, avg2);
    }

    private static HttpResponse handleReq1(HttpRequest req, SqliteDb db, JsonParser p) throws Exception {
        var data = p.parse(req.getBodyAsString());
        String key = String.valueOf(data.get("key"));
        String value = String.valueOf(data.get("value"));
        db.store(key, value);
        String retrieved = db.retrieve(key);
        String json = p.toJson(Map.of("retrieved", retrieved));
        return new HttpResponse(200, json.getBytes(StandardCharsets.UTF_8), "application/json");
    }

    private static HttpResponse handleReq2(HttpRequest req, JsonParser p) throws Exception {
        var data = p.parse(req.getBodyAsString());
        int n = ((Number) data.get("number")).intValue();
        int res = n * n + n * 31;
        String json = p.toJson(Map.of("result", res));
        return new HttpResponse(200, json.getBytes(StandardCharsets.UTF_8), "application/json");
    }

    private static void printTable(Map<String, Double> r) {
        System.out.println("\nРЕЗУЛЬТАТЫ:");
        System.out.println("| req       | Virtual + own parser | Virtual + GSON | Classic + own parser | Classic + GSON |");
        System.out.println("|-----------|----------------------|---------------|----------------------|----------------|");
        printRow(r, "Request-1", "Virtual + own parser-req1", "Virtual + GSON-req1", "Classic + own parser-req1", "Classic + GSON-req1");
        printRow(r, "Request-2", "Virtual + own parser-req2", "Virtual + GSON-req2", "Classic + own parser-req2", "Classic + GSON-req2");
    }

    private static void printRow(Map<String, Double> r, String req, String k1, String k2, String k3, String k4) {
        System.out.printf("| %-9s | %-20s | %-13s | %-20s | %-14s |%n", req, fmt(r.get(k1)), fmt(r.get(k2)), fmt(r.get(k3)), fmt(r.get(k4)));
    }
    private static String fmt(Double v) { return v == null ? "N/A" : String.format("%.2f ms", v); }
}