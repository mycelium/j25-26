package ru.spbstu.hsai.java.l0205;

import java.io.BufferedReader;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Client {

	private static final String IP = "127.0.0.1";
	private static final int PORT = 8080;
	private static final String image = "image.jpeg";

	public static void main(String[] args) {
		try (Socket socket = new Socket(IP, PORT)) {

			Path path = Path.of(image);
			// File file = new File(path);
			byte[] bytes = Files.readAllBytes(path);

			User user = new User("S", "Sofid", "12345");
			Request request = new Request(bytes, user);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(request);
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			Response response = (Response) input.readObject();
			System.out.println(response.getTextResponse());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

}