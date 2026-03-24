import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		Server server = new Server("localhost", 8080, 10, true); // последний параметр включает Executor на основе
																	// newVirtualThreadPerTaskExecutor()
// обработчики
		server.addHandler(Server.Method.GET, "/", (req, resp) -> resp.setBody("Hello from HTTP server!"));

		server.addHandler(Server.Method.POST, "/register", (req, resp) -> {
			//формат строки application/x-www-form-urlencoded
			String body = new String(req.getBody(), java.nio.charset.StandardCharsets.UTF_8);
			Map<String, String> params = parseUrlEncoded(body);
			String name = params.get("name");
			String login = params.get("login");
			String password = params.get("password");
			if (name == null || login == null || password == null) {
				resp.setStatus(400);
				resp.setBody("Missing fields");
				return;
			}
			Files.writeString(Path.of("users.txt"), name + " " + login + " " + password + "\n",
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			resp.setBody("User registered: " + name);
		});

		server.addHandler(Server.Method.PUT, "/update", (req, resp) -> resp.setBody("Updated"));
		server.addHandler(Server.Method.PATCH, "/patch", (req, resp) -> resp.setBody("Patched"));
		server.addHandler(Server.Method.DELETE, "/delete", (req, resp) -> resp.setBody("Deleted"));

		server.start();
		System.out.println("Server started at http://localhost:8080");
		System.out.println("Press Enter to stop");
		System.in.read();  // блокирует выполнение, пока не нажат Enter
		server.stop();
	}

	private static Map<String, String> parseUrlEncoded(String data) {
		Map<String, String> map = new java.util.HashMap<>();
		for (String pair : data.split("&")) {
			int eq = pair.indexOf('=');
			if (eq > 0) {
				map.put(pair.substring(0, eq), pair.substring(eq + 1));
			}
		}
		return map;
	}
}