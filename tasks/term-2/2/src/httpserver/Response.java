package httpserver;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Response {
	private int status = 200;
	private final Map<String, String> headers = new HashMap<>();
	private byte[] body = new byte[0];

	public Response() {
		headers.put("Content-Type", "text/plain");
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setHeader(String name, String value) {
		headers.put(name, value);
	}

	public void setBody(byte[] body) {
		this.body = body != null ? body : new byte[0];
		headers.put("Content-Length", String.valueOf(this.body.length));
	}

	public void setBody(String body) {
		setBody(body.getBytes(StandardCharsets.UTF_8));
	}

	public void send(OutputStream outputStream) throws java.io.IOException {
		// строим ответ в виде строки (заголовки + тело)
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 ").append(status).append(" ").append(getStatusText(status)).append("\r\n");
		for (Map.Entry<String, String> e : headers.entrySet()) {
			sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
		}
		sb.append("\r\n");
		// отправляем заголовки
		outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
		// отправляем тело (если есть)
		if (body.length > 0) {
			outputStream.write(body);
		}
		outputStream.flush();
	}

	private String getStatusText(int code) {
		switch (code) {
		case 200:
			return "OK";
		case 404:
			return "Not Found";
		case 500:
			return "Internal Server Error";
		default:
			return "Unknown";
		}
	}
}