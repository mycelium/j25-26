package ru.spbstu.hsai.java.exam;

import java.io.File;
import java.io.FileInputStream;

public class Test {
	public static void main(String[] args) {
		
		
//		synchronized (Yank.class) {
//			Yank yank = Yank.yank();
//			Yank yank2 = Yank.yank();
//			System.out.println(yank == yank2);
//		}
//		try
		
		try {
			doSmth();
		} catch (Exception e) {
			// TODDO: handle exception
		}
		
	}
	
	
	private static int doSmth() {
//		new FileInputStream(new File(""));
		return 0;
	}
}
