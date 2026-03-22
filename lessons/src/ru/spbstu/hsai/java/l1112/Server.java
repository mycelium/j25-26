package ru.spbstu.hsai.java.l1112;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String[] args) {
		try {
			
			ServerSocket socket = new ServerSocket(30102);
			Socket client = socket.accept();
			
			InputStream clientInputStream = client.getInputStream();
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(clientInputStream)
			);
			
			String clientStringData = reader.readLine();
			
			System.out.println(clientStringData);
			
			OutputStream clientOutputStream = client.getOutputStream();
			
			StringBuilder builder = new StringBuilder(clientStringData);
			clientOutputStream.write(
					builder.reverse().append("\n")
					.toString()
					.getBytes()
			);
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
