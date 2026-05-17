package ru.spbstu.hsai.java.l1001;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Exceptions {

	public static void main(String[] args) throws IOException {

//		var x = 42 / 0;

		try {
			Integer x = null;
			x.compareTo(42);
		} catch (ArithmeticException e) {
			System.err.println(e.getMessage());
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Crash");
		} finally {
			
		}
		
		try {
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		System.out.println("Hello World");
		
		throw new SPBSTUException();
		
//		doSmth();

	}

	private static void doSmth() throws IOException {
		Files.readAllBytes(Path.of(""));
	}

}
