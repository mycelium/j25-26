package ru.spbstu.hsai.java.l0205;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
	
	private static final String IP = "127.0.0.1";
	private static final int PORT = 8080;
	
	public static void main(String[] args) {
		try (Socket socket = new Socket(IP, PORT)) {
			Request request = new Request(new byte[10]);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(request);
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			Response response = (Response)input.readObject();
			System.out.println(response.getTextResponse());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
}