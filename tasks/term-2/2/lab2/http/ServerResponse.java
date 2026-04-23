package lab2.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ServerResponse {
    private final int httpCode;
    private final String reasonPhrase;
    private final String payload;
    private final Map<String, String> responseHeaders;

    public ServerResponse(int httpCode, String reasonPhrase, String payload) {
        this.httpCode = httpCode;
        this.reasonPhrase = reasonPhrase;
        this.payload = (payload == null) ? "" : payload;
        this.responseHeaders = new HashMap<>();

        // Базовые заголовки задаются сразу
        this.responseHeaders.put("Connection", "close");
        this.responseHeaders.put("Content-Type", "application/json; charset=utf-8");
        this.responseHeaders.put("Content-Length", String.valueOf(this.payload.getBytes(StandardCharsets.UTF_8).length));
    }

    public void injectHeader(String headerName, String headerValue) {
        this.responseHeaders.put(headerName, headerValue);
    }

    public byte[] generateBytes() {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ").append(httpCode).append(" ").append(reasonPhrase).append("\r\n");

        for (Map.Entry<String, String> hdr : responseHeaders.entrySet()) {
            builder.append(hdr.getKey()).append(": ").append(hdr.getValue()).append("\r\n");
        }

        builder.append("\r\n").append(payload);
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}