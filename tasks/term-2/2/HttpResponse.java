import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode = 200;
    private String statusMessage = "OK";
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public void setStatus(int code, String message) {
        this.statusCode = code;
        this.statusMessage = message;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void setBody(String text) {
        this.body = text.getBytes(StandardCharsets.UTF_8);
        setHeader("Content-Length", String.valueOf(this.body.length));
        if (!headers.containsKey("Content-Type")) {
            setHeader("Content-Type", "text/plain; charset=utf-8");
        }
    }

    public void setBody(byte[] data) {
        this.body = data;
        setHeader("Content-Length", String.valueOf(this.body.length));
    }

    public void send(OutputStream outputStream) throws IOException {
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
    
    if (!headers.containsKey("Connection")) {
        headers.put("Connection", "close");
    }

    writer.printf("HTTP/1.1 %d %s\r\n", statusCode, statusMessage);
    for (Map.Entry<String, String> entry : headers.entrySet()) {
        writer.printf("%s: %s\r\n", entry.getKey(), entry.getValue());
    }
    writer.print("\r\n");
    writer.flush();

    outputStream.write(body);
    outputStream.flush();
}
}
