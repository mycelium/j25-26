import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpClient {
    
    private String host;
    private int port;
    
    public HttpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String get(String path) {
        return sendRequest("GET", path, null, null);
    }

    public String post(String path, String body) {
        return sendRequest("POST", path, "application/json", body);
    }

    public String put(String path, String body) {
        return sendRequest("PUT", path, "application/json", body);
    }

    public String delete(String path) {
        return sendRequest("DELETE", path, null, null);
    }

    private String sendRequest(String method, String path, String contentType, String body) {
        StringBuilder request = new StringBuilder();

        request.append(method).append(" ").append(path).append(" HTTP/1.1\r\n");
        request.append("Host: ").append(host).append(":").append(port).append("\r\n");

        if (contentType != null) {
            request.append("Content-Type: ").append(contentType).append("\r\n");
        }
        
        if (body != null) {
            request.append("Content-Length: ").append(body.length()).append("\r\n");
        }

        request.append("\r\n");

        if (body != null) {
            request.append(body);
        }
        System.out.println("запрос:");
        System.out.println(request.toString());
        
        try (Socket socket = new Socket(host, port);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            out.write(request.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();

            StringBuilder response = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            
            String line;
            boolean bodyStarted = false;
            
            while ((line = reader.readLine()) != null) {
                if (bodyStarted) {
                    response.append(line).append("\n");
                } else if (line.isEmpty()) {
                    bodyStarted = true;
                }
            }
            
            String responseBody = response.toString().trim();
            System.out.println("ответ:");
            System.out.println(responseBody);
            System.out.println();
            
            return responseBody;
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}