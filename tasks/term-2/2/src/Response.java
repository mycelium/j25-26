import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Response {
	private int status = 200;
	private final Map<String, String> headers = new HashMap<>();
	private byte[] body = new byte[0];

	public Response() {
		headers.put("Content-Type", "text/plain"); // устанавливается заголовок
	}

	public void setStatus(int status) { // устанавливает код статуса
		this.status = status;
	} 

	public void setHeader(String name, String value) { // добавляет или заменяет заголовок
		headers.put(name, value);
	} 

	public void setBody(byte[] body) { // устанавливает тело как массив байт
		this.body = body != null ? body : new byte[0];
		headers.put("Content-Length", String.valueOf(this.body.length));
	}

	public void setBody(String body) { // преобразует строку в байты
		setBody(body.getBytes(StandardCharsets.UTF_8));
	}

	public void send(OutputStream outputStream) throws java.io.IOException { // запись HTTP-ответа в переданный OutputStream
		PrintWriter writer = new PrintWriter(outputStream, true);
		writer.print("HTTP/1.1 " + status + " " + getStatusText(status) + "\r\n");
		for (Map.Entry<String, String> e : headers.entrySet()) {
			writer.print(e.getKey() + ": " + e.getValue() + "\r\n");
		}
		writer.print("\r\n");
		writer.flush();
		if (body.length > 0) {
			outputStream.write(body);
			outputStream.flush();
		}
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