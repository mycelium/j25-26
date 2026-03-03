package ru.spbstu.hsai.java.exam;

import java.util.Iterator;
import java.util.Random;

public class Main {
	
	
	
	
	public static void main(String[] args) {
		var rand = new Random();
		
		for (int i = 0; i < rand.nextInt(1000); i++) {
			rand.nextDouble();
		}
		
		for (int i = 0; i < 20; i++) {
			System.out.println(i+ ": "+rand.nextInt(1, 15) + ", "+rand.nextInt(15, 30));
			
		}
		
	}
}
