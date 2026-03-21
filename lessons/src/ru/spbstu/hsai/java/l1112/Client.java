package ru.spbstu.hsai.java.l1112;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	public static void main(String[] args) {
		
		try {
			
			Socket client = new Socket("localhost", 30102);
			
			OutputStream clientOutputStream = client.getOutputStream();
			clientOutputStream.write("Hello world!\n".getBytes());
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(client.getInputStream())
			);
			
			System.out.println(reader.readLine());
			
		}
		catch (UnknownHostException e) {
			System.err.println("unknown host");
		} 
		catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		
	}
}
