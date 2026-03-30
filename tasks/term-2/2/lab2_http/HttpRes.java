import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class HttpRes {
    private int statusCode = 200;
    private String statusText = "OK";
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public void setStatus(int code, String text) {
        this.statusCode = code;
        this.statusText = text;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        addHeader("Content-Length", String.valueOf(this.body.length));
    }

    public void setBody(byte[] body) {
        this.body = body;
        addHeader("Content-Length", String.valueOf(this.body.length));
    }

    public void send(OutputStream os) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
        writer.printf("HTTP/1.1 %d %s\r\n", statusCode, statusText);

        headers.putIfAbsent("Content-Type", "text/plain; charset=utf-8");
        headers.forEach((k, v) -> writer.printf("%s: %s\r\n", k, v));

        writer.print("\r\n");
        writer.flush();

        os.write(body);
        os.flush();
    }
}