package ru.spbstu.hsai.java.exam;

public class Yank {
	private static Yank singelton;
	private final static Object check = new Object();
	
	private Yank() {};
	
	
	
	public static Yank yank () { 
		if (singelton == null) {
			
			synchronized (Yank.class) {
				if (singelton == null) {
					singelton = new Yank();
				}
			}
		}
		return singelton;
	}
}
