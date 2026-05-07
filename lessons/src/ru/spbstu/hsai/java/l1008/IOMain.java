package ru.spbstu.hsai.java.l1008;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Scanner;

public class IOMain {
	
	private static final int CONSTANT_VALUE = 42; 
	
	
	public static void main(String[] args) {
		File file = new File("source/heavy.mkv");
		try {
			System.out.println(file.getPath());
			System.out.println(file.getAbsolutePath());
			System.out.println(file.getCanonicalPath());

			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			InputStream is = new FileInputStream(file);

			byte[] chunk = new byte[2048];

//			int readBytes = is.read(chunk);
//			while (readBytes > 0) {
//				processChunk(chunk, readBytes);
//				readBytes = is.read(chunk);
//			}

//			newFile.createNewFile();
//			writeFileIO();
//			writeBinaryFile();
			
//			Scanner sc = new Scanner(System.in);
//			int value = sc.nextInt();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void writeFileIO() {

		File newFile = new File("sink/test.txt");
//		FileWriter fw = null;
//		try {
//			fw = new FileWriter(newFile);
//			fw.write("Hello world!");
//		} catch (IOException e) {
//			System.err.println(e.getMessage());
//		} finally {
//			try {
//				fw.close();
//			} catch (IOException e) {
//				System.err.println(e.getMessage());
//			}
//		}

		try (FileWriter fw = new FileWriter(newFile)) {
			fw.write("Hello world!");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}

	private static void writeBinaryFile() {

		File file = new File("sink/binfile");
		try (OutputStream os = new FileOutputStream(file)) {
			byte[] data = new byte[2048];
			byte val = 42;
			Arrays.fill(data, val);
			os.write(data);
			
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private static void processChunk(byte[] chunk, int bytesCount) {
		// TODO Auto-generated method stub

	}

}
