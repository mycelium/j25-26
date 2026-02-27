package ru.spbstu.hsai.java.l1112;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;


public class Main {
	public static void main(String[] args) {
		ServerSocket server;
		
//		try {
//			NetworkInterface.getNetworkInterfaces().asIterator().forEachRemaining( ni ->{
//				System.out.println(ni.getDisplayName());
//			});
//		} catch (SocketException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		try {
			URLConnection conn = new URL("https://google.ru").openConnection();
			
			InputStream is = conn.getInputStream();
			BufferedReader bis = new BufferedReader(new InputStreamReader(is));
			System.out.println(bis.readLine());
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
