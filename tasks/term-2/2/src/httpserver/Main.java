package httpserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		Server server = new Server("localhost", 8080, 10, true);

		// GET /
		server.addHandler(Server.Method.GET, "/", (req, resp) -> resp.setBody("Hello from HTTP server!"));

		// POST /register (принимает multipart/form-data)
		server.addHandler(Server.Method.POST, "/register", (req, resp) -> {
			Map<String, String> params = req.getFormData();
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

		// PUT /update
		server.addHandler(Server.Method.PUT, "/update", (req, resp) -> resp.setBody("Updated"));

		// PATCH /patch
		server.addHandler(Server.Method.PATCH, "/patch", (req, resp) -> resp.setBody("Patched"));

		// DELETE /delete
		server.addHandler(Server.Method.DELETE, "/delete", (req, resp) -> resp.setBody("Deleted"));

		server.start();
		System.out.println("Server started at http://localhost:8080");
		System.out.println("Press Enter to stop");
		System.in.read();
		server.stop();
	}
}