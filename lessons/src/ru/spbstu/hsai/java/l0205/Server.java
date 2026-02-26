package ru.spbstu.hsai.java.l0205;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Server {

	private static final int PORT = 8080;
	//private static final String usersDB = "users.txt";

	public static void main(String[] args) {
		try (ServerSocket myServer = new ServerSocket(PORT)) {
			System.out.println("Server has started!");
			
			//FileWriter file = new FileWriter(usersDB);
			

			while (true) {
				Socket socket = myServer.accept();
				ObjectInputStream request = new ObjectInputStream(socket.getInputStream());
				Request imageRequest = (Request) request.readObject();
				processImageRequest(imageRequest.getImageBytes());

				String user = imageRequest.getUsers();
				//file.write(user);
				
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				Response response = new Response("Success!", true);
				out.writeObject(response);

			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private static void processImageRequest(byte[] imageBytes) {
		System.out.println("We have read " + imageBytes.length + " bytes.");

	}

}
