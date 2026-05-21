package papka_HTTP;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HTTPResponse {

    private int m_status;
    private String m_statusMes;
    private final Map<String, String> m_headers = new HashMap<>();
    private byte[] m_body = new byte[0];

    public HTTPResponse() {
        m_headers.put("Content-Type", "text/plain; charset=utf-8");
    }

    public void setStutus(int status)
    {
        this.m_status = status;
        switch (m_status) {
            case 200: m_statusMes = "OK"; break;
            case 201: m_statusMes = "Created"; break;
            case 400: m_statusMes = "Bad Request"; break;
            case 404: m_statusMes = "Not Found"; break;
            case 500: m_statusMes = "Internal Server Error"; break;
            default: m_statusMes = "Unknown";

        }
    }

    public void setBody(String body) {
        this.m_body = body.getBytes(StandardCharsets.UTF_8);
        m_headers.put("Content-Length", String.valueOf(this.m_body.length));
    }

    public byte[] toBytes() {
        StringBuilder response = new StringBuilder();

        response.append("HTTP/1.1 ")
                .append(m_status)
                .append(" ")
                .append(m_statusMes)
                .append("\r\n");

        for (Map.Entry<String, String> header : m_headers.entrySet()) {
            response.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        response.append("\r\n");

        if (m_body != null && m_body.length > 0) {
            response.append(new String(m_body, StandardCharsets.UTF_8));
        }

        return response.toString().getBytes(StandardCharsets.UTF_8);
    }
}
