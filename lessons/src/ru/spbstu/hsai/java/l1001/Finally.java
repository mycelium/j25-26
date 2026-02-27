package ru.spbstu.hsai.java.l1001;

import java.io.IOError;
import java.io.IOException;

public class Finally {
	public static void main(String[] args) {
//		try {
//			throw new RuntimeException("Crash");
////			System.exit(0);
//		} finally {
//			System.out.println("Hello");
//			throw new RuntimeException("Trick");
//		}
		
		Exception e = new IOException();
		
		Error error = new IOError(e);
		
		try {
			throw new IOException();
		} catch (IOException e2) {
			// TODO: handle exception
		}
		
		try {
			
		} catch (IOError e2) {
			// TODO: handle exception
		}
		
	}
}
