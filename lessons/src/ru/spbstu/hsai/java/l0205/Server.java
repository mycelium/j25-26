package ru.spbstu.hsai.java.l0205;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private static final int PORT = 8080;
	
	public static void main(String[] args) {
		try (ServerSocket myServer = new ServerSocket(PORT)) {
			System.out.println("Server has started!");
			
			while (true) {
				Socket socket = myServer.accept();
				ObjectInputStream request = new ObjectInputStream(socket.getInputStream());
				
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				Response response = new Response("Success!", true);
				out.writeObject(response);
				
			}
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	
}
