package com.labs.server;

import httpserverlib.Request;
import httpserverlib.Response;
import com.labs.config.ParserMode;
import com.labs.json.JsonAdapter;
import java.util.Map;
import java.util.function.BiConsumer;


@FunctionalInterface
public interface RequestHandler extends BiConsumer<Request, Response> {

}

class Request1Handler implements RequestHandler {
    private final Storage storage;
    private final ParserMode parserMode;

    public Request1Handler(Storage storage, ParserMode parserMode) {
        this.storage = storage;
        this.parserMode = parserMode;
    }

    @Override
    public void accept(Request req, Response res) {
        try {

            Map<String, Object> input = JsonAdapter.parse(req.body, parserMode);
            String key = (String) input.get("key");
            String value = (String) input.get("value");

            if (key == null || value == null) {
                res.setStatus(400);
                res.setBody("{\"error\":\"Missing 'key' or 'value' field\"}");
                return;
            }


            storage.save(key, value);


            String retrieved = storage.load(key);


            Map<String, String> response = Map.of(
                    "status", "ok",
                    "key", key,
                    "retrieved", retrieved != null ? retrieved : "null"
            );
            String json = JsonAdapter.toJson(response, parserMode);

            res.setHeader("Content-Type", "application/json");
            res.setBody(json);
            res.setStatus(200);

        } catch (Exception e) {
            res.setStatus(500);
            res.setBody("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}


class Request2Handler implements RequestHandler {
    private final ParserMode parserMode;

    public Request2Handler(ParserMode parserMode) {
        this.parserMode = parserMode;
    }

    @Override
    public void accept(Request req, Response res) {
        try {
            Map<String, Object> input = JsonAdapter.parse(req.body, parserMode);
            Number num = (Number) input.get("number");

            if (num == null) {
                res.setStatus(400);
                res.setBody("{\"error\":\"Missing 'number' field\"}");
                return;
            }

            long n = num.longValue();


            long result = compute(n);

            Map<String, Object> response = Map.of(
                    "status", "ok",
                    "input", n,
                    "result", result,
                    "computation", "n*n + n*31 + (nanoTime % 100)"
            );
            String json = JsonAdapter.toJson(response, parserMode);

            res.setHeader("Content-Type", "application/json");
            res.setBody(json);
            res.setStatus(200);

        } catch (Exception e) {
            res.setStatus(500);
            res.setBody("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }


    private long compute(long n) {
        long sum = 0;

        for (int i = 0; i < 100; i++) {
            sum += n * n + n * 31 + (System.nanoTime() % 100);
        }
        return sum / 100;
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}